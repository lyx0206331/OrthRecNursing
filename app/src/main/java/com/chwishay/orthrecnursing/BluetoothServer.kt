package com.chwishay.orthrecnursing

import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import androidx.annotation.IntDef
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

//                       _ooOoo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                       O\ = /O
//                   ____/`---'\____
//                 .   ' \\| |// `.
//                  / \\||| : |||// \
//                / _||||| -:- |||||- \
//                  | | \\\ - /// | |
//                | \_| ''\---/'' | |
//                 \ .-\__ `-` ___/-. /
//              ______`. .' /--.--\ `. . __
//           ."" '< `.___\_<|>_/___.' >'"".
//          | | : `- \`.;`\ _ /`;.`/ - ` : | |
//            \ \ `-. \_ __\ /__ _/ .-` / /
//    ======`-.____`-.___\_____/___.-`____.-'======
//                       `=---='
//
//    .............................................
//             佛祖保佑             永无BUG
/**
 * author:RanQing
 * date:2021/3/26 0026 14:34
 * description:
 */
object BluetoothServer {

    const val STATE_BT_DISCONNECT_SUCCESS = 0
    const val STATE_BT_DISCONNECT_FAILED = 1
    const val STATE_BT_CONNECT_SUCCESS = 2
    const val STATE_BT_CONNECT_FAILED = 3
    const val STATE_BT_DISCOVERY_STARTED = 4
    const val STATE_BT_DISCOVERY_FINISHED = 5

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(STATE_BT_DISCOVERY_STARTED, STATE_BT_DISCOVERY_FINISHED)
    annotation class BluetoothDiscState

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(STATE_BT_CONNECT_SUCCESS, STATE_BT_CONNECT_FAILED, STATE_BT_DISCONNECT_FAILED, STATE_BT_DISCONNECT_SUCCESS)
    annotation class BluetoothConnState

    const val STATE_IDLE = 0
    const val STATE_HEAD_0 = 1
    const val STATE_HEAD_1 = 2
    const val STATE_DATA = 3
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(STATE_IDLE, STATE_HEAD_0, STATE_HEAD_1, STATE_DATA)
    annotation class TransDataState

    @TransDataState
    private var cacheDataState = STATE_IDLE

    private var index = 0

    private val cacheDataArray = ByteArray(22)

    //帧头帧尾
    var frameHead: ByteArray? = null

    private val context = OrthRecApp.instance

    private val btAdapter: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var btManager: BluetoothManager? = if (isBleSupported()) context.getSystemService(
        Context.BLUETOOTH_SERVICE) as BluetoothManager else null

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private var isTransportable = false

    private var lastSendTime = 0L

    const val LOG_TAG = "BT_LOG"

    var parseBlock:((data: ByteArray?) -> FrameData?)? = null
    var verifyBlock: ((data: ByteArray?) -> Boolean?)? = null

    const val REQUEST_ENABLE_BT = 999
    private val DEV_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9B34FB")
//        val instance: BTConnectUtil by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { BTConnectUtil() }
//        @Volatile
//        private var instance: BTConnectUtil? = null
//        fun getInstance(activity: Activity) = instance ?: synchronized(this) { instance ?: BTConnectUtil(activity).also  { instance = it }}


    private val connStateSubject by lazy {
        BehaviorSubject.create<Int>()
    }

    fun onBtConnStateChange(): Observable<Int> {
        return connStateSubject
    }

    val dataReceiveSubject by lazy {
        PublishSubject.create<FrameData>()
    }

    fun onReceiveBytesData(): Observable<FrameData> {
        return dataReceiveSubject.observeOn(Schedulers.io())
    }

    private val deviceFoundSubject by lazy { PublishSubject.create<ArrayList<ClassicBtInfo>>() }

    fun onDeviceFoundChange(): Observable<ArrayList<ClassicBtInfo>> = deviceFoundSubject

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    //蓝牙连接状态.注意：配对时会发送停止扫描设备通知
    @BluetoothConnState
    var btConnState = STATE_BT_DISCONNECT_SUCCESS
        set(value) {
//            if (field != value) {
            field = value
            connStateSubject.onNext(field)
            "connectState".logE("state:$field")
//            }
        }

    var isConnected = btConnState == STATE_BT_CONNECT_SUCCESS

    private val discBtStateSubject by lazy { BehaviorSubject.create<Int>() }

    fun onBtDiscStateChange(): Observable<Int> = discBtStateSubject

    @BluetoothDiscState
    var btDiscState = STATE_BT_DISCOVERY_FINISHED
        set(value) {
            if (field != value) {
                field = value
                discBtStateSubject.onNext(field)
//                "discoveryState".logE("state:$field")
            }
        }

    var device : BluetoothDevice? = null
        set(value) {
            field = value
            isScanningOrConnecting = false
        }

    //正在搜索或者连接
    var isScanningOrConnecting = false

    private val bluetoothFilter = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
    }

    private val btReceiver by lazy {
        object : BroadcastReceiver() {
            private val devList = arrayListOf<ClassicBtInfo>()

            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.also { i ->
                    val dev = i.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    when(i.action) {
                        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                            isScanningOrConnecting = true
                            devList.clear()
                            btDiscState = STATE_BT_DISCOVERY_STARTED
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            isScanningOrConnecting = false
                            btDiscState = STATE_BT_DISCOVERY_FINISHED
                            device?.let {
                                GlobalScope.launch {
                                    createSocket(it)
                                }
                            }
                        }
                        BluetoothDevice.ACTION_FOUND -> {
                            dev?.also {
                                if (!TextUtils.isEmpty(it.name)) {
                                    if (devList.none { bt ->
                                                bt.dev.address == it.address
                                            }) {
                                        val rssi = i.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                                        "BT_LOG".logE("dev ${it.name}- ${it.address} rssi:$rssi")
                                        devList.add(0, ClassicBtInfo(it, rssi))
                                        deviceFoundSubject.onNext(devList)
                                    }
                                }
                            }
                        }
                        BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                            val bondState = dev?.bondState
                            "BT_LOG".logE("bond state changed: $bondState")
                            when (bondState) {
                                BluetoothDevice.BOND_NONE -> {
                                    isScanningOrConnecting = false
                                    showShortToast("解除配对")
                                }
                                BluetoothDevice.BOND_BONDING -> {
                                    isScanningOrConnecting = true
                                    showShortToast("正在配对")
                                }
                                BluetoothDevice.BOND_BONDED -> {
                                    isScanningOrConnecting = false
                                    showShortToast("已配对")
                                     btConnState = STATE_BT_CONNECT_SUCCESS
//                                    GlobalScope.launch {
//                                        delay(500)
//                                        connect(dev!!)
//                                    }
                                }
                            }
                        }
                        BluetoothDevice.ACTION_ACL_CONNECTED -> {
                            showShortToast("连接设备:${dev?.address}")
                            device = dev
//                            GlobalScope.launch {
//                                delay(1000)
//                                connect(dev!!)
//                            }
                            btConnState = STATE_BT_CONNECT_SUCCESS
                            isScanningOrConnecting = false
                        }
                        BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                            showShortToast("断开设备")
                            disconnect()
//                            device = null
                            btConnState = STATE_BT_DISCONNECT_SUCCESS
                            isScanningOrConnecting = false
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    init {
        registerBtReceiver()
    }

    private fun registerBtReceiver() {
        "Init BT Server".logE("register receiver!")
        context.registerReceiver(btReceiver, bluetoothFilter)
    }

    fun unregisterBtReceiver() {
        context.unregisterReceiver(btReceiver)
    }

    fun isConnected(dev: BluetoothDevice) = if (device == null) false else device?.address == dev.address

    /**
     * 是否支持蓝牙
     */
    private fun isBluetoothSupported() = btAdapter != null

    /**
     * 是否支持BLE
     */
    private fun isBleSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    /**
     * 是否已打开蓝牙
     */
    private fun isBluetoothEnabled() = btAdapter.state == BluetoothAdapter.STATE_ON

    /**
     * 蓝牙是否可用
     */
    fun isEnable(): Boolean {
        return if (!isBluetoothSupported()) {
            throw IllegalStateException("设备不支持蓝牙")
        } else {
            btAdapter.isEnabled.orDefault()
        }
    }

    fun isDiscovering() = btAdapter != null && btAdapter.isDiscovering

    /**
     * 自动异步打开蓝牙（无提示）
     */
    fun openBluetoothAsyn(activity: Activity) {
        btAdapter.enable().orDefault()
    }

    /**
     * 自动同步打开蓝牙（有提示）
     */
    fun openBluetoothSync(activity: Activity) {
        activity.startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BT
        )
    }

    /**
     * 关闭蓝牙
     */
    fun closeBluetooth() = btAdapter.disable().orDefault()

    /**
     * 扫描蓝牙设备
     */
    fun scanBluetoothDev(): Boolean {
        if (!isEnable()) {
            return false
        }
        if (btAdapter.isDiscovering) {
            btAdapter.cancelDiscovery()
        }
        return btAdapter.startDiscovery()
    }

    /**
     * 停止扫描蓝牙设备
     */
    fun cancelScanBluetoothDev(): Boolean {
        if (isDiscovering()) {
            return btAdapter.cancelDiscovery()
        }
        return true
    }

    /**
     * 获取已绑定设备
     */
    fun getBondedDevices(): Set<BluetoothDevice>? {
        return btAdapter.bondedDevices
    }

    fun getRemoteDevice(mac: String): BluetoothDevice? =
        if (!TextUtils.isEmpty(mac)) btAdapter.getRemoteDevice(mac) else null

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getConnectedBleDevices(): List<BluetoothDevice> {
        val devices = arrayListOf<BluetoothDevice>()
        btManager?.getConnectedDevices(BluetoothProfile.GATT)?.let { devices.addAll(it) }
        return devices
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getConnectStatus(mac: String): Int {
        val device = getRemoteDevice(mac)
        return btManager?.getConnectionState(device, BluetoothProfile.GATT).orDefault(-1)
    }

    fun getBondState(mac: String): Int =
            getRemoteDevice(mac)?.bondState.orDefault(BluetoothDevice.BOND_NONE)

    fun getBondedClassicBtDevices(): List<BluetoothDevice>? = btAdapter.bondedDevices?.toList()

    fun isConnected(mac: String): Boolean =
            getConnectStatus(mac) == BluetoothProfile.STATE_CONNECTED

    fun getProfile() {
        btAdapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                TODO("Not yet implemented")
            }

            override fun onServiceDisconnected(profile: Int) {
                TODO("Not yet implemented")
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    /**
     * 手动配对
     */
    fun createBond(dev: BluetoothDevice): Boolean {
        cancelScanBluetoothDev()
        var result = false
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            val createBondMethod = BluetoothDevice::class.java.getMethod("createBond")
            result = createBondMethod.invoke(dev) as Boolean
        }
        return result
    }

    /**
     * 自动配对（勿需输入PIN码）
     * 有点问题
     */
    fun autoBond(dev: BluetoothDevice, pin: String = "0000"): Boolean {
        cancelScanBluetoothDev()
        val autoBondMethod = BluetoothDevice::class.java.getMethod("setPin", ByteArray::class.java)
        val result = autoBondMethod.invoke(dev, pin.toByteArray()) as Boolean
        return result
    }

    /**
     * 解除配对
     */
    fun removeBond(dev: BluetoothDevice): Boolean {
        val removeBondMethod = BluetoothDevice::class.java.getMethod("removeBond")
        val result = removeBondMethod.invoke(dev) as Boolean
        return result
    }

    private val needConnect = false

    /**
     * 连接设备
     */
    suspend fun connect(dev: BluetoothDevice) {
        withContext(Dispatchers.IO) {
            device = dev
            cancelScanBluetoothDev()
//            socket?.close()
//            createSocket(dev)
        }
    }

    suspend fun createSocket(dev: BluetoothDevice) {
        GlobalScope.launch(Dispatchers.IO) {
            isScanningOrConnecting = true
            socket = dev.createRfcommSocketToServiceRecord(DEV_UUID)
            try {
                socket?.connect()
                isTransportable = true
                buildIOStream()
                writeLog("连接设备:${dev.name},mac:${dev.address}")
                isScanningOrConnecting = false
            } catch (ex: Exception) {
                writeLog("连接异常:${ex.message}", LOG_ERROR)
                isScanningOrConnecting = false
                //                release()
            }
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        isTransportable = false
        device = null
        release()
        writeLog("断开连接", LOG_WARN)
    }

    /**
     * 创建IO流
     */
    private suspend fun buildIOStream() {
        socket?.also { s ->
            inputStream = s.inputStream
            outputStream = s.outputStream

            val buffer = ByteArray(512)
            var count: Int

            while (isTransportable) {
                try {
                    delay(40)
//                    LOG_TAG.logE("read abailable:${inputStream?.available()}")
                    if (inputStream?.available() != 0) {
                        count = inputStream?.read(buffer).orDefault()
                        val data = buffer.copyOf(count)
//                        LOG_TAG.logE("receive data: size:$count:: ${data.contentToString()}")
//                        LOG_TAG.logE("接收数据:${data.formatHexString(" ")}")
                        if (frameHead == null) {
                            LOG_TAG.logE("数据帧头不能为空")
                        } else {
                            data.forEach {
                                if (it == frameHead!![0]) {
                                    cacheDataState = STATE_HEAD_0
                                    index = 0
                                    cacheDataArray[index] = it
                                } else if (it == frameHead!![1]) {
                                    if (cacheDataState == STATE_HEAD_0) {
                                        cacheDataState = STATE_HEAD_1
                                        index++
                                        cacheDataArray[index] = it
                                    } else {
                                        cacheDataState = STATE_IDLE
                                    }
                                } else if (cacheDataState == STATE_HEAD_1) {
                                    cacheDataState = STATE_DATA
                                    index++
                                    cacheDataArray[index] = it
                                } else if (cacheDataState == STATE_DATA) {
                                    index++
                                    cacheDataArray[index] = it
                                    if (index == cacheDataArray.size - 1) {
                                        val cmd = cacheDataArray.copyOf()
                                        if (verifyBlock?.invoke(cmd).orDefault()) {
                                            cmd.toFrameData(parseBlock)?.also { fd ->
                                                dataReceiveSubject.onNext(fd)
                                            }
                                        } else {
                                            writeLog(
                                                "校验失败:${
                                                    cmd.contentToString().also { LOG_TAG.logE(it) }
                                                }", LOG_ERROR
                                            )
                                        }
                                        cacheDataState = STATE_IDLE
                                    }
                                } else {
                                    cacheDataState = STATE_IDLE
                                }
                            }
                        }

                    }
                } catch (ex: Exception) {
                    writeLog("接收消息异常:${ex.message}", LOG_ERROR)
                    if (release()) {
                        writeLog("接收消息异常,断开连接", LOG_ERROR)
                    }
                    break
                }
            }
        }
    }

    /**
     * 释放资源
     */
    fun release(): Boolean {
        return try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
            true
        } catch (ex: IOException) {
            writeLog("蓝牙断开异常:${ex.message}", LOG_ERROR)
            false
        } finally {
            inputStream = null
            outputStream = null
            socket = null
        }
    }

    /**
     * 发送数据
     * ps:两条指令之间至少间隔50ms
     */
    suspend fun sendData(data: ByteArray, delayMillis: Long = 1000) {
            delay(500)
            if (isTransportable) {
//                delay(realDelay)
                write(data)
            }
    }

//    fun <T> sendDataWaitBack(data: ByteArray): T = coroutineScope {
//        val deferred = async {
//            dataReceiveSubject
//        }
//    }

    /**
     * 发送数据
     */
    private fun write(data: ByteArray) : Boolean {
        try {
            if (outputStream == null) {
                return false
            }
            outputStream?.write(data)
//            outputStream?.flush()
            writeLog("写入数据:${data.formatHexString(" ")?.also { "写数据".logE(it) }}", LOG_WARN)
            return true
        } catch (ex: Exception) {
            writeLog("写入失败:${data.formatHexString(" ")}", LOG_ERROR)
            return false
        }
    }
}

fun BluetoothDevice.isBond(): Boolean {
    val bondDevs = BluetoothServer.getBondedDevices()
    bondDevs?.forEach {
        if (it.address == this.address) {
            return true
        }
    }
    return false
}

data class ClassicBtInfo(val dev: BluetoothDevice, val rssi: Short) {
    /**
     * 检测蓝牙是否已绑定
     *
     * @return
     */
    fun isBond(): Boolean = dev.isBond()
}

data class FrameData(val type: Int = -1, val data: ByteArray? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FrameData

        if (type != other.type) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}

fun ByteArray?.toFrameData(block: ((data: ByteArray?) -> FrameData?)? = null): FrameData? {
    return block?.invoke(this)
}