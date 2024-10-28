package com.kct.iqsdisplayer.common

import android.media.MediaPlayer
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.isExistFile
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
            Log.d("soundPath : $soundPath")
            mp.setDataSource(soundPath)
            mp.prepareAsync()
        }
    }

    private fun setVolume() {
        val volume = ScreenInfo.volumeLevel
        val fVolumeRate = 1 - ((ln((10 - volume).toDouble()) / ln(10.0)).toFloat())
        mp.setVolume(fVolumeRate, fVolumeRate) //볼륨 설정
    }

    private fun makePlayList(callNum: Int, callWinNum: Int, isVIP: Boolean = false) {
        val repeatCount = ScreenInfo.callRepeatCount
        val bellFileName = ScreenInfo.bellFileName
        val ment = ScreenInfo.callMent

        val logMessage =
            """
                |호출정보
                |   변수값 : CallNum=$callNum, WinNum=$callWinNum, 반복횟수=$repeatCount, bellFileName=$bellFileName, ment=$ment
            """.trimMargin()

        fun addFileToPlayList(fileName: String) {
            val filePath = Const.Path.DIR_SOUND + fileName
            if (filePath.isExistFile()) listCallSound.add(filePath)
            else Log.w("사운드 파일이 없음 : $filePath")
        }

        repeat(repeatCount) {
            // 1. 띵동 벨소리
            if (bellFileName.isNotEmpty()) {
                addFileToPlayList(bellFileName)
            }

            // 2. 호출 번호
            if (callNum > 999) { // 천의 자리
                addFileToPlayList("P%04d.wav".format((callNum / 1000) * 1000))
            }
            addFileToPlayList("N%04d.wav".format(callNum % 1000))

            // 3. 안내 멘트
            if (isVIP) {
                addFileToPlayList("W0000.wav") // VIP 멘트
            } else {
                // 창구 번호 멘트
                addFileToPlayList("W%04d.wav".format(callWinNum))

                // 안내 멘트 (조건에 따른 파일명 선택)
                val mentFileName = when (ment) {
                    "창구로 오십시오." -> "1025.wav"
                    "창구로 모시겠습니다." -> "1054.wav"
                    "창구에서 도와드리겠습니다." -> "1055.wav"
                    else -> "1025.wav"
                }
                addFileToPlayList(mentFileName)
            }
        }
    }
}