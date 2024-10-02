package com.kct.iqsdisplayer.network

import androidx.lifecycle.ViewModelProvider
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.common.SystemReadyModel
import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.data.packet.send.KeepAliveRequest
import com.kct.iqsdisplayer.ui.FragmentFactory.Index
import com.kct.iqsdisplayer.ui.FragmentFactory.replaceFragment
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
    //private val socketLock = Mutex()

    private var inputStream: InputStream? = null
    private var timerKeepAlive = 0
    private var isConnected = false
    private var enableKeepAlive = false

    private val supervisorJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + supervisorJob)
    private lateinit var keepAliveJob: Job
    private lateinit var receiverJob: Job

    private var listener: OnTcpEventListener? = null

    init { Log.d("Tcp 연결 시도: IP:${host}, PORT:${port}") }

    private fun reconnectAndStartJobs() {
        coroutineScope.launch(Dispatchers.IO) {
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

    fun setOnTcpEventListener(listener: OnTcpEventListener) {
        this.listener = listener
    }

    fun start() {
        coroutineScope.launch(Dispatchers.IO) {
            reconnectAndStartJobs()
        }
    }

    fun sendData(sendByteBuffer: ByteBuffer) {
        coroutineScope.launch(Dispatchers.IO) {
            sendProtocol(sendByteBuffer)
        }
    }

    private suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) { // IO dispatcher를 사용하여 백그라운드에서 실행
            var exceptionMessage: String? = null
            try {
                if (socket == null || socket?.isClosed == true) {
                    socket = Socket(host, port).apply { soTimeout = 20000 }
                }
                inputStream = socket?.getInputStream()
                listener?.onConnected() // 연결 성공 알림
                Log.d("Tcp 연결 성공: IP:${host}, PORT:${port}")
                true
            } catch (e: SocketException) {
                exceptionMessage = "Tcp 연결 실패: SocketException (${e.message})"
                false
            } catch (e: SocketTimeoutException) {
                exceptionMessage = "Tcp 연결 실패: SocketTimeoutException (${e.message})"
                false
            } catch (e: IOException) {
                exceptionMessage = "Tcp 연결 실패: IOException (${e.message})"
                false
            } catch (e: Exception) {
                e.printStackTrace()
                exceptionMessage = "Tcp 연결 실패: Exception (${e.message})"
                false
            } finally {
                exceptionMessage?.let { Log.e(it) }
            }
        }
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

    fun enableKeepAlive(isEnable: Boolean) {
        enableKeepAlive = isEnable
    }

    private fun startKeepAlive() {
        keepAliveJob = coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (timerKeepAlive >= 10 && enableKeepAlive) {
                    Log.v("KeepAlive 전송")
                    sendProtocol(KeepAliveRequest().toByteBuffer())
                } else {
                    delay(1000)
                    timerKeepAlive++
                }
            }
        }
    }

    private fun startTcpReceiver() {
        receiverJob = coroutineScope.launch(Dispatchers.IO) {
            tcpReceiverLoop(this)
        }
    }

    private suspend fun tcpReceiverLoop(scope: CoroutineScope) {
        Log.d("TCP 네트워크 수신중...")
        while (scope.isActive) {
            try {
                // 연결 상태 확인
                if (socket?.isConnected != true) {
                    throw SocketException("Socket is disconnected")
                }

                // 입력 스트림에 읽을 데이터가 있는지 확인
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
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                Log.e("TcpReceiver: SocketTimeoutException (${e.message})")
            } catch (e: IOException) {
                e.printStackTrace()
                handleError("TcpReceiver: IOException (${e.message})")
            } catch (e: SocketException) {
                e.printStackTrace()
                handleError("TcpReceiver: SocketException (${e.message})")
            }
            //delay(10)
        }
        Log.d("TcpReceiver STOPPED")
    }

    private fun handleError(errorMessage: String) {
        Log.e(errorMessage)
        disconnect()
        reconnectAndStartJobs()
    }

    private suspend fun sendProtocol(sendByteBuffer: ByteBuffer) {
        withContext(Dispatchers.IO) {
            socket?.let {
                if (!it.isConnected || it.isClosed) {
                    Log.w("SendProtocol: 연결되지 않은 상태입니다.")
                    return@let
                }
                try {
                    it.getOutputStream().let { outStream ->
                        outStream.write(sendByteBuffer.array())
                        outStream.flush()
                    }
                    timerKeepAlive = 0
                } catch (e: IOException) {
                    e.printStackTrace()
                    handleError("SendProtocol: IOException (${e.message})")
                } catch (e: Exception) {
                    e.printStackTrace()
                    handleError("SendProtocol: Exception (${e.message})")
                }
            }
        }
    }

    fun onDestroy() {
        supervisorJob.cancel()
    }
}