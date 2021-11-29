package com.chwishay.orthrecnursing

import android.graphics.Color
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.annotation.IntDef
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.regex.Pattern

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
 * date:2021/1/7 0007 10:27
 * description:
 */
val logSubject = PublishSubject.create<SpannableString>()
fun observableLog() = logSubject.observeOn(AndroidSchedulers.mainThread())

val processSubject = PublishSubject.create<String>()
fun observableProcess() = processSubject.observeOn(Schedulers.io())

var ifCreateLog = true

const val LOG_NORMAL = 0
const val LOG_ERROR = 1
const val LOG_WARN = 2
const val LOG_BEST = 3

@Retention(AnnotationRetention.SOURCE)
@IntDef(LOG_NORMAL, LOG_ERROR, LOG_WARN, LOG_BEST)
annotation class LogType

fun writeLog(log: String, @LogType type: Int = LOG_NORMAL) {
    if (ifCreateLog) {
        val millis = System.currentTimeMillis()
        val content = "${millis.formatDateString(DateFormatStr.FORMAT_HMS_SSS)}:$log"
        val ss = SpannableString(content).also {
            it.setSpan(
                ForegroundColorSpan(
                    when (type) {
                        LOG_NORMAL -> Color.WHITE
                        LOG_ERROR -> Color.RED
                        LOG_WARN -> Color.YELLOW
                        LOG_BEST -> Color.GREEN
                        else -> Color.WHITE
                    }
                ), 0, content.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
//        "D82_LOG".logE(content)
        logSubject.onNext(ss)
    }
}

fun writeProcess(data: ByteArray) {}

object ExoLogUtil {

    fun parseLogFile(filePath: String): List<LogEntity>? =
        File(filePath).let { file ->
            if (!file.exists() || !file.isFile) {
                null
            } else {
                val list = arrayListOf<LogEntity>()
                file.reader().useLines { lines ->
                    var logEntity: LogEntity? = null
                    var i = 0
                    lines.forEach { line ->
                        if (line == "####%%%%&&&&") {
                            logEntity = LogEntity()
                            i = 0
                            return@forEach
                        }
                        if (!line.trim().isNullOrEmpty()) {
                            i++
                            when (i) {
                                1 -> line.split(Pattern.compile("\\t")).let {
                                    if (it.size == 4) {
                                        logEntity?.index = it[0].toInt()
                                        logEntity?.type = it[1].toInt()
                                        logEntity?.millis = it[2].toLong()
                                        logEntity?.dataLength = it[3].toInt()
                                    }
                                }
                                2 -> line.split(":").let {
                                    if (it.size == 2) {
                                        logEntity?.data?.put(it[0].trim(), hashMapOf())
                                    }
                                }
                                else -> line.split(":").let {
                                    if (it.size == 2) {
                                        logEntity?.data?.let { map ->
                                            map[map.keys.first()]?.put(it[0].trim(), it[1].trim())
                                        }
                                    }
                                }
                            }
                        } else if (i > 0 && logEntity != null) {
                            list.add(logEntity!!)
                            logEntity = null
                        }
                    }
                }
                list
            }
        }
}

data class LogEntity(
    var index: Int = 0,
    var type: Int = 0,
    var millis: Long = 0,
    var dataLength: Int = 0,
    var data: HashMap<String, HashMap<String, String>> = hashMapOf()
)