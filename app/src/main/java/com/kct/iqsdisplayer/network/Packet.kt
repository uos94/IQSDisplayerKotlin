package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.saveFile
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * TCP 통신 중 순번발행기로 부터 받은 Byte 데이터를 String, int, short로 변환하는 클래스
 */
class Packet(headerBytes: ByteArray, dataBytes: ByteArray) {
    private var protocolId: Int
    private var length: Int
    private var data: ByteBuffer

    init {
        // header (ByteBuffer 활용)
        val headerBuffer = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
        length      = headerBuffer.short.toInt() and 0xFFFF
        protocolId  = headerBuffer.short.toInt() and 0xFFFF

        // data (ByteBuffer.wrap() 직접 사용)
        data = ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN)
    }

    fun getId(): Short      = protocolId.toShort()
    fun getLength(): Short  = length.toShort()
    fun getData(): ByteBuffer  = data
    val byte: Byte get() = data.get()

    val string: String get() {
        val byteArrayOutputStream = ByteArrayOutputStream()
        var temp: Byte

        while (data.hasRemaining()) {
            temp = data.get()
            if (temp.toInt() == 0x00) break
            byteArrayOutputStream.write(temp.toInt())
        }

        return String(byteArrayOutputStream.toByteArray(), Charset.forName("euc-kr")).trim()
    }

    val integer: Int get() {
        if (data.remaining() >= 4) {
            return data.int
        } else {
            throw IllegalStateException("Not enough data to read an integer")
        }
    }

    fun getFileAndSave(fileName: String) {
        try {
            val fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).lowercase()
            val dataArray = ByteArray(data.remaining()) { data.get() }

            when (fileExtension) {
                "apk" -> saveFile(fileName, Const.Path.DIR_PATCH, dataArray)
                "wav" -> saveFile(fileName, Const.Path.DIR_DOWNLOAD_SOUND, dataArray)
                "jpg", "jpeg", "png", "mp4" -> saveFile(fileName, Const.Path.DIR_DOWNLOAD_VIDEO, dataArray)
                else -> Log.e("Unsupported file extension: $fileExtension")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // =============================================================================================
// 2022.08.03 bhyang FTP->TCP
// ---------------------------------------------------------------------------------------------
// 1) getFileAndSave(String fileName, int nLength)
//    - 입력 파라미터 파일의 확장자가 *.APK 이거나 *.WAV를 구분하여 처리 방법을 구분한다.
// =============================================================================================
    fun getFileAndSave(fileName: String, nLength: Int) {
        try {
            val fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).lowercase()
            val data = ByteArray(nLength) { data.get() }

            when (fileExtension) {
                "apk" -> saveFile(fileName, Const.Path.DIR_PATCH, data)
                "wav" -> saveFile(fileName, Const.Path.DIR_DOWNLOAD_SOUND, data)
                "jpg", "jpeg", "png", "mp4" -> saveFile(fileName, Const.Path.DIR_DOWNLOAD_VIDEO, data)
                else -> Log.e("Unsupported file extension: $fileExtension")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}