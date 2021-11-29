package com.chwishay.orthrecnursing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chwishay.orthrecnursing.DispatchUtil.frameHead
import com.chwishay.orthrecnursing.DispatchUtil.getVerifyCode
import com.chwishay.orthrecnursing.DispatchUtil.settingJointAngleVelocity
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



    val resultLiveData = MutableLiveData<DataInfo>()

    val resultSubject by lazy { BehaviorSubject.create<DataInfo>() }

    var paramsInfo = ParamsInfo(60, 1, 10,
        60, 40, 0, 1, 1,
        1, 1, 1, 1)

    fun onResultObservable(): Observable<DataInfo> = resultSubject

    var targetTrainingDuration = 0
    var actuallyTrainingDurationSum = 0
    var targetTrainingNum = 0
    var actuallyTrainingNum = 0
    var effectiveTrainingNum = 0
    var settingJointActiveRange = 0
    var actuallyJointActiveRange = 0
    var settingJointAngleVelocity = 0
    var actuallyJointAngleVelocitySum = 0
    var averageStrengthOfLateralThighSum = 0
    var averageStrengthOfMedialFemorisSum = 0
    var averageStrengthOfBicepsFemorisSum = 0
    var averageStrengthOfSemitendinosusFemorisSum = 0
    var averageStrengthOfTibialisAnteriormuscleSum = 0
    var averageStrengthOfPeroneusLongusSum = 0

    val timeCounterLiveData = MutableLiveData("0m0s")

    val frameHead = byteArrayOf(0xAB.toByte(), 0xCD.toByte())

    private var timerValue = 0
        set(value) {
            field = value
//            "TIMER_VALUE".logE("$field")
            if (field > 0) {
                actuallyTrainingDurationSum = field
            }
            timeCounterLiveData.postValue(field.format2Date())
        }

    fun Int.format2Date() = if (this < 0) "0m0s" else "${this/60}m${this%60}s"
    private var timerJob: Job? = null

    var isTimerStart = false
        set(value) {
            field = value
            if (field) {
                timerValue = 0
                resetData()
                timerJob = GlobalScope.launch(Dispatchers.IO) {
                    while (true) {
                        delay(1000)
                        ++timerValue
                    }
                }
            } else {
                timerJob?.cancel("停止计时")
            }
        }
    
    fun init() {
        BluetoothServer.frameHead = frameHead
        BluetoothServer.parseBlock = {
//            LOG_TAG.logE("接收数据:${it.formatHexString(" ")}")
            writeLog("${it.formatHexString(" ")}")
            if (it != null && it.size == 19) {
                FrameData(data = it.copyOfRange(2, 19))
            } else {
                writeLog("数据异常:size:${it?.size.orDefault()}.content:${it.formatHexString(" ")}", LOG_ERROR)
                null
            }
        }
        BluetoothServer.verifyBlock = {
            val code = if (it == null || it.size != 19) 0 else {
                it.copyOfRange(0, 18).sum()
            }
            code.toUByte() == it!!.last().toUByte()
        }
        BluetoothServer.onReceiveBytesData().subscribe({ frameData ->
            if (frameData != null) {
                val result = frameData.data!!.parseData()
//                resultLiveData.postValue(result)
                appendData(result)
                resultSubject.onNext(result)
            }
        }, {
            "Exception".logE(it.message?:"unknown exception")
        })
    }

    fun ByteArray.getVerifyCode(dataSize: Int = 19): Int = if (this == null || this.size != dataSize) 0 else {
        this.copyOfRange(0, size-1).sum()
    }

    fun appendData(dataInfo: DataInfo) {
        targetTrainingDuration = dataInfo.targetTrainingDuration
        targetTrainingNum = dataInfo.targetTrainingNum
        actuallyTrainingNum = dataInfo.currentTrainingNum
        effectiveTrainingNum = dataInfo.completeTrainingNum
        settingJointActiveRange = dataInfo.targetJointAngle
        actuallyJointActiveRange = if (dataInfo.jointAngle > actuallyJointActiveRange) dataInfo.jointAngle else actuallyJointActiveRange
        settingJointAngleVelocity = 40
        actuallyJointAngleVelocitySum += dataInfo.jointAngleVelocity
        averageStrengthOfLateralThighSum += dataInfo.lateralFemoralMuscleContractionStrength
        averageStrengthOfMedialFemorisSum += dataInfo.medialFemoralMuscleContractionStrength
        averageStrengthOfBicepsFemorisSum += dataInfo.bicepsFemoralContractionStrength
        averageStrengthOfSemitendinosusFemorisSum += dataInfo.semitendinosusFemoralContractionStrength
        averageStrengthOfTibialisAnteriormuscleSum += dataInfo.anteriorTibialTendonContractionStrength
        averageStrengthOfPeroneusLongusSum += dataInfo.peronealMuscleContractionStrength
    }

    fun resetData() {
        targetTrainingDuration = 0
        actuallyTrainingDurationSum = 0
        targetTrainingNum = 0
        actuallyTrainingNum = 0
        effectiveTrainingNum = 0
        settingJointActiveRange = 0
        actuallyJointActiveRange = 0
        settingJointAngleVelocity = 0
        actuallyJointAngleVelocitySum = 0
        averageStrengthOfLateralThighSum = 0
        averageStrengthOfMedialFemorisSum = 0
        averageStrengthOfBicepsFemorisSum = 0
        averageStrengthOfSemitendinosusFemorisSum = 0
        averageStrengthOfTibialisAnteriormuscleSum = 0
        averageStrengthOfPeroneusLongusSum = 0
    }
}

data class DataInfo(var targetTrainingNum: Int = 0,
                    var targetJointAngle: Int = 0,
                    var currentTrainingNum: Int,
                    var completeTrainingNum: Int,
                    var jointAngle: Int,
                    var jointAngleVelocity: Int,
                    var lateralFemoralMuscleContractionStrength: Int,
                    var medialFemoralMuscleContractionStrength: Int,
                    var bicepsFemoralContractionStrength: Int,
                    var semitendinosusFemoralContractionStrength: Int,
                    var anteriorTibialTendonContractionStrength: Int,
                    var peronealMuscleContractionStrength: Int,
                    var targetTrainingDuration: Int,
                    var exceptionCode: Int) {
}

fun ByteArray.parseData() = DataInfo(this[0].toUByte().toInt(), this[1].toUByte().toInt(), this[2].toUByte().toInt(), this[3].toUByte().toInt(),
                                    this.copyOfRange(4, 6).read2ShortLE()/10, this.copyOfRange(6, 8).read2ShortLE()/10,
                                    this[8].toUByte().toInt(), this[9].toUByte().toInt(), this[10].toUByte().toInt(), this[11].toUByte().toInt(),
                                    this[12].toUByte().toInt(), this[13].toUByte().toInt(), this[14].toUByte().toInt(), this[15].toUByte().toInt())

data class ParamsInfo(var everydayTrainingDuration: Byte = 0,   //每天训练时长
                      var everydayTrainingGroupNum: Byte = 1,   //每天训练组数
                      var groupTrainingNum: Byte = 10,   //每组训练次数
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
        targetAngleVelocityLowBit = settingJointAngleVelocity.toShort().toBytesLE()[1]
        targetAngleVelocityHighBit = settingJointAngleVelocity.toShort().toBytesLE()[0]
//        lateralFemoralMuscleIfWork = 1
//        medialFemoralMuscleIfWork = 1
//        bicepsFemorisIfWork = 1
//        semitendinosusFemorisIfWork = 1
//        anteriorTibialTendonIfWork = 1
//        peronealMuscleIfWork = 1
    }

    fun getTargetAngleVelocity() = byteArrayOf(targetAngleVelocityLowBit, targetAngleVelocityHighBit).read2IntLE()
    fun toFrameByteArray(): ByteArray {
        val content = byteArrayOf(everydayTrainingDuration, everydayTrainingGroupNum, groupTrainingNum, targetAngle, targetAngleVelocityLowBit, targetAngleVelocityHighBit,
        lateralFemoralMuscleIfWork, medialFemoralMuscleIfWork, bicepsFemorisIfWork, semitendinosusFemorisIfWork, anteriorTibialTendonIfWork, peronealMuscleIfWork)
        val data = byteArrayOf(*frameHead, *content)
        return byteArrayOf(*data, data.getVerifyCode(data.size).toByte())
    }
}