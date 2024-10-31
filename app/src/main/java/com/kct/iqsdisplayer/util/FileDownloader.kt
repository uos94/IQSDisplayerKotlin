package com.kct.iqsdisplayer.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.Channels

@Deprecated("이 프로젝트에서는 사용 할 일이 없을 것으로 보임.")
class FileDownloader {

    suspend fun downloadFile(url: String, destinationPath: String, progressCallback: (Float) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            val contentLength = connection.contentLength

            val channel = Channels.newChannel(connection.getInputStream())
            val outputFile = File(destinationPath)
            val outputStream = outputFile.outputStream()
            val fileChannel = outputStream.channel

            var bytesRead = 0L
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read: Int

            while (channel.read(ByteBuffer.wrap(buffer)).also { read = it } != -1) {
                fileChannel.write(ByteBuffer.wrap(buffer, 0, read))
                bytesRead += read
                val progress = bytesRead.toFloat() / contentLength
                withContext(Dispatchers.Main) {
                    progressCallback(progress)
                }
            }

            outputStream.close()
            channel.close()

            withContext(Dispatchers.Main) {
                progressCallback(1f)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                throw e
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8192
    }
}