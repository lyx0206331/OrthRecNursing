package com.chwishay.orthrecnursing

import com.chwishay.orthrecnursing.BluetoothServer.LOG_TAG
import com.chwishay.orthrecnursing.DispatchUtil.frameHead
import com.chwishay.orthrecnursing.DispatchUtil.getVerifyCode
import com.chwishay.orthrecnursing.DispatchUtil.jointAngleVelocity
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import java.nio.ByteBuffer

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

    var everydayTrainingDuration = 0
    var everydayTrainingNum = 0
    var eachGroupTrainingNum = 0
    var targetJointAngle = 0
    var currentTrainingNum = 0
    var jointAngle = 0
    var jointAngleVelocity = 0
    var lateralFemoralMuscle = 0
    var medialFemoris = 0
    var bicepsFemoris = 0
    var semitendinosusFemoris = 0
    var tibialisAnteriorMuscle = 0
    var peroneusLongus = 0
    var exception = 0

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
    
    fun init() {
        BluetoothServer.frameHead = frameHead
        BluetoothServer.parseBlock = {
            LOG_TAG.logE("待解析数据:${it.formatHexString(" ")}")
            writeLog("${it.formatHexString(" ")}")
            if (it != null && it.size == DATA_SIZE) {
                FrameData(data = it.copyOfRange(frameHead.size, DATA_SIZE - 1))
            } else {
                writeLog("数据异常:size:${it?.size.orDefault()}.content:${it.formatHexString(" ")}", LOG_ERROR)
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
                val result = frameData.data!!.parseData()/*DataInfo(
                    10, 65, 20, 40, (1..200).random().toShort(),
                    (1..40).random().toShort(), (10..100).random().toShort(),
                    (10..125).random().toShort(), (0..40).random().toByte(),
                    (0..40).random().toByte(), (0..40).random().toByte(), (0..40).random().toByte(),
                    (0..40).random().toByte(), (0..40).random().toByte(), (0..12).random().toByte()
                )*/
                appendData(result)
                lastFrameData = result
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
        this.copyOfRange(0, size).sum()
    }

    fun appendData(dataInfo: DataInfo) {
        everydayTrainingDuration = dataInfo.everydayTrainingDuration.toInt()
        everydayTrainingNum = dataInfo.everydayTrainingNum.toInt()
        eachGroupTrainingNum = dataInfo.eachGroupTrainingNum.toInt()
        targetJointAngle = dataInfo.targetJointAngle.toInt()
        currentTrainingNum = dataInfo.currentTrainingNum.toInt()
        jointAngle = dataInfo.jointAngle.toInt()
        jointAngleVelocity = dataInfo.jointAngleVelocity.toInt()
        lateralFemoralMuscle =
            if (dataInfo.jointAngle > lateralFemoralMuscle) dataInfo.jointAngle.toInt() else lateralFemoralMuscle
        medialFemoris += dataInfo.medialFemoris
        bicepsFemoris += dataInfo.bicepsFemoris
        semitendinosusFemoris += dataInfo.semitendinosusFemoris
        tibialisAnteriorMuscle += dataInfo.tibialisAnteriorMuscle
        peroneusLongus += dataInfo.peroneusLongus
    }

    fun resetData() {
        everydayTrainingDuration = 0
        everydayTrainingNum = 0
        eachGroupTrainingNum = 0
        targetJointAngle = 0
        currentTrainingNum = 0
        jointAngle = 0
        jointAngleVelocity = 0
        lateralFemoralMuscle = 0
        medialFemoris = 0
        bicepsFemoris = 0
        semitendinosusFemoris = 0
        tibialisAnteriorMuscle = 0
        peroneusLongus = 0
    }
}

data class DataInfo(
    var everydayTrainingDuration: Byte = 0,
    var everydayTrainingNum: Byte = 0,
    var eachGroupTrainingNum: Byte = 0,
    var targetJointAngle: Byte = 0,
    var sumTrainingDuration: Short = 0,
    var currentTrainingNum: Short = 0,
    var jointAngle: Short = 0,
    var jointAngleVelocity: Short = 0,
    var lateralFemoralMuscle: Byte = 0,    //股外侧肌
    var medialFemoris: Byte = 0,            //股内侧肌
    var bicepsFemoris: Byte = 0,            //股二头肌
    var semitendinosusFemoris: Byte = 0,    //股半腱肌
    var tibialisAnteriorMuscle: Byte = 0,  //胫前肌
    var peroneusLongus: Byte = 0,           //腓长肌
    var exceptionCode: Byte = 0
)

var testJob: Job? = null

fun test() {
    DispatchUtil.isTimerStart = !DispatchUtil.isTimerStart
    testJob = GlobalScope.launch {
        while (DispatchUtil.isTimerStart) {
            delay(20)
            val arrayData = byteArrayOf(
                10, 65, 20, 40, *(1..200).random().toShort().toBytesLE(),
                *(1..40).random().toShort().toBytesLE(), *(10..100).random().toShort().toBytesLE(),
                *(10..125).random().toShort().toBytesLE(), (0..40).random().toByte(),
                (0..40).random().toByte(), (0..40).random().toByte(), (0..40).random().toByte(),
                (0..40).random().toByte(), (0..40).random().toByte(), (0..12).random().toByte()
            )

            BluetoothServer.dataReceiveSubject.onNext(FrameData(data = arrayData))
        }
    }

}

fun ByteArray.parseData(): DataInfo {
    return ByteBuffer.wrap(this).let {
        DataInfo(
            it.get(),
            it.get(),
            it.get(),
            it.get(),
            it.short,
            it.short,
            it.short,
            it.short,
            it.get(),
            it.get(),
            it.get(),
            it.get(),
            it.get(),
            it.get(),
            it.get()
            )
    }
//    DataInfo(
//        this[0].toUByte().toInt(),
//        this[1].toUByte().toInt(),
//        this[2].toUByte().toInt(),
//        this[3].toUByte().toInt(),
//        this.copyOfRange(4, 6).toUnsignInt(),
//        this.copyOfRange(6, 8).toUnsignInt(),
//        this.copyOfRange(8, 10).toUnsignInt() / 10,
//        this.copyOfRange(10, 12).toUnsignInt() / 10,
//        this[12].toUByte().toInt(),
//        this[13].toUByte().toInt(),
//        this[14].toUByte().toInt(),
//        this[15].toUByte().toInt(),
//        this[16].toUByte().toInt(),
//        this[17].toUByte().toInt(),
//        this[18].toUByte().toInt()
//    )
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
    fun fromDataInfo(dataInfo: DataInfo) {
//        everydayTrainingDuration = 60
//        everydayTrainingGroupNum = 1
//        groupTrainingNum = 10
        targetAngle = dataInfo.targetJointAngle.toByte()
        targetAngleVelocityLowBit = jointAngleVelocity.toShort().toBytesLE()[0]
        targetAngleVelocityHighBit = jointAngleVelocity.toShort().toBytesLE()[1]
//        lateralFemoralMuscleIfWork = 1
//        medialFemoralMuscleIfWork = 1
//        bicepsFemorisIfWork = 1
//        semitendinosusFemorisIfWork = 1
//        anteriorTibialTendonIfWork = 1
//        peronealMuscleIfWork = 1
    }

    fun getTargetAngleVelocity() =
        byteArrayOf(targetAngleVelocityLowBit, targetAngleVelocityHighBit).read2IntLE()

    fun toFrameByteArray(): ByteArray {
        val hisDuration = historyTrainingDuration.toBytesLE()
        val hisNum = historyTrainingNum.toBytesLE()
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