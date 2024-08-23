package com.kct.iqsdisplayer.common

import android.media.MediaPlayer
import com.kct.iqsdisplayer.util.Log
import java.util.LinkedList
import kotlin.math.floor
import kotlin.math.ln

class CallSoundManager {
    private val mp = MediaPlayer()
    private val listCallSound = LinkedList<String>()

    init {
        setVolume()
        addListener()
    }

    fun play(callNum:Int, callWinNum:Int, flagVIP:Boolean = false) {
        makePlayList(callNum, callWinNum, flagVIP) //재생여부와 상관없이 리스트는 만들어 두고
        if (!mp.isPlaying) {
            // 재생 중이 아닌 경우에는 재생 시작
            playNextSound()
        }
    }

    private fun addListener() {
        mp.setOnCompletionListener { playNextSound() }
        mp.setOnPreparedListener { it.start() }
        mp.setOnErrorListener { _, what, extra ->
            Log.e("CallSoundManager OnError what:$what , extra:$extra")
            playNextSound() //남은거라도 재생해야하는가...
            true }
    }

    private fun playNextSound() {
        if (listCallSound.isNotEmpty()) {
            val soundPath = listCallSound.poll()
            mp.reset()
            mp.setDataSource(soundPath)
            mp.prepareAsync()
            mp.start()
        }
    }

    private fun setVolume() {
        val volume = ScreenInfo.instance.volumeInfo.toIntOrNull() ?: 1
        val fVolumeRate = 1 - ((ln((10 - volume).toDouble()) / ln(10.0)).toFloat())
        mp.setVolume(fVolumeRate, fVolumeRate) //볼륨 설정
    }

    private fun makePlayList(callNum:Int, callWinNum:Int, isVIP:Boolean = false) {

        val repeatCount = ScreenInfo.instance.callInfo.toIntOrNull() ?: 1  //호출 반복횟수
        val bellFileName = ScreenInfo.instance.bellInfo
        val ment = ScreenInfo.instance.ment

        for (i in 0 until repeatCount) {
            //1. 띵동 벨소리
            if(bellFileName.isNotEmpty()) {
                listCallSound.add(Const.Path.DIR_SOUND+bellFileName)
            }

            //2.호출 번호
            if (callNum > 999) //천의자리
            {
                val strTmp = "P%04d.wav".format(floor((callNum / 1000).toDouble()).toInt() * 1000)
                listCallSound.add(Const.Path.DIR_SOUND + strTmp)
            }
            //나머지 자리
            val nTmp = callNum % 1000
            var strTmp = "N%04d.wav".format(nTmp)
            listCallSound.add(Const.Path.DIR_SOUND + strTmp)

            //3.안내 멘트
            if(isVIP) {
                listCallSound.add(Const.Path.DIR_SOUND + "W0000.wav") // VIP실로 오십시오
            }
            else {
                //창구번호 (~번)
                strTmp = "W%04d.wav".format(callWinNum)
                listCallSound.add(Const.Path.DIR_SOUND + strTmp)

                //안내멘트 (0:창구로 오십시오 / 1:창구로 모시겠습니다 2:창구에서 도와드리겠습니다.)
                when (ment) {
                    "창구로 오십시오." -> listCallSound.add(Const.Path.DIR_SOUND + "1025.wav")
                    "창구로 모시겠습니다." -> listCallSound.add(Const.Path.DIR_SOUND + "1054.wav")
                    "창구에서 도와드리겠습니다." -> listCallSound.add(Const.Path.DIR_SOUND + "1055.wav")
                }
            }
        }
    }

    fun playVolumeTest() {

        val repeatCount = ScreenInfo.instance.callInfo.toIntOrNull() ?: 1  //호출 반복횟수
        val bellFileName = ScreenInfo.instance.bellInfo

        for (i in 0 until repeatCount) {
            //1. 띵동 벨소리
            if(bellFileName.isNotEmpty()) {
                listCallSound.add(Const.Path.DIR_SOUND+bellFileName)
            }

            //2.창구 번호
            val strTmp = "W%04d.wav".format(ScreenInfo.instance.winNum)
            listCallSound.add(Const.Path.DIR_SOUND + strTmp)

            //3.안내 멘트 (0:창구로 오십시오 / 1:창구로 모시겠습니다 2:창구에서 도와드리겠습니다.)
            when (ScreenInfo.instance.testVolume.infoSound) {
                0 -> listCallSound.add(Const.Path.DIR_SOUND + "1025.wav")
                1 -> listCallSound.add(Const.Path.DIR_SOUND + "1054.wav")
                2 -> listCallSound.add(Const.Path.DIR_SOUND + "1055.wav")
            }
        }

        val volumeSize = ScreenInfo.instance.testVolume.volumeSize
        val fVolumeRate = 1 - ((ln((10 - volumeSize).toDouble()) / ln(10.0)).toFloat())
        mp.setVolume(fVolumeRate, fVolumeRate) //볼륨 설정
        playNextSound()
    }
}