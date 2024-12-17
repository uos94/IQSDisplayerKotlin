package com.kct.iqsdisplayer.data.packet.send

import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.data.packet.BaseSendPacket
import com.kct.iqsdisplayer.network.ProtocolDefine
import java.io.File

data class MediaListRequest(
    val code: Short = ProtocolDefine.MEDIA_LIST_REQUEST.value
) : BaseSendPacket(code) {

    private val videoListValue: String by lazy { generateVideoList() }

    override fun getDataArray(): Array<Any> {
        return arrayOf(videoListValue)
    }

    private fun generateVideoList(): String {
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
                return ""
            }
        } catch (ex: Exception) {
            return ""
        }

        return filesToStringData.toString()
    }

    override fun toString(): String {
        return "MediaListRequest(videoListValue='$videoListValue')"
    }


}



