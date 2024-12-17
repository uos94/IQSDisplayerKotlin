package com.kct.iqsdisplayer.data.packet.send

import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.data.packet.BaseSendPacket
import com.kct.iqsdisplayer.network.ProtocolDefine
import java.io.File

data class MediaListRequest(
    val code: Short = ProtocolDefine.MEDIA_LIST_REQUEST.value
) : BaseSendPacket(code) {

    private val videoList: String by lazy { getVideoList() }

    private fun getVideoList(): String {
        val videoPath = Const.Path.DIR_VIDEO
        val videoFolder = File(videoPath)
        val filesToStringData = StringBuilder()

        try {
            val files = videoFolder.listFiles()
            if (files != null) {
                files.forEach { videoFile ->
                    val fileName = videoFile.name
                    val fileSize = videoFile.length().toString()
                    filesToStringData.append("$fileName;$fileSize#")
                }
            } else {
                // 디렉터리가 아니거나 경로가 존재하지 않음
                return ""
            }
        } catch (ex: Exception) {
            // 기타 예외 처리
            return ""
        }

        return filesToStringData.toString()
    }

    override fun getDataArray(): Array<Any> {
        return arrayOf(videoList)
    }

}



