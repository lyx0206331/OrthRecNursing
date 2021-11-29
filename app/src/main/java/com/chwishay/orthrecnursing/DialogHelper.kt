package com.chwishay.orthrecnursing

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chwishay.orthrecnursing.DispatchUtil.format2Date
import com.chwishay.orthrecnursing.DispatchUtil.getVerifyCode
import com.chwishay.orthrecnursing.views.ExpandableEditText
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.find
import org.w3c.dom.Text

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
 * date:2021/10/21 0021 3:43
 * description:
 */

class BtDevListDialog(context: Context, private val lifecycleOwner: LifecycleOwner) :
    Dialog(context, R.style.DialogTheme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemView = LayoutInflater.from(context).inflate(R.layout.dialog_device_list, null, true)
        setContentView(itemView)

        val flLoading = itemView.find<FrameLayout>(R.id.flLoading)
        val tvStateText = itemView.find<TextView>(R.id.tvStateText)
        val tvScan = itemView.find<TextView>(R.id.tvScan)
        val adapter = BtDevAdapter { dev ->
            lifecycleOwner.lifecycleScope.launch {
                if (BluetoothServer.isConnected(dev.dev)) {
                    showShortToast("当前设备已连接")
                } else {
                    flLoading.isVisible = true
                    BluetoothServer.device = dev.dev
                    withContext(Dispatchers.IO) {
                        lifecycleOwner.lifecycleScope.launch {
                            if (dev.isBond()) {
                                tvStateText.text =
                                    "${context.getString(R.string.connect_ongoing)}\n${BluetoothServer.device!!.address}"
                            } else {
                                lifecycleOwner.lifecycleScope.launch {
                                    tvStateText.text =
                                        "${context.getString(R.string.bind_ongoing)}\n${BluetoothServer.device!!.address}"
                                }
                            }
                        }
                        BluetoothServer.createSocket(dev.dev)
                    }
                }
            }
        }

        val rvDevices = itemView.find<RecyclerView>(R.id.rvDevices).apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(
                    ColorDrawable(ContextCompat.getColor(context, R.color.black_232323))
                )
            })
        }
        rvDevices.adapter = adapter
        BluetoothServer.onDeviceFoundChange().observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.submitList(it.toMutableList())
            rvDevices.scrollToPosition(0)
        }
        BluetoothServer.onBtConnStateChange().observeOn(AndroidSchedulers.mainThread()).subscribe {
            when (it) {
                BluetoothServer.STATE_BT_CONNECT_SUCCESS -> {
                    SP.put(MASTER_MAC_KEY, BluetoothServer.device?.address)
                    dismiss()
                }
                BluetoothServer.STATE_BT_CONNECT_FAILED -> {
                    showShortToast(R.string.connect_failed)
                    flLoading.isVisible = false
                }
            }
        }
        BluetoothServer.onBtDiscStateChange().observeOn(AndroidSchedulers.mainThread()).subscribe {
            when (it) {
                BluetoothServer.STATE_BT_DISCOVERY_STARTED -> {
                    flLoading.isVisible = true
                    tvStateText.setText(R.string.search_ongoing)
                    tvScan.setText(R.string.stop_scan)
                }
                BluetoothServer.STATE_BT_DISCOVERY_FINISHED -> {
                    flLoading.isVisible = false
                    tvScan.setText(R.string.start_scan)
                }
            }
        }
        flLoading.setOnTouchListener { _, _ ->
            true
        }
        tvScan.onClick {
            if (BluetoothServer.btDiscState == BluetoothServer.STATE_BT_DISCOVERY_STARTED) {
                stopScan()
            } else {
                startScan()
            }
        }
        setOnShowListener {
            startScan()
        }
        setOnDismissListener {
            if (BluetoothServer.btDiscState == BluetoothServer.STATE_BT_DISCOVERY_STARTED) {
                stopScan()
            }
        }
    }

    private fun stopScan() {
        BluetoothServer.cancelScanBluetoothDev()
    }

    private fun startScan() {
        BluetoothServer.device = null
        BluetoothServer.scanBluetoothDev()
    }
}

class ReportDialog(context: Context, private val lifecycleOwner: LifecycleOwner) :
    Dialog(context, R.style.DialogTheme) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_report)

//        DispatchUtil.resultLiveData.value

        find<TextView>(R.id.tvClose).onClick {
            cancel()
        }
        find<TextView>(R.id.tvTargetTrainingDuration).text =
            "${DispatchUtil.targetTrainingDuration}m"
        find<TextView>(R.id.tvActuallyTrainingDuration).text =
            "${DispatchUtil.actuallyTrainingDurationSum.format2Date()}"
        find<TextView>(R.id.tvTargetTrainingNum).text = "${DispatchUtil.targetTrainingNum}次"
        find<TextView>(R.id.tvActuallyTrainingNum).text = "${DispatchUtil.actuallyTrainingNum}次"
        find<TextView>(R.id.tvEffectiveTrainingNum).text = "${DispatchUtil.effectiveTrainingNum}次"
        find<TextView>(R.id.tvSettingJointActiveRange).text =
            "0~${DispatchUtil.settingJointActiveRange}°"
        find<TextView>(R.id.tvActuallyJointActiveRange).text =
            "${DispatchUtil.actuallyJointActiveRange}°"
        find<TextView>(R.id.tvSettingJointAngleVelocity).text =
            "${DispatchUtil.settingJointAngleVelocity}°/s"
        find<TextView>(R.id.tvActuallyJointAngleVelocity).text =
            "${DispatchUtil.actuallyJointAngleVelocitySum / DispatchUtil.actuallyTrainingDurationSum}°/s"
        find<TextView>(R.id.tvAverageStrengthOfLateralThigh).text =
            "${DispatchUtil.averageStrengthOfLateralThighSum / DispatchUtil.actuallyTrainingDurationSum}"
        find<TextView>(R.id.tvAverageStrengthOfMedialFemoris).text =
            "${DispatchUtil.averageStrengthOfMedialFemorisSum / DispatchUtil.actuallyTrainingDurationSum}"
        find<TextView>(R.id.tvAverageStrengthOfBicepsFemoris).text =
            "${DispatchUtil.averageStrengthOfBicepsFemorisSum / DispatchUtil.actuallyTrainingDurationSum}"
        find<TextView>(R.id.tvAverageStrengthOfSemitendinosusFemoris).text =
            "${DispatchUtil.averageStrengthOfSemitendinosusFemorisSum / DispatchUtil.actuallyTrainingDurationSum}"
        find<TextView>(R.id.tvAverageStrengthOfTibialisAnteriormuscle).text =
            "${DispatchUtil.averageStrengthOfTibialisAnteriormuscleSum / DispatchUtil.actuallyTrainingDurationSum}"
        find<TextView>(R.id.tvAverageStrengthOfPeroneusLongus).text =
            "${DispatchUtil.averageStrengthOfPeroneusLongusSum / DispatchUtil.actuallyTrainingDurationSum}"
    }
}

class SettingDialog(context: Context, private val lifecycleOwner: LifecycleOwner) :
    Dialog(context, R.style.DialogTheme) {

    private lateinit var etEverydayTrainingDuration: ExpandableEditText
    private lateinit var etEverydayTrainingGroup: ExpandableEditText
    private lateinit var etGroupTrainingNum: ExpandableEditText
    private lateinit var etTargetAngle: ExpandableEditText
    private lateinit var etTargetAngleVelocity: ExpandableEditText
    private lateinit var cbLateralFemoralMuscleIfWork: CheckBox
    private lateinit var cbMedialFemoralMuscleIfWork: CheckBox
    private lateinit var cbBicepsFemorisIfWork: CheckBox
    private lateinit var cbSemitendinosusFemorisIfWork: CheckBox
    private lateinit var cbAnteriorTibialTendonIfWork: CheckBox
    private lateinit var cbPeronealMuscleIfWork: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_setting)
        etEverydayTrainingDuration = find<ExpandableEditText>(R.id.etEverydayTrainingDuration).apply {
            centerText = "${DispatchUtil.paramsInfo.everydayTrainingDuration}"
        }
        etEverydayTrainingGroup = find<ExpandableEditText>(R.id.etEverydayTrainingGroup).apply {
            centerText = "${DispatchUtil.paramsInfo.everydayTrainingGroupNum}"
        }
        etGroupTrainingNum = find<ExpandableEditText>(R.id.etGroupTrainingNum).apply {
            centerText = "${DispatchUtil.paramsInfo.groupTrainingNum}"
        }
        etTargetAngle = find<ExpandableEditText>(R.id.etTargetAngle).apply {
            centerText = "${DispatchUtil.paramsInfo.targetAngle}"
        }
        etTargetAngleVelocity = find<ExpandableEditText>(R.id.etTargetAngleVelocity).apply {
            centerText = "${DispatchUtil.paramsInfo.getTargetAngleVelocity()}"
        }
        cbLateralFemoralMuscleIfWork = find<CheckBox>(R.id.cbLateralFemoralMuscleIfWork).apply {
            setOnCheckedChangeListener { buttonView, isChecked ->
                DispatchUtil.paramsInfo.lateralFemoralMuscleIfWork = if (isChecked) 1 else 0
            }
        }
        cbMedialFemoralMuscleIfWork = find<CheckBox>(R.id.cbMedialFemoralMuscleIfWork).apply {
            setOnCheckedChangeListener { buttonView, isChecked ->
                DispatchUtil.paramsInfo.medialFemoralMuscleIfWork = if (isChecked) 1 else 0
            }
        }
        cbBicepsFemorisIfWork = find<CheckBox>(R.id.cbBicepsFemorisIfWork).apply {
            setOnCheckedChangeListener { buttonView, isChecked ->
                DispatchUtil.paramsInfo.bicepsFemorisIfWork = if (isChecked) 1 else 0
            }
        }
        cbSemitendinosusFemorisIfWork = find<CheckBox>(R.id.cbSemitendinosusFemorisIfWork).apply {
            setOnCheckedChangeListener { buttonView, isChecked ->
                DispatchUtil.paramsInfo.semitendinosusFemorisIfWork = if (isChecked) 1 else 0
            }
        }
        cbAnteriorTibialTendonIfWork = find<CheckBox>(R.id.cbAnteriorTibialTendonIfWork).apply {
            setOnCheckedChangeListener { buttonView, isChecked ->
                DispatchUtil.paramsInfo.anteriorTibialTendonIfWork = if (isChecked) 1 else 0
            }
        }
        cbPeronealMuscleIfWork = find<CheckBox>(R.id.cbPeronealMuscleIfWork).apply {
            setOnCheckedChangeListener { buttonView, isChecked ->
                DispatchUtil.paramsInfo.peronealMuscleIfWork = if (isChecked) 1 else 0
            }
        }

        find<TextView>(R.id.tvConfirm).onClick {
            if (BluetoothServer.btConnState != BluetoothServer.STATE_BT_CONNECT_SUCCESS) {
                showShortToast("请先搜索连接设备...")
                return@onClick
            }
            if (etEverydayTrainingDuration.centerText.isNullOrEmpty() ||
                etEverydayTrainingGroup.centerText.isNullOrEmpty() ||
                etGroupTrainingNum.centerText.isNullOrEmpty() ||
                etTargetAngle.centerText.isNullOrEmpty() ||
                etTargetAngleVelocity.centerText.isNullOrEmpty()) {
                showShortToast("参数输入异常，请检查参数")
                return@onClick
            }
            DispatchUtil.paramsInfo.everydayTrainingDuration =
                Integer.parseInt(etEverydayTrainingDuration.centerText).toByte()
            DispatchUtil.paramsInfo.everydayTrainingGroupNum =
                Integer.parseInt(etEverydayTrainingGroup.centerText).toByte()
            DispatchUtil.paramsInfo.groupTrainingNum =
                Integer.parseInt(etGroupTrainingNum.centerText).toByte()
            DispatchUtil.paramsInfo.targetAngle = Integer.parseInt(etTargetAngle.centerText).toByte()
            Integer.parseInt(etTargetAngleVelocity.centerText).toBytesLE().also {
                DispatchUtil.paramsInfo.targetAngleVelocityLowBit = it[0]
                DispatchUtil.paramsInfo.targetAngleVelocityHighBit = it[1]
            }
            lifecycleOwner.lifecycleScope.launch {
                BluetoothServer.sendData(DispatchUtil.paramsInfo.toFrameByteArray())
                cancel()
            }
        }
    }
}


//class ParamsInfo(
//): ViewModel() {
//    var everydayTrainingDuration = MutableLiveData<Byte>(0)   //每天训练时长
//    var everydayTrainingGroupNum = MutableLiveData<Byte>(0)   //每天训练组数
//    var groupTrainingNum = MutableLiveData<Byte>(0)   //每组训练次数
//    var targetAngle = MutableLiveData<Byte>(0)    //目标角度
//    var targetAngleVelocityLowBit = MutableLiveData<Byte>(40) //目标角速度-低位
//    var targetAngleVelocityHighBit = MutableLiveData<Byte>(0)    //目标角速度-高位
//    var lateralFemoralMuscleIfWork = MutableLiveData<Byte>(0) //股外侧肌
//    var medialFemoralMuscleIfWork = MutableLiveData<Byte>(0)  //股内侧肌
//    var bicepsFemorisIfWork = MutableLiveData<Byte>(0)    //股二头肌
//    var semitendinosusFemorisIfWork = MutableLiveData<Byte>(0)    //股半腱肌
//    var anteriorTibialTendonIfWork = MutableLiveData<Byte>(0) //胫前肌
//    var peronealMuscleIfWork = MutableLiveData<Byte>(0)    //腓长肌
//
//    fun toFrameByteArray(): ByteArray {
//        val content = byteArrayOf(everydayTrainingDuration.value!!, everydayTrainingGroupNum.value!!, groupTrainingNum.value!!, targetAngle.value!!,
//            targetAngleVelocityLowBit.value!!, targetAngleVelocityHighBit.value!!, lateralFemoralMuscleIfWork.value!!, medialFemoralMuscleIfWork.value!!,
//            bicepsFemorisIfWork.value!!, semitendinosusFemorisIfWork.value!!, anteriorTibialTendonIfWork.value!!, peronealMuscleIfWork.value!!)
//        val data = byteArrayOf(*DispatchUtil.frameHead, *content)
//        return byteArrayOf(*data, data.getVerifyCode(data.size).toByte())
//    }
//}