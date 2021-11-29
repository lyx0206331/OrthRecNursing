package com.chwishay.orthrecnursing

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

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
 * date:2021/3/19 0019 17:01
 * description:View扩展方法
 */

internal infix fun SwitchCompat.onCheckedChanged(function: (CompoundButton, Boolean) -> Unit) {
    setOnCheckedChangeListener(function)
}

internal infix fun View.onClick(function: () -> Unit) {
    setOnClickListener { function() }
}

internal infix fun SeekBar.onProgressChanged(block: (seekBar: SeekBar, progress: Int, fromUser: Boolean) -> Unit) {
    setOnSeekBarChangeListener(object : OnSeekBarProgressChanged() {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            block(seekBar, progress, fromUser)
        }
    })
}

/**
 * 设置TextView是否可滚动
 *
 * @param scrollable
 */
internal infix fun TextView.isScrollable(scrollable: Boolean) {
    movementMethod = if (scrollable) ScrollingMovementMethod.getInstance() else null
}

/**
 * TextView滚动到最后一行
 *
 */
internal fun TextView.scroll2LastLine() {
    val offset = this.lineCount * this.lineHeight
    if (offset > this.height) {
        this.scrollTo(0, offset - height)
    }
}

abstract class OnSeekBarProgressChanged: SeekBar.OnSeekBarChangeListener {
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}

/**
 * 打开系统设置
 *
 */
internal fun Context.openSysSettings() = startActivity(Intent(Settings.ACTION_SETTINGS))