package com.chwishay.orthrecnursing

import android.content.Context
import android.os.Build
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

//  ┏┓　　　┏┓
//┏┛┻━━━┛┻┓
//┃　　　　　　　┃
//┃　　　━　　　┃
//┃　┳┛　┗┳　┃
//┃　　　　　　　┃
//┃　　　┻　　　┃
//┃　　　　　　　┃
//┗━┓　　　┏━┛
//   ┃　　　┃   神兽保佑
//   ┃　　　┃   代码无BUG！
//   ┃　　　┗━━━┓
//   ┃　　　　　　　┣┓
//   ┃　　　　　　　┏┛
//   ┗┓┓┏━┳┓┏┛
//     ┃┫┫　┃┫┫
//     ┗┻┛　┗┻┛
/**
 * Author:RanQing
 * Create time:20-8-4 下午1:46
 * Description:Kotlin常用扩展函数
 **/

object DateFormatStr {
    const val FORMAT_YMDHMS_CN = "yyyy年MM月dd日 HH时mm分ss秒"
    const val FORMAT_YMDHMS = "yyyy/MM/dd HH:mm:ss"
    const val FORMAT_YMD_CN = "yyyy年MM月dd日"
    const val FORMAT_YMD = "yyyy/MM/dd"
    const val FORMAT_HMS_CN = "HH时mm分ss秒"
    const val FORMAT_HMS = "HH:mm:ss"
    const val FORMAT_YM_CN = "yyyy年MM月"
    const val FORMAT_YM = "yyyy/MM"
    const val FORMAT_MD_CN = "MM月dd日"
    const val FORMAT_MD = "MM/dd"
    const val FORMAT_HM_CN = "HH时mm分"
    const val FORMAT_HM = "HH:mm"
    const val FORMAT_MS_CN = "mm分ss秒"
    const val FORMAT_MS = "mm:ss"

    const val FORMAT_HMS_SSS = "HH:mm:ss.SSS"
}

fun Short?.orDefault(default: Short = 0): Short = this ?: default

fun Int?.orDefault(default: Int = 0): Int = this ?: default

fun Long?.orDefault(default: Long = 0L): Long = this ?: default

fun Float?.orDefault(default: Float = .0f): Float = this ?: default

fun Double?.orDefault(default: Double = .0): Double = this ?: default

fun Boolean?.orDefault(default: Boolean = false): Boolean = this ?: default

fun String?.orDefault(default: String = ""): String = this ?: default

fun String.logE(msg: String, tr: Throwable? = null, isForce: Boolean = false) =
    if (BuildConfig.DEBUG || isForce) Log.e(this, msg, tr) else 0

fun String.logV(msg: String, tr: Throwable? = null, isForce: Boolean = false) =
    if (BuildConfig.DEBUG || isForce) Log.v(this, msg, tr) else 0

fun String.logD(msg: String, tr: Throwable? = null, isForce: Boolean = false) =
    if (BuildConfig.DEBUG || isForce) Log.d(this, msg, tr) else 0

fun String.logW(msg: String, tr: Throwable? = null, isForce: Boolean = false) =
    if (BuildConfig.DEBUG || isForce) Log.w(this, msg, tr) else 0

fun String.logI(msg: String, tr: Throwable? = null, isForce: Boolean = false) =
    if (BuildConfig.DEBUG || isForce) Log.i(this, msg, tr) else 0

fun Context.showShortToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context.showLongToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

fun Context.showShortToast(@StringRes resId: Int) =
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

fun Context.showLongToast(@StringRes resId: Int) =
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()

/**
 * 格式化日期字符串
 */
fun Long.formatDateString(
    formatStr: String = DateFormatStr.FORMAT_YMD_CN,
    locale: Locale = Locale.CHINESE
): String = SimpleDateFormat(formatStr, locale).format(Date(this))

fun Date.formatDateString(
    formatStr: String = DateFormatStr.FORMAT_YMD_CN,
    locale: Locale = Locale.CHINESE
): String = SimpleDateFormat(formatStr, locale).format(this)

/**
 * 大端模式下，字节数组转Short
 */
fun ByteArray.toShortBE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(
        ByteOrder.BIG_ENDIAN
    ).short

/**
 * 小端模式下，字节数组转Short
 */
fun ByteArray.toShortLE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(
        ByteOrder.LITTLE_ENDIAN
    ).short

/**
 * 大端模式下，Short转字节数组
 */
fun Short.toByteArrayBE(capacity: Int = 2) =
    ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN).putShort(this).array()

/**
 * 小端模式下，Short转字节数组
 */
fun Short.toByteArrayLE(capacity: Int = 2) =
    ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN).putShort(this).array()

/**
 * 大端模式下，字节数组转Int
 */
fun ByteArray.toIntBE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(
        ByteOrder.BIG_ENDIAN
    ).int

/**
 * 小端模式下，字节数组转Int
 */
fun ByteArray.toIntLE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(
        ByteOrder.LITTLE_ENDIAN
    ).int

/**
 * 大端模式下，Int转字节数组
 */
fun Int.toByteArrayBE(capacity: Int = 4): ByteArray =
    ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN).putInt(this).array()

/**
 * 小端模式下，Int转字节数组
 */
fun Int.toByteArrayLE(capacity: Int = 4): ByteArray =
    ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN).putInt(this).array()

/**
 * 大端模式下，字节数组转Float
 */
fun ByteArray.toFloatBE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(ByteOrder.BIG_ENDIAN).float

/**
 * 小端模式下，字节数组转Float
 */
fun ByteArray.toFloatLE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(ByteOrder.LITTLE_ENDIAN).float

/**
 * 大端模式下，Float转字节数组
 */
fun Float.toByteArrayBE(capacity: Int = 4): ByteArray =
    ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN).putFloat(this).array()

/**
 * 小端模式下，Float转字节数组
 */
fun Float.toByteArrayLE(capacity: Int = 4): ByteArray =
    ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN).putFloat(this).array()

/**
 * 大端模式下，字节数组转Long
 */
fun ByteArray.toLongBE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(ByteOrder.BIG_ENDIAN).long

/**
 * 小端模式下，字节数组转Long
 */
fun ByteArray.toLongLE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(
        ByteOrder.LITTLE_ENDIAN
    ).long

/**
 * 大端模式下，Long转字节数组
 */
fun Long.toByteArrayBE(capacity: Int = 8): ByteArray =
    ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN).putLong(this).array()

/**
 * 小端模式下，Long转字节数组
 */
fun Long.toByteArrayLE(capacity: Int = 8): ByteArray =
    ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN).putLong(this).array()

/**
 * 大端模式下，字节数组转Double
 */
fun ByteArray.toDoubleBE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(ByteOrder.BIG_ENDIAN).double

/**
 * 小端模式下，字节数组转Double
 */
fun ByteArray.toDoubleLE(offset: Int = 0, length: Int = this.size) =
    ByteBuffer.wrap(this, offset, length).order(ByteOrder.LITTLE_ENDIAN).double

/**
 * 大端模式下，Double转字节数组
 */
fun Double.toByteArrayBE(capacity: Int = 8): ByteArray =
    ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN).putDouble(this).array()

/**
 * 小端模式下，Double转字节数组
 */
fun Double.toByteArrayLE(capacity: Int = 8): ByteArray =
    ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN).putDouble(this).array()

/**
 * 字节数组转换为字符串
 * @param seperator 间隔符
 */
fun ByteArray?.formatHexString(seperator: String = ""): String? =
    if (this == null || this.isEmpty()) {
        null
    } else {
        val sb = StringBuilder()
        this.forEach { item ->
            var hex = Integer.toHexString(item.toInt().and(0xff))
            if (hex.length == 1) hex = "0$hex"
            sb.append("$hex$seperator")
        }
        sb.toString().trim()
    }

/**
 * 十六进制字符串转字节数组
 */
fun String?.hexString2Bytes(): ByteArray? =
    if (this.isNullOrEmpty() || !this.isHexString()) null
    else {
        val src = this.trim().uppercase(Locale.getDefault())
        val len = src.length / 2
        val result = ByteArray(len) { 0 }
        val hexChars = src.toCharArray()
        for (i in 0 until len) {
            val pos = i * 2
            result[i] = (hexChars[pos].char2Byte().toInt().shl(4)
                .or(hexChars[pos + 1].char2Byte().toInt())).toByte()
        }
        result
    }

/**
 * 字符转字节
 */
fun Char.char2Byte() = "0123456789ABCDEF".indexOf(this).toByte()

/**
 * 字条串转换为二进制字符串，以空格相隔
 */
fun String?.str2BinStr(): String? =
    if (this.isNullOrEmpty()) null
    else {
        val strChar = this.toCharArray()
        var result = ""
        strChar.forEach {
            result += "${Integer.toBinaryString(it.code)} "
        }
        result.trim()
    }

/**
 * 判断是否十六进制字符串
 */
fun String?.isHexString() =
    if (this.isNullOrEmpty()) false
    else {
        Pattern.compile("^[0-9A-Fa-f]+$").matcher(this).matches()
    }

/**
 * 判断是否邮箱地址
 */
fun String?.isEmailString() =
    if (this.isNullOrEmpty()) false
    else {
        Pattern.compile("^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]\$")
            .matcher(this).matches()
    }

/**
 * 判断是否Wi-Fi SSID
 */
fun String?.isSsidString() =
    if (this.isNullOrEmpty()) false
    else {
        Pattern.compile("^[A-Za-z]+[\\w\\-\\:\\.]*\$").matcher(this).matches()
    }

val CHINESE_UNICODE = "[\\u4e00-\\u9fa5]"

/**
 * 是否包含汉字
 */
fun String?.containtChinese() = Pattern.compile(CHINESE_UNICODE).matcher(this).find()

/**
 * 是否全是汉字
 */
fun String?.isAllChinese() = Pattern.compile(CHINESE_UNICODE).matcher(this).matches()

/**
 * 是否只包含字母数字及汉字
 */
fun String.isNumOrCharOrChinese() = this.matches(Regex("^[a-z0-9A-Z\\u4e00-\\u9fa5]+$"))

fun File.checkFileExists(): File {
    val dir = File(this.parent)
    if (!dir.exists()) {
        dir.mkdirs()
    }
    val file = File(dir, "${path.substring(path.lastIndexOf("/") + 1, path.length)}")
    if (!file.exists()) {
        file.createNewFile()
    }
    return file
}

fun File.createFile(delOld: Boolean = false): File =
    if (!exists() || !isFile) {
        File(this.parent).also {
            it.mkdirs()
        }
        this.createNewFile()
        this
    } else if (delOld) {
        delete()
//        val f = File(path)
        this.createNewFile()
        this
    } else {
        this
    }

/**
 * 根据时间范围获取文件
 */
fun File.getFilesByTimeRange(from: Long = 0, to: Long = System.currentTimeMillis()): Array<File>? = if (exists()) {
//    "time".logE("from:${from.formatDateString()}, to:${to.formatDateString()}")
    listFiles { pathname -> pathname?.lastModified() in from..to }
} else {
    null
}

/**
 * 是否有可用剩余空间
 */
@RequiresApi(Build.VERSION_CODES.GINGERBREAD)
fun File.hasAvailableSpace(cacheByteCount: Long = 50*1024*1024L): Boolean = usableSpace > cacheByteCount

//写入文件类型为字符串
const val TYPE_WRITE_STRING = 0
//写入文件类型为字节
const val TYPE_WRITE_BYTE = 1
@Retention(AnnotationRetention.SOURCE)
@IntDef(TYPE_WRITE_STRING, TYPE_WRITE_BYTE)
annotation class TypeWrite2File

@Synchronized
fun File.write(data: ByteArray, @TypeWrite2File type: Int = TYPE_WRITE_STRING) {
    when(type) {
        TYPE_WRITE_STRING -> {
            val d = data.formatHexString(" ")
//            "LOG".logE("size:${data.size},format:${d}")
            this.createFile().appendText("$d\n")
        }
        TYPE_WRITE_BYTE -> {
            this.createFile().appendBytes(data)
        }
    }
}

/**
 * 获取字节的低半字节数据
 *
 * @return
 */
internal fun Byte.getLowHalfByte(): Int = this.toInt().and(0x0000000F)

/**
 * 获取字节的高半字节数据
 *
 * @return
 */
internal fun Byte.getHighHalfByte(): Int = this.toInt().and(0x000000F0).shr(4)

/**
 * 获取字节最后一位
 *
 * @return
 */
internal fun Byte.getLastBit(): Int = this.toInt().and(0x00000001)

/**
 * 判断是否MAC地址字符串
 */
fun String?.isMacString() =
    if (this.isNullOrEmpty()) false
    else {
        Pattern.compile("[0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5}").matcher(this).matches()
    }

inline fun Context.getColor1(@ColorRes colorId: Int) = this.resources.getColor(colorId)

fun String.toEditable() = Editable.Factory.getInstance().newEditable(this)