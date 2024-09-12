package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.data.packet.send.KeepAliveRequest
import com.kct.iqsdisplayer.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

class TCPClient(private val host: String, private val port: Int) {

    interface OnTcpEventListener {
        fun onConnected()
        fun onReceivedData(protocolDefine: ProtocolDefine, receivedData: BaseReceivePacket)
        fun onDisconnected()
    }

    private var socket: Socket? = null

    private var inputStream: InputStream? = null
    private var timerKeepAlive = 0
    private var isConnected = false

    private val supervisorJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + supervisorJob)
    private lateinit var keepAliveJob: Job
    private lateinit var receiverJob: Job

    private var listener: OnTcpEventListener? = null

    init {
        Log.d("Tcp 연결 시도: IP:${host}, PORT:${port}")
        reconnectAndStartJobs()
    }

    private fun reconnectAndStartJobs() {
        coroutineScope.launch {
            var retryCount = 0
            val retryDelay = 1000L

            while (isActive && !isConnected) {
                if (connect()) {
                    isConnected = true
                    startKeepAlive()
                    startTcpReceiver()
                    break
                } else {
                    retryCount++
                    delay(retryDelay)
                }
            }
        }
    }

    private fun connect(): Boolean {
        var exceptionMessage: String? = null
        try {
            if (socket == null || socket?.isClosed == true) {
                socket = Socket(host, port).apply { soTimeout = 20000 }
            }
            inputStream = socket?.getInputStream()
            Log.d("Tcp 연결 성공: IP:${host}, PORT:${port}")
            return true
        } catch (e: SocketException) {
            exceptionMessage = "Tcp 연결 실패: SocketException (${e.message})"
        } catch (e: SocketTimeoutException) {
            exceptionMessage = "Tcp 연결 실패: SocketTimeoutException (${e.message})"
        } catch (e: IOException) {
            exceptionMessage = "Tcp 연결 실패: IOException (${e.message})"
        } catch (e: Exception) {
            exceptionMessage = "Tcp 연결 실패: Exception (${e.message})"
        } finally {
            exceptionMessage?.let { Log.e(it) }
        }
        return false
    }

    private fun disconnect() {
        // 기존 코루틴 작업 취소
        keepAliveJob.cancel()
        receiverJob.cancel()

        try {
            inputStream?.close()
            socket?.close()
            socket = null
            inputStream = null
            isConnected = false
            Log.d("Tcp 연결 종료")
            listener?.onDisconnected() // 연결 끊김 알림
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Tcp 연결 종료 실패: IOException (${e.message})")
        }
    }

    private fun startKeepAlive() {
        keepAliveJob = coroutineScope.launch {
            while (isActive) {
                if (timerKeepAlive >= 10) {
                    sendProtocol(KeepAliveRequest().toByteBuffer())
                    timerKeepAlive = 0
                } else {
                    delay(1000)
                    timerKeepAlive++
                }
            }
        }
    }

    private fun startTcpReceiver() {
        receiverJob = coroutineScope.launch {
            tcpReceiverLoop(this)
        }
    }

    private suspend fun tcpReceiverLoop(scope: CoroutineScope) {
        Log.d("TcpReceiver STARTED...")
        while (scope.isActive) {
            try {
                val packetAnalyzer = PacketAnalyzer(withContext(Dispatchers.IO) {
                    socket?.getInputStream() ?: throw IOException("Socket is not connected")
                })
                val protocolDefine = packetAnalyzer.getProtocolId()
                val receivedData = packetAnalyzer.getData()
                timerKeepAlive = 0
                if(protocolDefine != null && receivedData != null) {
                    listener?.onReceivedData(protocolDefine, receivedData) // 데이터 수신 알림
                }
                else {
                    Log.w("프로토콜 확인에 실패하였습니다.")
                }
            } catch (e: SocketTimeoutException) {
                Log.e("TcpReceiver: SocketTimeoutException (${e.message})")
            } catch (e: IOException) {
                handleError("TcpReceiver: IOException (${e.message})")
            } catch (e: SocketException) {
                handleError("TcpReceiver: SocketException (${e.message})")
            }

            // 딜레이 추가
            delay(10)
        }
        Log.d("TcpReceiver STOPPED")
    }

    private fun handleError(errorMessage: String) {
        Log.e(errorMessage)
        disconnect()
        reconnectAndStartJobs()
    }

    fun setOnTcpEventListener(listener: OnTcpEventListener) {
        this.listener = listener
    }

    fun sendProtocol(sendByteBuffer: ByteBuffer) {
        if (!isConnected) {
            Log.e("SendProtocol: 연결되지 않은 상태입니다.")
            return
        }

        try {
            socket?.getOutputStream()?.let { outStream ->
                outStream.write(sendByteBuffer.array())
                outStream.flush()
            }
            timerKeepAlive = 0
        } catch (e: IOException) {
            handleError("SendProtocol: IOException (${e.message})")
        } catch (e: Exception) {
            handleError("SendProtocol: Exception (${e.message})")
        }
    }

    fun onDestroy() {
        supervisorJob.cancel()
    }
}