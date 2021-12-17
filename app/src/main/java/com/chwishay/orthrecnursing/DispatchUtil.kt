package com.chwishay.orthrecnursing

import com.chwishay.orthrecnursing.DispatchUtil.frameHead
import com.chwishay.orthrecnursing.DispatchUtil.getVerifyCode
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*

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
 * date:2021/10/25 0025 3:51
 * description:
 */
object DispatchUtil {

    val DATA_SIZE = 22

    val resultSubject by lazy {
        BehaviorSubject.create<DataInfo>()
//        BehaviorProcessor.create<DataInfo>().apply { observeOn(Schedulers.io()) }
//        BehaviorSubject.create<DataInfo>().toFlowable(BackpressureStrategy.LATEST)
    }

    var lastFrameData: DataInfo = DataInfo()

    fun onResultObservable(): Observable<DataInfo> = resultSubject

    var everydayTrainingDuration = 0.toUInt()
    var everydayTrainingNum = 0.toUInt()
    var eachGroupTrainingNum = 0.toUInt()
    var targetJointAngle = 0.toUInt()
    var sumTrainingDuration = 0.toShort()
    var currentTrainingNum = 0.toShort()
    var jointAngle = 0f
    var jointAngleVelocity = 0f
    var lateralFemoralMuscle = 0.toUInt()
    var medialFemoris = 0.toUInt()
    var bicepsFemoris = 0.toUInt()
    var semitendinosusFemoris = 0.toUInt()
    var tibialisAnteriorMuscle = 0.toUInt()
    var peroneusLongus = 0.toUInt()
    var exception = 0.toUInt()

//    val timeCounterLiveData = MutableLiveData("0m0s")

    val frameHead = byteArrayOf(0xAB.toByte(), 0xCD.toByte())

//    private var timerValue = 0
//        set(value) {
//            field = value
//            if (field > 0) {
//                eachGroupTrainingNum = field
//            }
//            timeCounterLiveData.postValue(field.format2Date())
//        }

    fun Int.format2Date() = if (this < 0) "0m0s" else "${this / 60}m${this % 60}s"

//    private var timerJob: Job? = null

    var isTimerStart = false
        set(value) {
            field = value
            if (field) {
//                timerValue = 0
                resetData()
//                timerJob = GlobalScope.launch(Dispatchers.IO) {
//                    while (true) {
//                        delay(1000)
//                        ++timerValue
//                    }
//                }
//            } else {
//                timerJob?.cancel("停止计时")
            } else {
//                testJob?.cancel("停止发送数据")
            }
        }

    var params: ParamsInfo = ParamsInfo()
        set(value) {
            field = value
            SP.put(PARAMS, field)
        }
        get() = SP.get<ParamsInfo>(PARAMS) ?: ParamsInfo(
            0.toShort(), 0.toShort(), 60.toByte(), 1.toByte(), 10.toByte(),
            60, 40, 0, 1, 1,
            1, 1, 1, 1
        )
    private var saveParamIndex = 0

    fun init() {
        BluetoothServer.frameHead = frameHead
        BluetoothServer.parseBlock = {
//            LOG_TAG.logE("待解析数据:${it.formatHexString(" ")}")
//            writeLog("${it.formatHexString(" ")}")
            if (it != null && it.size == DATA_SIZE) {
                FrameData(data = it.copyOfRange(frameHead.size, DATA_SIZE - 1))
            } else {
                writeLog(
                    "数据异常:size:${it?.size.orDefault()}.content:${it.formatHexString(" ")}",
                    LOG_ERROR
                )
                null
            }
        }
        BluetoothServer.verifyBlock = {
            val code = if (it == null || it.size <= 3) 0 else {
                it.copyOfRange(0, it.size - 1).getVerifyCode()
            }
            code.toUByte() == it!!.last().toUByte()
        }
        BluetoothServer.onReceiveBytesData().subscribe({ frameData ->
            if (frameData != null) {
//                LOG_TAG.logE("$frameData")
                val result = frameData.data!!.parseData()
                /*DataInfo(
                    10, 65, 20, 40, (1..200).random().toShort(),
                    (1..40).random().toShort(), (10..100).random().toShort(),
                    (10..125).random().toShort(), (0..40).random().toByte(),
                    (0..40).random().toByte(), (0..40).random().toByte(), (0..40).random().toByte(),
                    (0..40).random().toByte(), (0..40).random().toByte(), (0..12).random().toByte()
                )*/
                appendData(result)
                lastFrameData = result
                if (++saveParamIndex % 500 == 0) {
                    params.historyTrainingDuration = lastFrameData.sumTrainingDuration.toShort()
                    params.historyTrainingNum = lastFrameData.currentTrainingNum.toShort()
                    params = params.copy()
                }
                resultSubject.onNext(result)
            }
        }, {
            "Exception".logE(it.message ?: "unknown exception")
        })
    }

    /**
     * 获取校验码
     */
    fun ByteArray.getVerifyCode(): Int = if (this == null || this.size <= 3) 0 else {
        this.sum()
    }

    fun appendData(dataInfo: DataInfo) {
        everydayTrainingDuration = dataInfo.everydayTrainingDuration
        everydayTrainingNum = dataInfo.everydayTrainingNum
        eachGroupTrainingNum = dataInfo.eachGroupTrainingNum
        targetJointAngle = dataInfo.targetJointAngle
        sumTrainingDuration = dataInfo.sumTrainingDuration
        currentTrainingNum = dataInfo.currentTrainingNum
        jointAngle = dataInfo.jointAngle
        jointAngleVelocity = dataInfo.jointAngleVelocity
        lateralFemoralMuscle += dataInfo.lateralFemoralMuscle
        medialFemoris += dataInfo.medialFemoris
        bicepsFemoris += dataInfo.bicepsFemoris
        semitendinosusFemoris += dataInfo.semitendinosusFemoris
        tibialisAnteriorMuscle += dataInfo.tibialisAnteriorMuscle
        peroneusLongus += dataInfo.peroneusLongus
    }

    fun resetData() {
        everydayTrainingDuration = 0.toUInt()
        everydayTrainingNum = 0.toUInt()
        eachGroupTrainingNum = 0.toUInt()
        targetJointAngle = 0.toUInt()
        currentTrainingNum = 0.toShort()
        jointAngle = 0f
        jointAngleVelocity = 0f
        lateralFemoralMuscle = 0.toUInt()
        medialFemoris = 0.toUInt()
        bicepsFemoris = 0.toUInt()
        semitendinosusFemoris = 0.toUInt()
        tibialisAnteriorMuscle = 0.toUInt()
        peroneusLongus = 0.toUInt()
    }
}

data class DataInfo(
    var everydayTrainingDuration: UInt = 0.toUInt(),
    var everydayTrainingNum: UInt = 0.toUInt(),
    var eachGroupTrainingNum: UInt = 0.toUInt(),
    var targetJointAngle: UInt = 0.toUInt(),
    var sumTrainingDuration: Short = 0,
    var currentTrainingNum: Short = 0,
    var jointAngle: Float = 0f,
    var jointAngleVelocity: Float = 0f,
    var lateralFemoralMuscle: UInt = 0.toUInt(),    //股外侧肌
    var medialFemoris: UInt = 0.toUInt(),            //股内侧肌
    var bicepsFemoris: UInt = 0.toUInt(),            //股二头肌
    var semitendinosusFemoris: UInt = 0.toUInt(),    //股半腱肌
    var tibialisAnteriorMuscle: UInt = 0.toUInt(),  //胫前肌
    var peroneusLongus: UInt = 0.toUInt(),           //腓长肌
    var exceptionCode: Int = 0
)

var testJob: Job? = null

fun test() {
    DispatchUtil.isTimerStart = !DispatchUtil.isTimerStart
    if (DispatchUtil.isTimerStart) {
        testJob = GlobalScope.launch {
            while (DispatchUtil.isTimerStart) {
                delay(20)
                val arrayData = byteArrayOf(
                    10,
                    65,
                    20,
                    40,
                    *(1..200).random().toShort().toByteArrayLE(),
                    *(1..40).random().toShort().toByteArrayLE(),
                    *(10..100).random().toShort().toByteArrayLE(),
                    *(10..125).random().toShort().toByteArrayLE(),
                    (0..40).random().toByte(),
                    (0..40).random().toByte(),
                    (0..40).random().toByte(),
                    (0..40).random().toByte(),
                    (0..40).random().toByte(),
                    (0..40).random().toByte(),
                    (0..12).random().toByte()
                )

                BluetoothServer.dataReceiveSubject.onNext(FrameData(data = arrayData))
            }
        }
    } else {
        testJob?.cancel("cancel test job")
    }

}

fun Float.toBytes() = java.lang.Float.floatToIntBits(this)

fun ByteArray.parseData(): DataInfo {
    return DataInfo(
        this[0].toUInt(),
        this[1].toUInt(),
        this[2].toUInt(),
        this[3].toUInt(),
        this.copyOfRange(4, 6).toShortLE(),
        this.copyOfRange(6, 8).toShortLE(),
        this.copyOfRange(8, 10).toShortLE() / 10f,
        this.copyOfRange(10, 12).toShortLE() / 10f,
        this[12].toUInt(),
        this[13].toUInt(),
        this[14].toUInt(),
        this[15].toUInt(),
        this[16].toUInt(),
        this[17].toUInt(),
        this[18].toInt()
    )
}

data class ParamsInfo(
                        var historyTrainingDuration: Short = 0.toShort(),
                        var historyTrainingNum: Short = 0.toShort(),
                        var everydayTrainingDuration: Byte = 0.toByte(),   //每天训练时长
                      var everydayTrainingGroupNum: Byte = 1.toByte(),   //每天训练组数
                      var groupTrainingNum: Byte = 10.toByte(),   //每组训练次数
                      var targetAngle: Byte = 0,    //目标角度
                      var targetAngleVelocityLowBit: Byte = 40, //目标角速度-低位
                      var targetAngleVelocityHighBit: Byte = 0,    //目标角速度-高位
                      var lateralFemoralMuscleIfWork: Byte = 0, //股外侧肌
                      var medialFemoralMuscleIfWork: Byte = 0,  //股内侧肌
                      var bicepsFemorisIfWork: Byte = 0,    //股二头肌
                      var semitendinosusFemorisIfWork: Byte = 0,    //股半腱肌
                      var anteriorTibialTendonIfWork: Byte = 0, //胫前肌
                      var peronealMuscleIfWork: Byte = 0    //腓长肌
) {

    fun getTargetAngleVelocity() =
        byteArrayOf(targetAngleVelocityLowBit, targetAngleVelocityHighBit).toShortLE()

    fun toFrameByteArray(): ByteArray {
        val hisDuration = historyTrainingDuration.toByteArrayLE()
        val hisNum = historyTrainingNum.toByteArrayLE()
        val content = byteArrayOf(
            hisDuration[0],
            hisDuration[1],
            hisNum[0],
            hisNum[1],
            everydayTrainingDuration,
            everydayTrainingGroupNum,
            groupTrainingNum,
            targetAngle,
            targetAngleVelocityLowBit,
            targetAngleVelocityHighBit,
            lateralFemoralMuscleIfWork,
            medialFemoralMuscleIfWork,
            bicepsFemorisIfWork,
            semitendinosusFemorisIfWork,
            anteriorTibialTendonIfWork,
            peronealMuscleIfWork
        )
        val data = byteArrayOf(*frameHead, *content)
        return byteArrayOf(*data, data.getVerifyCode().toByte())
    }
}

fun ByteArray.toUnsignInt(): Int =
    (this[1].toInt() and 0xFF) shl 8 or (this[0].toInt() and 0xFF)