package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.saveFile
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * TCP 통신 중 순번발행기로 부터 받은 Byte 데이터를 String, int, short로 변환하는 클래스
 */
class Packet(buffer: ByteArray) {
    private var id: Int
    private var length: Int
    private var data: ByteBuffer

    init {
        // header
        // length(2) + id(2)
        length = bytesToInt(buffer[1], buffer[0])
        id = bytesToInt(buffer[3], buffer[2])

        // data
        data = if (length > 0) {
            // ByteBuffer를 초기화
            ByteBuffer.allocate(buffer.size - 4).apply {
                // byte[] 의 4번째부터 buffer의 마지막 까지 -> 4번째 까지는 데이터부 길이, 프로토콜 아이디가 존재
                put(buffer, 4, buffer.size - 4)
                rewind()    // Position을 맨 앞으로 돌아감
            }
        } else {
            ByteBuffer.allocate(0)
        }
    }

    private fun bytesToInt(b1: Byte, b0: Byte): Int {
        return ((b1.toInt() and 0xff) shl 8) or (b0.toInt() and 0xff)
    }

    fun getId(): Short {
        return id.toShort()
    }

    fun getLength(): Short {
        return length.toShort()
    }

    val byte: Byte
        get() {
            val value = data.get()
            return value
        }

    val string: String
        get() {
            val byteArrayOutputStream = ByteArrayOutputStream()
            var temp: Byte

            while (data.hasRemaining()) {
                temp = data.get()
                if (temp.toInt() == 0x00) break // 0x00을 만나면 종료
                byteArrayOutputStream.write(temp.toInt())
            }

            val result = String(byteArrayOutputStream.toByteArray(), charset("euc-kr")).trim()
            Log.d("getString : $result")
            return result
        }
    val integer: Int
        get() {
            if (data.remaining() >= 4) {
                val result = data.int
                Log.d("getInteger : $result")
                return result
            } else {
                throw IllegalStateException("Not enough data to read an integer")
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

    // 이걸로 대체가 가능할 것 같다.
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
}