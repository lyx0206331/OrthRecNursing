package com.chwishay.orthrecnursing

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import androidx.annotation.RawRes
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
 * Author:RanQing
 * Date:2021/12/28 6:24 下午
 * Description:
 */
class SoundPlayer(val context: Context) {

    /**
     * 音频Raw文件列表
     */
    var soundRawList = arrayListOf<@androidx.annotation.RawRes Int>()
        set(value) {
            field = value
            soundIds.clear()
            field.forEach {
                soundIds.add(soundPool.load(context, it, 0))
            }
        }

    /**
     * 音频ID列表
     */
    private var soundIds = arrayListOf<Int>()

    /**
     * 当前播放流ID
     */
    private var streamId = 0

    /**
     * 是否正在播放
     */
    var isPlaying = false

    /**
     * 当前播放索引
     */
    var curIndex = 0
    private val soundPool by lazy {
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder().build()
        } else {
            SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        }
    }

    private var executor = Executors.newScheduledThreadPool(1)

    init {
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
//            "SOUNDPOOL".logE("soundPool:$soundPool, sampleId:$sampleId, status:$status")
            play(curIndex)
        }
    }

    private fun getSoundDuration(@RawRes rawResId: Int) =
        MediaPlayer.create(context, rawResId).duration

    @Synchronized
    fun play(index: Int) {
        if (isPlaying) return
        curIndex = index
        if (curIndex >= soundRawList.size) {
            curIndex = soundRawList.size - 1
        }
        streamId = soundPool.play(soundIds[curIndex], 1f, 1f, 0, 0, 1f)
        val duration = getSoundDuration(soundRawList[curIndex]).toLong()
//        "SOUNDPOOL".logE("index:$curIndex, duration:${duration}")
        if (streamId != 0) {
            isPlaying = true
            executor.schedule({ isPlaying = false }, duration, TimeUnit.MILLISECONDS)
        }
    }

    fun stop() {
        soundPool.stop(streamId)
        isPlaying = false
    }

    fun pause() {
        soundPool.pause(streamId)
        isPlaying = false
    }

    fun release() {
        soundPool.release()
        isPlaying = false
        executor.shutdown()
        executor = null
    }
}