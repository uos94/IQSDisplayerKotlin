package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.data.packet.BaseSendPacket
import com.kct.iqsdisplayer.data.packet.send.KeepAliveRequest
import com.kct.iqsdisplayer.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer

class TCPClient() {

    interface OnTcpEventListener {
        fun onConnected()
        fun onReceivedData(protocolDefine: ProtocolDefine, receivedData: BaseReceivePacket)
        fun onDisconnected()
    }

    private var socket: Socket? = null
    private val retryDelay = 3000L

    private var timerKeepAlive = 0
    private var isConnected = false
    private var enableKeepAlive = false

    private var coroutineScope = CoroutineScope(Dispatchers.IO)

    private var connection: Job?    = null
    private var jobSendData: Job?    = null
    private var jobKeepAlive: Job?   = null
    private var jobTcpReceiver: Job? = null

    private var listener: OnTcpEventListener? = null

    fun setOnTcpEventListener(listener: OnTcpEventListener) {
        this.listener = listener
    }

    fun connectAndStart() {
        connection = coroutineScope.launch {
            // 연결이 성공할 때까지 재시도
            //Log.v("connectAndStart isActive[$isActive], isConnected[$isConnected]")
            while (isActive) {
                if (isConnected) {
                    disconnect() // 이미 연결된 상태라면 기존 연결을 끊고 재시작 준비
                    delay(retryDelay)
                }

                if (connect()) {
                    Log.i("연결 성공")
                    startTcpReceiver() // 연결이 성공한 후 데이터 수신 시작
                    break // 연결에 성공했으므로 반복 종료
                } else {
                    Log.i("연결 재시도 중...") // 연결이 실패하면 재시도
                    delay(retryDelay)
                }
            }
        }
    }

    fun sendData(sendByteBuffer: ByteBuffer, packetName: String = "") {
        jobSendData = coroutineScope.launch {
            sendProtocol(sendByteBuffer, packetName)
        }
    }

    fun sendData(packet: BaseSendPacket) {
        jobSendData = coroutineScope.launch {
            sendProtocol(packet.toByteBuffer(), packet.toString())
        }
    }

    fun enableKeepAlive(isEnable: Boolean) {
        Log.d("KeepAlive 활성화 isEnable:$isEnable")
        enableKeepAlive = isEnable
    }

    private suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            val host = Const.ConnectionInfo.IQS_IP
            val port = Const.ConnectionInfo.IQS_PORT
            Log.d("Tcp 연결 시도: IP:$host, PORT:${Const.ConnectionInfo.IQS_PORT}")

            var exceptionMessage: String? = null
            try {
                if (socket == null || socket?.isClosed == true) {
                    socket = Socket(host, port).apply { soTimeout = 20000 }
                }

                isConnected = true
                listener?.onConnected()
                Log.d("Tcp 연결 성공: IP:${host}, PORT:${port}")
                startKeepAlive() // 연결 후 keepAlive 시작
                true
            } catch (e: Exception) {
                exceptionMessage = "Tcp 연결 실패: Exception (${e.message})"
                false
            } finally {
                exceptionMessage?.let { Log.e(it) }
            }
        }
    }

    private fun disconnect() {
        isConnected = false

        connection?.cancel()
        jobTcpReceiver?.cancel()
        jobSendData?.cancel()
        jobKeepAlive?.cancel()

        try {
            socket?.inputStream?.close()
            socket?.close()
            socket = null
            Log.i("Tcp 연결 종료")
            listener?.onDisconnected()
        } catch (e: Exception) {
            Log.s(e)
            Log.e("Tcp 연결 종료 실패: Exception (${e.message})")
        }
    }

    private fun startKeepAlive() {
        jobKeepAlive = coroutineScope.launch {
            Log.d("startKeepAlive isActive[$isActive], isConnected[$isConnected]")
            while (isActive && isConnected) {
                // enableKeepAlive가 설정될 때까지 대기
                if (!enableKeepAlive) {
                    //Log.v("KeepAlive 활성화 대기중..")
                    delay(1000)  // 1초 대기
                    continue
                }

                if (timerKeepAlive >= 10) {
                    sendData(KeepAliveRequest())
                    timerKeepAlive = 0
                } else {
                    delay(1000)
                    timerKeepAlive++
                }
            }
            Log.d("KeepAlive 종료")
        }
    }

    private fun startTcpReceiver() {
        jobTcpReceiver = coroutineScope.launch {
            tcpReceiverLoop()
        }
    }

    private fun tcpReceiverLoop() {
        Log.d("TCP 네트워크 수신중...")
        while (isConnected) {
            try {
                if (socket?.isConnected != true) {
                    throw SocketException("Socket is disconnected")
                }
                val inputStream = socket?.inputStream
                if ((inputStream?.available() ?: 0) > 0) {
                    val packetAnalyzer = PacketAnalyzer(inputStream!!)
                    val protocolDefine = packetAnalyzer.getProtocolId()
                    val receivedData = packetAnalyzer.getData()
                    timerKeepAlive = 0
                    if (protocolDefine != null) {
                        listener?.onReceivedData(protocolDefine, receivedData)
                    } else {
                        Log.w("프로토콜 확인에 실패하였습니다.")
                    }
                }
            } catch (e: Exception) {
                handleError("TcpReceiver: Exception (${e.message})", e)
            }
        }
        Log.d("TcpReceiver 수신 종료")
    }

    private fun handleError(errorMessage: String, e: Throwable? = null) {
        Log.e(errorMessage)
        e?.let { Log.s(e) }
        coroutineScope.launch { connectAndStart() }
    }

    private fun sendProtocol(sendByteBuffer: ByteBuffer, tag: String) {
        timerKeepAlive = 0 // KeepAlive 타이머 초기화

        socket?.let {
            if (!isConnected || it.isClosed || !it.isConnected) {
                Log.w("SendProtocol: 연결되지 않은 상태입니다. 재연결 시도 중...")
                connectAndStart() // 연결이 끊겼다면 재연결 시도
                return@let
            }
            try {
                it.getOutputStream().let { outStream ->
                    outStream.write(sendByteBuffer.array())
                    outStream.flush()
                }
            } catch (e: Exception) {
                handleError("SendProtocol: 예기치 않은 오류 발생 - ${e.message}", e)
                Log.e("실패한 패킷정보 $tag")
            }
        } ?: run {
            Log.w("SendProtocol: 소켓이 null 상태입니다. 재연결 시도 중...")
            connectAndStart()
        }
    }

    fun release() {
        Log.i("TcpClient 사용 안함.")
        disconnect()
    }
}