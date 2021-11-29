package com.chwishay.orthrecnursing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

fun Context.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    val view = Toast.makeText(this, "", Toast.LENGTH_SHORT).view
    val sToast = Toast(this)
    sToast.view = view
    sToast.setText(msg)
    sToast.duration = duration
    sToast.show()
}

fun Context.toast(@StringRes msgId: Int) = this.toast(getString(msgId))

@SuppressLint("HardwareIds")
fun Context.getClientId(): String {
    var uniqueId = "default"
    try {
        val androidID = Settings.System.getString(contentResolver, Settings.System.ANDROID_ID)
        uniqueId = androidID + Build.SERIAL
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return uniqueId
}

fun getClientId(): String {
    return try {
        OrthRecApp.instance.getClientId()
    } catch (e: java.lang.Exception) {
        "123"
    }
}

/**
 * 当前系统语言。zh:中文;en:英文
 */
fun Context.getCurrentLanguage(): String = this.resources.configuration.locale.language

/**
 * 当前国家.CN:中国;US:美国
 */
fun Context.getCurrentCountry(): String = this.resources.configuration.locale.country

fun Context.isChinese(): Boolean = this.getCurrentLanguage() == Locale.CHINESE.language

fun Context.isEnglish(): Boolean = this.getCurrentLanguage() == Locale.US.language

fun showShortToast(msg: String) {
    OrthRecApp.instance.showShortToast(msg)
}

fun showShortToast(msgId: Int) {
    showShortToast(OrthRecApp.instance.getString(msgId))
}

fun showShortToast4Debug(msg: String) {
    if (BuildConfig.DEBUG) {
        GlobalScope.launch(Dispatchers.Main) {
            showShortToast(msg)
        }
    }
}

fun Context.checkConnectState(block:() -> Unit) {
    if (!BluetoothServer.isEnable()) {
        BluetoothServer.openBluetoothAsyn(this as Activity)
    } else if (BluetoothServer.btConnState != BluetoothServer.STATE_BT_CONNECT_SUCCESS && this is AppCompatActivity) {
        BtDevListDialog(this, this).show()
    } else {
        block
    }
}