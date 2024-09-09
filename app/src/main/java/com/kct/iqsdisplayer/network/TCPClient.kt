package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.util.Log
import java.io.IOException
import java.io.InputStream
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

class TCPClient(private val host: String, private val port: Int) {

    private lateinit var socket: Socket
    private lateinit var inputStream: InputStream

    init {
        Log.d("Tcp연결: IP:${host}, PORT:${port}")
    }

    fun connect() {
        var exceptionMessage: String? = null
        try {
            socket = Socket(host, port).apply { setSoTimeout(20000) }
            inputStream = socket.getInputStream() // inputStream 초기화
        } catch (e: SocketException) {
            exceptionMessage = "Tcp연결실패 : SocketException (${e.message})"
        } catch (e: SocketTimeoutException) {
            exceptionMessage = "Tcp연결실패 : SocketTimeoutException (${e.message})"
        } catch (e: IOException) {
            exceptionMessage = "Tcp연결실패 : IOException (${e.message})"
        } catch (e: Exception) {
            exceptionMessage = "Tcp연결실패 : Exception (${e.message})"
        } finally {
            exceptionMessage?.let {
                Log.e(it)
            }
        }
    }

    fun disconnect() {
        inputStream.close()
        socket.close()
    }

    fun receiveData(size: Int): ByteArray {
        return inputStream.readNBytes(size)
    }

    fun receiveProtocolId(): Int {
        val headerBytes = receiveData(PacketAnalyzer.HEADER_SIZE)
        if (headerBytes.size != PacketAnalyzer.HEADER_SIZE) {
            throw IOException("Invalid packet header")
        }

        val headerBuffer = ByteBuffer.wrap(headerBytes)
        headerBuffer.short // 데이터 길이는 사용하지 않음
        return headerBuffer.short.toInt() and 0xFFFF // 프로토콜 ID 반환
    }
}