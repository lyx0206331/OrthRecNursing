package com.chwishay.orthrecnursing

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<TextView>(R.id.tvVersion).text = "v${BuildConfig.VERSION_NAME}"

        lifecycleScope.launch {
            delay(2000)
            requestPermission()
        }
    }

    private fun requestPermission() {
        requestPermission {
            lifecycleScope.launch {
                startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun requestPermission(action: () -> Unit) {
        RxPermissions(this)
            .requestEachCombined(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
//                Manifest.permission.BLUETOOTH_SCAN,
//                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ).subscribe { permission ->
                if (permission.granted) {
                    action.invoke()
                    return@subscribe
                }
                if (permission.name.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE, ignoreCase = true) || permission.name.equals(
                        Manifest.permission.READ_EXTERNAL_STORAGE, ignoreCase = true)) {
                    if (permission.shouldShowRequestPermissionRationale) {
                        Toast.makeText(
                            this,
                            "你拒绝授予本应用存储权限，本应用将无法正常使用，请前往设置页面授予相关权限。 ",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "请你授予本应用存储权限，如果你拒绝授权，应用将无法正常使用。",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (permission.name.equals(Manifest.permission.READ_PHONE_STATE, ignoreCase = true)) {
                    if (permission.shouldShowRequestPermissionRationale) {
                        Toast.makeText(
                            this,
                            "你拒绝授予本应用获取手机状态的权限，本应用将无法正常使用，请前往设置页面授予相关权限。 ",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "请你授予本应用获取手机状态的权限，如果你拒绝授权，应用将无法正常使用。 ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }/* else if (permission.name.equals(Manifest.permission.BLUETOOTH_SCAN, ignoreCase = true) ||
                    permission.name.equals(Manifest.permission.BLUETOOTH_CONNECT, ignoreCase = true)) {
                    if (permission.shouldShowRequestPermissionRationale) {
                        Toast.makeText(
                            this,
                            "你拒绝授予本应用蓝牙扫描及连接权限，本应用将无法正常使用，请前往设置页面授予相关权限。 ",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "请你授予本应用蓝牙扫描及连接权限，如果你拒绝授权，应用将无法正常使用。 ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } */else {
                    if (permission.shouldShowRequestPermissionRationale) {
                        Toast.makeText(
                            this,
                            "你拒绝授予本应用定位权限，本应用将无法正常使用，请前往设置页面授予相关权限。 ",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "请你授予本应用定位权限，如果你拒绝授权，应用将无法正常使用。 ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.addTo(defaultCompositeDisposable)
    }

    var defaultCompositeDisposable = CompositeDisposable()
    override fun onDestroy() {
        defaultCompositeDisposable.clear()
        super.onDestroy()
    }
}