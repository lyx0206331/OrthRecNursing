package com.chwishay.orthrecnursing

import android.content.Context
import android.content.res.AssetManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
 * date:2021/12/25 0025 17:38
 * description:
 */
object AudioTrackPlayer {

    private var isStarted = false
    private var audioTrack: AudioTrack? = null

    fun startPlayer(streamType: Int = AudioManager.STREAM_MUSIC, sampleRateInHz: Int = 44100,
                    channelConfig:Int = AudioFormat.CHANNEL_OUT_STEREO, audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT): Boolean {
        if (isStarted) {
            "AudioPlayer".logE("Player already started!")
            return false
        }
        var bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        if (bufferSizeInBytes == AudioTrack.ERROR_BAD_VALUE) {
            "AudioPlayer".logE("Invalid parameters!")
            return false
        }

        audioTrack = AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, AudioTrack.MODE_STREAM)
        if (audioTrack?.state == AudioTrack.STATE_UNINITIALIZED) {
            "AudioPlayer".logE("AudioTrack initialize!")
            return false
        }

        isStarted = true

        "AudioPlayer".logE("start audio player success!")
        return true
    }

    fun stopPlayer() {
        if (!isStarted) return;
        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack?.stop()
        }
        audioTrack?.release()
        isStarted = false

        "AudioPlayer".logE("Stop audio player success!")
    }

    fun play(audioData: ByteArray, offsetInBytes: Int = 0, sizeInBytes:Int = audioData.size) : Boolean {
        if (isStarted) {
            "AudioPlayer".logE("Player not started!")
            return false
        }
        val writeSize = audioTrack?.write(audioData, offsetInBytes, sizeInBytes)
        if (writeSize != sizeInBytes) {
            "AudioPlayer".logE("Could not write all the samples to the audio devices!writeSize:$writeSize, sizeInBytes:$sizeInBytes")
        }


        audioTrack?.play()

        "AudioPlayer".logE("Ok, Played $sizeInBytes bytes!")
        return true
    }

    fun playFromAssetsFile(context: Context, fileName: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val inputStream = context.resources.assets.open(fileName)
//            val size = inputStream.available()
//            val bytes = ByteArray(size)
            val bytes = inputStream.readBytes()
            inputStream.close()
            play(bytes)
            startPlayer()
            stopPlayer()
        }
    }
}