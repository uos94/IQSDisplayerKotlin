package com.kct.iqsdisplayer.service

import com.kct.iqsdisplayer.network.ConnectFTP
import SendBufferClass
import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfoManager
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.LogFile
import com.kct.iqsdisplayer.util.copyFile
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.System.arraycopy
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 순번발행기와 TCP UDP FTP 통신을 하기 위한 서비스 클래스
 * 통신은 Thread를 통해 개별로 수행
 * HISON : Coroutine으로 다 빼려고 했으나 덩어리가 커서 일단 그대로 감. 추후 변경 요망
 */
class IQSComClass : Service() {

    private var commResultReceiver: ResultReceiver? = null

    private var connectTcp: ConnectTcp? = null // TCP 통신 Thread
    private var connectFTP: ConnectFTP? = null // FTP 통신 및 이벤트 처리 클래스

    lateinit var tcpSocket: Socket
    lateinit var tcpReceiver: TcpReceiver

    /** KeepAlive용 카운트 10이 되면 send */
    private var timerKeepAlive = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("IQSComClass onCreate")
        LogFile.write("IQSComClass onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("IQSComClass onStartCommand")
        try {
            commResultReceiver = intent.getParcelableExtra("receiver")

            if (connectTcp == null) {
                Log.d("ConnectTCP is null start init")
                initNetwork()
            } else {
                Log.d("ConnectTCP is not null")
            }
        } catch (e: Exception) {
            Log.e("onStartCommand : Exception (" + e.message + ")")
            // stopSelf()
            onDestroy()
        }
        return START_NOT_STICKY
    }

    private fun initNetwork() {
        connectTcp = ConnectTcp().apply { start() }
    }

    /**
     * 순번발행기와 TCP 연결
     * TimeOut 20초, IP와 Port 번호는 CommunicationInfo 에서 받아옴,
     * 연결 성공후 TCP Recive 하는 Thread를 생성
     * Timeout,SocketException, IOException 발생 시 서비스 클래스를 실행 한 액티비티에 전달
     */
    inner class ConnectTcp : Thread() {
        init {
            Log.d("ConnectTcp: Initializing connection to ${Const.CommunicationInfo.IQS_IP}:${Const.CommunicationInfo.IQS_PORT}")
        }

        override fun run() {
            super.run()
            var exceptionMessage: String? = null
            try {
                tcpSocket = Socket(Const.CommunicationInfo.IQS_IP, Const.CommunicationInfo.IQS_PORT).apply { setSoTimeout(20000) }
                tcpReceiver = TcpReceiver().apply { start() }
            } catch (e: SocketException) {
                exceptionMessage = "ConnectTcp : SocketException (${e.message})"
            } catch (e: SocketTimeoutException) {
                exceptionMessage = "ConnectTcp : SocketTimeoutException (${e.message})"
            } catch (e: IOException) {
                exceptionMessage = "ConnectTcp : IOException (${e.message})"
            } catch (e: Exception) {
                exceptionMessage = "ConnectTcp : Exception (${e.message})"
            } finally {
                exceptionMessage?.let {
                    Log.e(it)
                    LogFile.write(it)
                    stopSelf()
                }
            }
        }
    }

    /**
     * 순번발행기 TCP 데이터 수신
     * TCP 통신으로 받아들이는 정보는 byte형으로 받음
     * 통신 최대 사이즈는 8192byte
     * SocketException, Timeout, 일 경우 ComResultReceiver을 통해 실행한 액티비티에 전달
     */
    inner class TcpReceiver : Thread() {
        private var readDataArr: ByteArray = ByteArray(4)
        private var isRun = true

        fun setRun(isRun: Boolean) {
            this.isRun = isRun
        }

        override fun run() {
            var exceptionMessage: String? = null

            while (isRun) {
                try {
                    var nHeadRead = 0
                    var nToHeadRead = 4
                    var nOffset = 0

                    while (true) {
                        nHeadRead = tcpSocket.getInputStream().read(readDataArr, nOffset, nToHeadRead)
                        if (nHeadRead == -1) {
                            Log.d("Head Read End Of Stream")
                            break
                        }
                        nToHeadRead -= nHeadRead
                        nOffset += nHeadRead
                        if (nToHeadRead > 0) {
                            Log.d("Head Not Read Complete")
                        }
                    }

                    val nBodySize = ((readDataArr[1].toInt() and 0xff) shl 8) or (readDataArr[0].toInt() and 0xff)
                    val nId = ((readDataArr[3].toInt() and 0xff) shl 8) or (readDataArr[2].toInt() and 0xff)

                    if (nId == 0x00) {
                        Log.d("ID is null")
                        isRun = false
                        break // while 루프 종료
                    }

                    val bodyData = ByteArray(nBodySize)
                    var nBodyRead = 0
                    var nToBodyRead = nBodySize
                    var nBodyOffset = 0

                    while (true) {
                        nBodyRead = tcpSocket.getInputStream().read(bodyData, nBodyOffset, nToBodyRead)
                        if (nBodyRead == -1) {
                            Log.d("Body Read End")
                            break
                        }
                        nToBodyRead -= nBodyRead
                        nBodyOffset += nBodyRead
                        if (nToBodyRead > 0) {
                            Log.d("Body Not Read Complete")
                        }
                    }

                    val realRecv = ByteArray(4 + nBodySize)
                    arraycopy(readDataArr, 0, realRecv, 0, 4)
                    arraycopy(bodyData, 0, realRecv, 4, nBodySize)

                    analysisID(realRecv)

                    timerKeepAlive = 0
                } catch (e: SocketException) {
                    exceptionMessage = "TcpReceiver : SocketException (${e.message}"
                } catch (e: SocketTimeoutException) {
                    exceptionMessage = "TcpReceiver : SocketTimeoutException (${e.message}"
                } catch (e: IOException) {
                    exceptionMessage = "TcpReceiver : IOException (${e.message}"
                } finally {
                    exceptionMessage?.let {
                        Log.e(it)
                        LogFile.write(it)
                        isRun = false // 쓰레드 종료
                        stopSelf()
                    }
                }
            }
        }
    }


    /**
     * 앱에서 순번발행기로 TCP 데이터 전송
     * 전달 파라미터는 byte형으로 ByteBuffer에 담아서 전송
     */
    inner class SendProtocol(private val sendByteBuffer: ByteBuffer) : Thread() {

        override fun run() {
            try {
                tcpSocket.getOutputStream().use { outStream ->
                    outStream.write(sendByteBuffer.array())
                    outStream.flush()
                }
                timerKeepAlive = 0
                // Log.d(Tag,"Send Message to TCP Server ");
            } catch (e: java.lang.Exception) {
                Log.d("SendProtocol : Exception (" + e.message + ")")
            }
        }
    }

    /**
     * 순번발행기와 10초간 송수신이 없을경우 KEEPALIVE를 순번발행기로 전달
     * 전달 파라미터는 byte형으로 ByteBuffer에 담아서 전송
     */
    inner class SendKEEPALIVE : Thread() {
        override fun run() {
            while (true) {
                if (timerKeepAlive >= 10) {
                    try {
                        val sendBuffer = SendBufferClass()
                        val sendByteBuffer = sendBuffer.keepAlive()

                        tcpSocket.getOutputStream().use { outStream ->
                            outStream.write(sendByteBuffer.array())
                            outStream.flush()
                        }
                    } catch (e: java.lang.Exception) {
                        Log.d("SendKEEPALIVE : Exception (${e.message})")
                        stopSelf()
                        break
                    }
                    timerKeepAlive = 0
                } else {
                    try {
                        timerKeepAlive += 1
                        sleep(1000)
                    } catch (e: InterruptedException) {
                        Log.d("SendKEEPALIVE : InterruptedException (${e.message})")
                        stopSelf()
                        break
                    }
                }
            }
        }
    }

    /**
     * 순번발행기에 접속하여 FTP를 통해 다운로드 받는 클래스
     * 순번발행기 경로내 이미지, 패치파일, 사운드, 홍보영상 다운로드
     * 최초 접속시 이미지, 패치파일, 사운드, 홍보영상은 모두 다운로드 받으며,
     * 다운로드 전 패치파일과 홍보영상은 앱 디렉토리내에서 전부 삭제 하며 새로 받아옴
     *
     */
    // IQS 접속 시 기본 파일 다운로드 스레드
    inner class DownLoadFTPFile(private val downList: Array<String>, private val basic: Boolean) : Thread() {
        private var localFolderPath: String = ""
        private var iqsPath: String = ""
        private var fileList: Array<String> = emptyArray()
        private var startProtocol: Short = 0
        private var endProtocol: Short = 0
        private var bundle: Bundle? = null
        private var result: Boolean = false
        private var status: Boolean = false
        private var appVer: Int = 0

        init {
            LogFile.write("FTP IP : ${Const.CommunicationInfo.IQS_IP} FTP PORT : ${Const.CommunicationInfo.FTP_PORT}")
            connectFTP = ConnectFTP()
            status = connectFTP?.ftpConnect(
                Const.CommunicationInfo.IQS_IP,
                Const.CommunicationInfo.FTP_ID,
                Const.CommunicationInfo.FTP_PW,
                Const.CommunicationInfo.FTP_PORT
            ) ?: false
        }

        fun setCategory(category: String) {
            iqsPath = connectFTP?.ftpGetDirectory() ?: ""

            when (category) {
                "Patch" -> {
                    localFolderPath = Const.Path.DIR_PATCH + Const.Path.SUB_PATH_PATCH
                    iqsPath += Const.Path.SUB_PATH_PATCH
                    startProtocol = ProtocolDefine.START_PATCH.value
                    endProtocol = ProtocolDefine.END_PATCH.value
                }
                "Sound" -> {
                    localFolderPath = Const.Path.DIR_SOUND + Const.Path.SUB_PATH_SOUND
                    iqsPath += Const.Path.SUB_PATH_SOUND
                    startProtocol = ProtocolDefine.START_SOUND.value
                    endProtocol = ProtocolDefine.END_SOUND.value
                }
                "Video" -> {
                    localFolderPath = Const.Path.DIR_VIDEO + Const.Path.SUB_PATH_VIDEO
                    iqsPath += Const.Path.SUB_PATH_VIDEO
                    startProtocol = ProtocolDefine.START_VIDEO.value
                    endProtocol = ProtocolDefine.END_VIDEO.value
                }
                "Image" -> {
                    localFolderPath = Const.Path.DIR_IMAGE + Const.Path.SUB_PATH_IMAGE
                    iqsPath += Const.Path.SUB_PATH_IMAGE
                    startProtocol = ProtocolDefine.START_IMAGE.value
                    endProtocol = ProtocolDefine.END_IMAGE.value
                }
                else -> Log.d("DownLoadFileList default")
            }
        }

        fun setFileList(fileList: Array<String>) {
            this.fileList = fileList
        }

        fun getFileList() {
            this.fileList = connectFTP?.ftpGetFileList(iqsPath) ?: emptyArray()
        }

        override fun run() {
            Log.d("FTP DownLoad Run")
            if (status) {
                for (category in downList) {
                    if (!isInterrupted) {
                        setCategory(category)
                        LogFile.write("FTP DownLoad Start Category : $category")

                        commResultReceiver?.send(startProtocol.toInt(), bundle)
                        if (basic) {
                            getFileList()
                        }

                        // 디렉토리 존재 확인 및 파일 삭제
                        val dir = File(localFolderPath)
                        if (dir.isDirectory && basic && (category == "Patch" || category == "Video")) {
                            dir.listFiles()?.forEach { childFile ->
                                if (childFile.isFile) {
                                    childFile.delete()
                                }
                            }
                        } else {
                            dir.mkdirs()
                        }

                        result = connectFTP?.ftpDownloadFile(iqsPath, localFolderPath, fileList, category) ?: false
                        bundle = Bundle()

                        if (!isInterrupted) {
                            bundle?.putBoolean("result", result)
                        } else {
                            bundle?.putBoolean("result", false)
                        }

                        if (result && category == "Patch") {
                            // 패치 파일의 경우 패치 파일 이름 번들에 담아 액티비티로 전달
                            val patchFile = getPatchFileName(localFolderPath, appVer)
                            bundle?.putString("FileName", patchFile)
                        }

                        commResultReceiver?.send(endProtocol.toInt(), bundle)
                        connectFTP?.reConnect = result
                        LogFile.write("FTP DownLoad Result : $result mReConnect : ${connectFTP?.reConnect}")

                        try {
                            sleep(1)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                            break
                        }
                    } else {
                        Log.d("Thread is Interrupted")
                        connectFTP?.reConnect = result
                        this.interrupt()
                        break
                    }
                }
            } else {
                Log.d("FTP Connect Failed")
                LogFile.write("FTP Connect Failed")
                bundle = Bundle().apply {
                    putBoolean("result", false)
                }
                commResultReceiver?.send(endProtocol.toInt(), bundle)
            }

            // IQS로 로그 파일 전송
            UploadFTP().start()
        }

        private fun getAppVersion(): Array<String>? {
            var appVer: Array<String>? = null
            try {
                val pi: PackageInfo = packageManager.getPackageInfo(packageName, 0)
                val appVersion = pi.versionName
                appVer = appVersion.split("\\.".toRegex()).toTypedArray()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return appVer
        }

        private fun getPatchFileName(folderPath: String, appVer: Int): String {
            var fileName = ""
            val dir = File(folderPath)
            val patchFile = dir.listFiles()

            val patchFileList = ArrayList<String>()

            patchFile?.forEach { file ->
                if (file.isFile && file.name.contains("IQSDisplay") && file.name.contains("apk")) {
                    patchFileList.add(file.name)
                    Log.d("File List Name : ${file.name}")
                }
            }

            if (patchFileList.isEmpty()) {
                fileName = ""
            } else {
                patchFileList.sort()
                val lastFile = patchFileList[patchFileList.size - 1]
                val lastFileVersion = lastFile.replace("IQSDisplay", "").replace(".apk", "")

                val fileVer = lastFileVersion.split("\\.".toRegex()).toTypedArray()

                if (fileVer.size == 3) {
                    try {
                        val nFileVer = (fileVer[0].toInt() * 100) + (fileVer[1].toInt() * 10) + (fileVer[2].toInt())
                        if (nFileVer > appVer) {
                            fileName = lastFile
                        }
                    } catch (e: Exception) {
                        Log.d("Integer Parsing error")
                        fileName = ""
                    }
                }
            }
            return fileName
        }
    }

    // IQS로 로그 파일 전송하는 스레드
    private inner class UploadFTP : Thread() {
        override fun run() {
            val serverLogFolderPath = "${Const.Path.SUB_PATH_LOG}Display${ScreenInfoManager.instance.winNum}/"
            LogFile.write("Try Send Log File ($serverLogFolderPath)")

            try {
                // DisplayLog 디렉토리 및 하위 디렉토리 생성 시도 (null-safe 체크 및 apply 사용)
                connectFTP?.apply {
                    ftpCreateDirectory(Const.Path.SUB_PATH_LOG)
                    ftpCreateDirectory(serverLogFolderPath) //Const.Path.SUB_PATH_LOG/Display000/
                }

                val localLogFolder = File(Const.Path.DIR_LOG)
                val localLogFileList = localLogFolder.listFiles()?.filter { it.isFile } // 파일만 필터링

                // 어제 로그와 오늘 로그 업로드 (null-safe 체크 및 destructuring 선언 사용)
                localLogFileList?.takeLast(2)?.forEachIndexed { index, file ->
                    val fileName = file.name
                    // 현재 날짜 파일은 제외
                    if (index == 1 || fileName != SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) + ".txt") {
                        Log.d("uploadLogFileToServerSub() : 업로드 파일 = $fileName")
                        LogFile.write("uploadLogFileToServerSub() : 업로드 파일 = $fileName")
                        uploadLogFileToServerSub(fileName)
                    }
                }
            } catch (e: Exception) {
                onDestroy()
                LogFile.write("Failed Send Log Files (${e.message})")
            }
        }
        /*
        override fun run() {
            val serverLogFolderPath = "${Const.Path.SUB_PATH_LOG}Display${ScreenInfoManager.instance.winNum}/"
            LogFile.write("Try Send Log File (${serverLogFolderPath})")

            try {
                // DisplayLog 디렉토리 생성
                if (connectFTP?.ftpCreateDirectory(Const.Path.SUB_PATH_LOG) == true) {
                    LogFile.write("Create dir ${Const.Path.SUB_PATH_LOG})")
                }

                // 내 표시기 번호 하위 디렉토리 생성
                if (connectFTP?.ftpCreateDirectory(serverLogFolderPath) == true) {
                    Log.d("Create dir")
                    LogFile.write("Create dir (${serverLogFolderPath})")
                } else {
                    Log.d("Failed create dir")
                }

                val localLogFolder = File(Const.Path.DIR_LOG)
                val localLogFileList = localLogFolder.listFiles()
                val len = localLogFileList?.size ?: 0

                // 어제 로그
                if (len > 1) {
                    connectFTP?.ftpUploadFile(
                        srcFilePath = Const.Path.DIR_LOG + localLogFileList!![len - 2].name,
                        desFileName = localLogFileList[len - 2].name,
                        desDirectory = serverLogFolderPath
                    )
                }

                // 오늘 로그
                if (len > 0) {
                    connectFTP?.ftpUploadFile(
                        srcFilePath = Const.Path.DIR_LOG + localLogFileList!![len - 1].name,
                        desFileName = localLogFileList[len - 1].name,
                        desDirectory = serverLogFolderPath
                    )
                }
            } catch (e: Exception) {
                onDestroy()
                LogFile.write("Failed Send Log Files (${e.message})")
            }
        }*/
    }

    /**
     * IQS에 요청하는 ByteBuffer 값을 얻는 함수
     * @param code IQS로 전송할 프로토콜 ID
     */
    private fun requestIQS(code: Short) {
        val protocolName = ProtocolDefine.entries.find { it.value == code }?.name ?: "Unknown"
        Log.d(protocolName) // 실제 protocolName 출력
        LogFile.write("IQS Request $protocolName")

        val sendByteBuffer: ByteBuffer? = when (code) {
            ProtocolDefine.WAIT_REQUEST.value -> SendBufferClass().waitRequest()      // 대기인수 정보 요청 패킷
            ProtocolDefine.KEEP_ALIVE_REQUEST.value -> null                                 // KEEPALIVE 요청
            ProtocolDefine.INSTALL_INFO.value -> null                                 // 설치 정보 패킷
            ProtocolDefine.SUB_SCREEN_REQUEST.value -> null                                 // 보조 표시정보 요청
            ProtocolDefine.VIDEO_LIST_REQUEST.value -> SendBufferClass().videoListRequest() // 동영상 리스트 요청
            ProtocolDefine.VIDEO_DOWNLOAD_REQUEST.value -> SendBufferClass().videoDownLoadRequest() // 231211, by HAHU  광고파일 요청
            ProtocolDefine.ACCEPT_REQUEST.value -> {
                val sendByteBuffer = SendBufferClass().acceptAuthRequest()
                if (sendByteBuffer != null) {
                    sendByteBuffer
                } else {
                    Log.d("IP or Mac is null")
                    commResultReceiver?.let { stopSelf() }
                    null
                }
            }

            ProtocolDefine.RESERVE_LIST_REQUEST.value -> SendBufferClass().reserveList()      // 상담 예약 관련 추가 ADD sblee 19-12-03
            ProtocolDefine.RESERVE_UPDATE_INFO_REQUEST.value -> SendBufferClass().appVersionRequest()// 2022.07.21 dyyoon 업데이트 정보 요청 add
            else -> {
                Log.e("Unknown protocol code: $code")
                null
            }
        }

        sendByteBuffer?.let { SendProtocol(it).start() }
    }


    /**
     * 순번발행기에서 TCP 통신 수신한 데이터의 ID와 값을 분석함
     * @param bytes (TCP 통신 데이터)
     */
    private fun analysisID(bytes: ByteArray) {
        val recvPacket = Packet(bytes)
        val protocolID = recvPacket.getId()
        val size = recvPacket.getLength()
        val bundleData = Bundle()

        val protocolName = ProtocolDefine.entries.find { it.value == protocolID }?.name ?: "Unknown"
        Log.d(protocolName) // 실제 protocolName 출력
        LogFile.write("IQS Response $protocolName")

        when (protocolID) {
            ProtocolDefine.ACCEPT_REQUEST.value -> {
                // 접속 성공 응답
                requestIQS(ProtocolDefine.ACCEPT_REQUEST.value)
                SendKEEPALIVE().start()
            }
            // 접속 거부 응답
            ProtocolDefine.ACCEPT_REJECT.value -> {}
            // 접속 승인 응답
            ProtocolDefine.ACCEPT_AUTH_REQUEST.value -> {
                LogFile.write("IQS Response AcceptAuthRequest data : ${recvPacket.string}")
            }

            ProtocolDefine.ACCEPT_AUTH_RESPONSE.value -> {
                val winNum = recvPacket.integer
                val winIdList = recvPacket.string
                val winNameList = recvPacket.string
                val tellerInfo = recvPacket.string
                val mediaInfo = recvPacket.string
                val volumeInfo = recvPacket.string
                val waitList = recvPacket.string
                val time = recvPacket.integer
                val settingInfo = recvPacket.string
                val movieInfo = recvPacket.string
                val bellInfo = recvPacket.string
                val callInfo = recvPacket.string
                val reserve1 = recvPacket.string // 안내 멘트
                val reserve2 = recvPacket.string // 전산 장애 표시
                val reserve3 = recvPacket.string // 공석 표시
                ScreenInfoManager.instance.setTellerInfo(tellerInfo)
                LogFile.write("SetTellerInfo")

                ScreenInfoManager.instance.setWinList(winIdList, winNameList, waitList)
                LogFile.write("SetWinList")

                ScreenInfoManager.instance.setScreenInfo(settingInfo, reserve1, reserve2, reserve3, bellInfo, callInfo, mediaInfo, volumeInfo)

                LogFile.write(
                    "IQS Response AcceptAuthResponse WinNum : $winNum WinIDList : $winIdList WinNameList : $winNameList TellerInfo : $tellerInfo mediaInfo : $mediaInfo waitList : $waitList " +
                            "settingInfo : $settingInfo movieInfo : $movieInfo bellInfo : $bellInfo callInfo : $callInfo info ment : $reserve1 Error : $reserve2 Empty : $reserve3"
                )

                commResultReceiver?.send(ProtocolDefine.ACCEPT_AUTH_RESPONSE.value.toInt(), bundleData)

                requestIQS(ProtocolDefine.RESERVE_LIST_REQUEST.value)
            }

            ProtocolDefine.WAIT_RESPONSE.value -> {
                // 대기 인수 정보 응답 패킷
                Log.d("WaitResponse")

                val ticketNum = recvPacket.integer            // 발권 번호
                val winID = recvPacket.integer                // 창구 ID
                val waitNum = recvPacket.integer              // 창구 대기 인수

                Log.d("TicketNum : $ticketNum WinID : $winID WaitNum : $waitNum")

                if (winID == ScreenInfoManager.instance.winID) {
                    // 같은 창구일 때
                    ScreenInfoManager.instance.setWaitResponse(ticketNum, waitNum) // camelCase로 변경
                    LogFile.write("IQS Response WaitResponse TicketNum : $ticketNum WinID : $winID WaitNum : $waitNum")
                    commResultReceiver?.send(ProtocolDefine.WAIT_RESPONSE.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }
            // 호출 요청
            ProtocolDefine.CALL_REQUEST.value,
            ProtocolDefine.RE_CALL_REQUEST.value -> {
                // 호출 요청 또는 재호출 요청
                val resultCode = if (protocolID == ProtocolDefine.CALL_REQUEST.value) {
                    ProtocolDefine.CALL_REQUEST.value
                } else {
                    ProtocolDefine.RE_CALL_REQUEST.value
                }

                val errorStatus = recvPacket.integer
                val callNum = recvPacket.integer
                val ticketWinID = recvPacket.integer
                val callWinID = recvPacket.integer
                val winWaitNum = recvPacket.integer
                val callWinNum = recvPacket.integer
                val lastCallNum = recvPacket.string
                val bkDisplay = recvPacket.integer
                val bkWay = recvPacket.integer
                val reserve = recvPacket.integer
                val vipFlag = recvPacket.integer

                var result = false
                LogFile.write(
                    "IQS Response ReCallRequest ErrorStatus : $errorStatus CallNum : $callNum TicketWinID : $ticketWinID CallWinID : $callWinID WinWaitNum : $winWaitNum " +
                            "CallWinNum : $callWinNum LastCallNum : $lastCallNum BkDisplay : $bkDisplay BkWay : $bkWay Reserve : $reserve VIPFlag : $vipFlag"
                )

                if (Const.CommunicationInfo.CALLVIEW_MODE == "2" || Const.CommunicationInfo.CALLVIEW_MODE == "3") { // 보조 순번
                    result = ScreenInfoManager.instance.setCallNum(
                        errorStatus,
                        callNum,
                        ticketWinID,
                        callWinID,
                        winWaitNum,
                        callWinNum,
                        lastCallNum,
                        bkDisplay,
                        bkWay,
                        reserve,
                        vipFlag
                    )
                    if (result) {
                        bundleData.putString("Display", "Main")
                        commResultReceiver?.send(resultCode.toInt(), bundleData)
                    }
                } else {
                    if (ScreenInfoManager.instance.pjt == 0) { // 사용 중
                        if (errorStatus == 0) {
                            // 정상일 때
                            if (callWinNum == ScreenInfoManager.instance.winNum) {
                                result = ScreenInfoManager.instance.setCallNum(
                                    errorStatus,
                                    callNum,
                                    ticketWinID,
                                    callWinID,
                                    winWaitNum,
                                    callWinNum,
                                    lastCallNum,
                                    bkDisplay,
                                    bkWay,
                                    reserve,
                                    vipFlag
                                )
                                if (result) {
                                    bundleData.putString("Display", "Main")
                                    commResultReceiver?.send(resultCode.toInt(), bundleData)
                                }
                            } else {
                                Log.d("CallWinNum != screenInfo.getWinNum()   --->   $callWinNum, ${ScreenInfoManager.instance.winNum}")
                            }

                        } else {
                            // 장애 상황
                            if (bkDisplay == ScreenInfoManager.instance.winNum) {
                                result = ScreenInfoManager.instance.setCallNum(
                                    errorStatus,
                                    callNum,
                                    ticketWinID,
                                    callWinID,
                                    winWaitNum,
                                    callWinNum,
                                    lastCallNum,
                                    bkDisplay,
                                    bkWay,
                                    reserve,
                                    vipFlag
                                )
                                if (result) {
                                    bundleData.putString("Display", "Bk")
                                    commResultReceiver?.send(resultCode.toInt(), bundleData)
                                }
                            }
                        }
                    } else {
                        // 공석 상태
                        LogFile.write("$resultCode but Display is PJT")
                    }
                }
            }


            ProtocolDefine.DISPLAY_INFO.value -> {
                // 화면 표시기 정보 패킷
                val data = recvPacket.string
                ScreenInfoManager.instance.setDisplayInfo(data) 
                commResultReceiver?.send(ProtocolDefine.DISPLAY_INFO.value.toInt(), bundleData)
            }

            ProtocolDefine.EMPTY_REQUEST.value -> {
                // 부재 정보 요청
                val winNum = recvPacket.integer              // 부재 설정 창구 직원 창구 번호
                val emptyFlag = recvPacket.integer           // 부재 설정 여부
                val emptyMsg = recvPacket.string               // 부재 설정 메시지

                if (winNum == ScreenInfoManager.instance.winNum) {
                    ScreenInfoManager.instance.setEmpty(emptyFlag, emptyMsg)
                    LogFile.write("IQS Response EmptyRequest WinNum : $winNum EmptyFlag : $emptyFlag EmptyMsg : $emptyMsg")
                    Log.d("EmptyRequest WinNum : $winNum EmptyFlag : $emptyFlag EmptyMsg : $emptyMsg")
                    commResultReceiver?.send(ProtocolDefine.EMPTY_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            ProtocolDefine.INFO_MESSAGE_REQUEST.value -> {
                // 안내 메시지 설정 요청
                val infoMessageWinNum = recvPacket.integer        // 창구 번호
                val infoMessage = recvPacket.string               // 안내 메시지

                if (infoMessageWinNum == ScreenInfoManager.instance.winNum) {
                    Log.d("infoMessage : $infoMessage")
                    ScreenInfoManager.instance.ment = infoMessage
                    LogFile.write("IQS Response InfoMessageRequest InfoMessageWinNum : $infoMessageWinNum InfoMessage : $infoMessage")
                    commResultReceiver?.send(ProtocolDefine.INFO_MESSAGE_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            ProtocolDefine.TELLER.value -> {
                // 직원 정보
                val tellerList = recvPacket.string          // 직원 설정 정보 리스트
                Log.d("tellerList : $tellerList")
                LogFile.write("IQS Response Teller Data : $tellerList")
                ScreenInfoManager.instance.setTellerList(tellerList)
                commResultReceiver?.send(ProtocolDefine.TELLER.value.toInt(), bundleData)
            }

            // 시스템 종료 패킷
            ProtocolDefine.SYSTEM_OFF.value -> {
                commResultReceiver?.send(ProtocolDefine.SYSTEM_OFF.value.toInt(), bundleData)
            }

            // 동영상 설정 패킷
            ProtocolDefine.VIDEO_SET.value -> {
                val videoInfo = recvPacket.string
                ScreenInfoManager.instance.setVideoInfo(videoInfo) 
                LogFile.write("IQS Response VideoSet Data : $videoInfo")
                try {
                    commResultReceiver?.send(ProtocolDefine.VIDEO_SET.value.toInt(), bundleData)
                    val list = arrayOf("Video")
                    val downLoadFTPFile = DownLoadFTPFile(list, false)
                    downLoadFTPFile.setFileList(ScreenInfoManager.instance.adFileList) 
                    Thread(downLoadFTPFile).start()
                } catch (e: Exception) {
                    LogFile.write("Failed VideoSet")
                }
            }

            ProtocolDefine.VOLUME_TEST.value -> {
                // 볼륨 테스트 패킷
                val volumeInfo = recvPacket.string
                try {
                    val volume = ScreenInfoManager.instance.setVolumTest(volumeInfo) 

                    val volumeWin = volume[0].toIntOrNull() ?: 0
                    val volumeSize = volume[1].toIntOrNull() ?: 0
                    val volumeName = volume[2]
                    val playNum = volume[3].toIntOrNull() ?: 0
                    val infoSound = volume[4].toIntOrNull() ?: 0

                    Log.d("VolumeWin : $volumeWin VolumeSize : $volumeSize VolumeName : $volumeName PlayNum : $playNum InfoSound : $infoSound")
                    LogFile.write("IQS Response VolumTest Data : $volumeInfo")

                    if (ScreenInfoManager.instance.winNum == volumeWin) {
                        // 액티비티로 전달
                        bundleData.putInt("VolumeWin", volumeWin)
                        bundleData.putInt("VolumeSize", volumeSize)
                        bundleData.putString("VolumeName", volumeName)
                        bundleData.putInt("PlayNum", playNum)
                        bundleData.putInt("InfoSound", infoSound)
                        commResultReceiver?.send(ProtocolDefine.VOLUME_TEST.value.toInt(), bundleData)
                    } else {
                        // 다른 창구일 때 처리 (필요한 경우)
                    }
                } catch (e: Exception) {
                    LogFile.write("Failed Volum Test")
                }
            }

            // 음성 설정 패킷
            ProtocolDefine.SOUND_SET.value -> {
                val soundSetData = recvPacket.string
                ScreenInfoManager.instance.soundSet(soundSetData)
                LogFile.write("IQS Response SoundSetData Data : $soundSetData")
                commResultReceiver?.send(ProtocolDefine.SOUND_SET.value.toInt(), bundleData)
            }

            // 재시작 요청
            ProtocolDefine.RESTART_REQUEST.value -> {
                val mode = recvPacket.integer                     // 표시기 모드 (2:창구표시기, 6:보조표시기)
                val restartWinNum = recvPacket.integer            // 창구번호

                if (restartWinNum == ScreenInfoManager.instance.winNum) {
                    Const.CommunicationInfo.MODE = mode
                    LogFile.write("IQS Response Restart Mode : $mode")
                    commResultReceiver?.send(ProtocolDefine.RESTART_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            ProtocolDefine.CROWDED_REQUEST.value -> {
                // 혼잡 요청
                Log.d("CrowedRequest")
                val isCrowded = recvPacket.integer                 // 혼잡 여부 BOOL
                val crowdedWinID = recvPacket.integer              // 창구 ID
                val crowdedMsg = recvPacket.string              // 혼잡 메시지

                if (crowdedWinID == ScreenInfoManager.instance.winID) {
                    ScreenInfoManager.instance.setCrowed(isCrowded, crowdedMsg)
                    LogFile.write("IQS Response CrowedRequest isCrowed : $isCrowded CrowedWinID : $crowdedWinID CrowedMsg : $crowdedMsg")
                    commResultReceiver?.send(ProtocolDefine.CROWDED_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            ProtocolDefine.WIN_RESPONSE.value -> {
                // 창구 응답
                val winIDList = recvPacket.string              // 창구 ID 리스트
                val winNmList = recvPacket.string              // 창구명 리스트
                val waitList = recvPacket.string              // 대기 인수 리스트

                ScreenInfoManager.instance.setWinList(winIDList, winNmList, waitList)
                Log.d("Response WinResponse WinIDList : $winIDList WinNmList : $winNmList WaitList : $waitList")
                LogFile.write("IQS Response WinResponse WinIDList : $winIDList WinNmList : $winNmList WaitList : $waitList")
                commResultReceiver?.send(ProtocolDefine.WIN_RESPONSE.value.toInt(), bundleData)
            }

            // KEEPALIVE 응답
            ProtocolDefine.KEEP_ALIVE_RESPONSE.value -> {}

            // 보조 표시 정보 응답
            ProtocolDefine.SUB_SCREEN_RESPONSE.value -> {
                val waitListInfo = recvPacket.string                   // 대기 인수 정보
                LogFile.write("IQS Response SubScreenResponse Data : $waitListInfo")
            }

            // 배경 음악 정보 패킷
            ProtocolDefine.BGM_INFO.value -> {
                val bgmInfo = recvPacket.string                        // 배경음악 정보
                LogFile.write("IQS Response BGMInfo Data : $bgmInfo")
            }

            // 동영상 리스트 응답
            ProtocolDefine.VIDEO_LIST_RESPONSE.value -> {
                val videoList = recvPacket.string                      // 동영상 리스트
                Log.d("VideoList : $videoList")
                LogFile.write("IQS Response VideoListResponse Data : $videoList")

                // 231130, by HAHU  동영상 다운로드 후 앱 업데이트
                requestIQS(ProtocolDefine.RESERVE_UPDATE_INFO_REQUEST.value)
            }

            // 설치 정보 패킷
            ProtocolDefine.INSTALL_INFO.value -> {}

            // 호출 취소 요청
            ProtocolDefine.CALL_CANCEL.value -> {
                val cancelError = recvPacket.integer                      // 장애 여부
                val cancelCallNum = recvPacket.integer                    // 호출 번호
                val ticketWinID = recvPacket.integer                     // 발권 창구 ID
                val callWinID = recvPacket.integer                      // 호출 창구 ID
                val wait = recvPacket.integer                      // 대기 인수
                val callWinNum = recvPacket.integer                       // 호출 창구 번호
                val lastCallNumList = recvPacket.string                // 과거 호출 번호 리스트
                val bkNum = recvPacket.integer                      // 백업 표시기 번호

                LogFile.write(
                    "IQS Response CallCancel CancelError : $cancelError CancelCallNum : $cancelCallNum TicketWINID : $ticketWinID " +
                            "CallWINID : $callWinID Wait : $wait CallWinNUM : $callWinNum LastCallNumList : $lastCallNumList BkNum : $bkNum"
                )

                if (cancelError == 1) {
                    // 장애 상태가 아닐 경우
                    if (callWinNum == ScreenInfoManager.instance.winNum) {
                        ScreenInfoManager.instance.setCallCancel(
                            cancelError,
                            cancelCallNum,
                            ticketWinID,
                            callWinID,
                            wait,
                            callWinNum,
                            lastCallNumList,
                            bkNum
                        ) 
                    } else {
                        // 다른 창구일 때 처리 (필요한 경우)
                    }
                } else {
                    // 장애 상태 처리
                    if (bkNum == ScreenInfoManager.instance.bkDisplay) {
                        // TODO: 장애 상태 처리 로직 추가
                    } else {
                        // 다른 백업 표시기일 때 처리 (필요한 경우)
                    }
                }
            }

            // 호출 횟수 설정 패킷
            ProtocolDefine.CALL_COLLECT_SET.value -> {
                val collectWinNum = recvPacket.integer                    // 창구 번호
                val collectNum = recvPacket.integer                    // 호출 횟수

                if (collectWinNum == ScreenInfoManager.instance.winNum) {
                    ScreenInfoManager.instance.collectNum = collectNum
                    LogFile.write("IQS Response CallCollectSet WinNum : $collectWinNum CollectNum : $collectNum")
                    commResultReceiver?.send(ProtocolDefine.CALL_COLLECT_SET.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            // 전산 장애 설정
            ProtocolDefine.ERROR_SET.value -> {
                val errorInfo = recvPacket.string                      // 전산 장애 설정 정보
                ScreenInfoManager.instance.setError(errorInfo)
                LogFile.write("IQS Response ErrorSet Data : $errorInfo")
                commResultReceiver?.send(ProtocolDefine.ERROR_SET.value.toInt(), bundleData)
            }

            // 공석 설정
            ProtocolDefine.PJT_SET.value -> {
                val pjtWinNum = recvPacket.integer // 창구 번호
                val pjt = recvPacket.integer // 공석 여부 BOOL

                if (pjtWinNum == ScreenInfoManager.instance.winNum) {
                    ScreenInfoManager.instance.pjt = pjt
                    LogFile.write("IQS Response PJTSet PJTWinNum : $pjtWinNum PJT : $pjt")
                    commResultReceiver?.send(ProtocolDefine.PJT_SET.value.toInt(), bundleData)
                } else if (pjtWinNum == ScreenInfoManager.instance.mainWinNum) {
                    LogFile.write("IQS Response MainPJTSet PJTWinNum : $pjtWinNum PJT : $pjt")
                    ScreenInfoManager.instance.mainPJT = pjt
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            // 예약 리스트 응답
            ProtocolDefine.RESERVE_LIST_RESPONSE.value -> {
                val mul = recvPacket.integer
                val reserveListStr = recvPacket.string
                ScreenInfoManager.instance.setReserveList(mul, reserveListStr) 
                LogFile.write("IQS Response ReserveListResponse Data : $reserveListStr")
                commResultReceiver?.send(ProtocolDefine.RESERVE_LIST_RESPONSE.value.toInt(), bundleData)

                // 2022.08.23 written by kshong
                // 사운드 디스플레이에 있는 로그 파일을 서버로 전송
                // - /iqs/log/* 밑의 로그 파일을 서버로 전송하는데 현재 날짜는 제외하고 전송
                // - 부팅 시 최초 한 번 전송하기 때문에 현재 날짜는 전송하지 않음
                uploadLogFileToServer() // TODO: 실제 구현 필요

                // 2022.07.21 dyyoon update 파일 정보 받아오기
                // ftp로 다운로드가 처리되던 것을 tcp로 방법을 변경
                // DownLoadFile()를 RequestIQS(protocolDefine.ReservUpdateInfoRequest)로 대체하여 처리
                // 231130, by HAHU  광고 업데이트 후 앱 업데이트. 표시기일 때만 받아 감. 음성 호출이나 보조 순번은 광고 필요 없음
                if (Const.CommunicationInfo.CALLVIEW_MODE == "0") {
                    requestIQS(ProtocolDefine.VIDEO_LIST_REQUEST.value)
                } else {
                    requestIQS(ProtocolDefine.RESERVE_UPDATE_INFO_REQUEST.value)
                }
            }

            // 예약 추가 요청 패킷
            ProtocolDefine.RESERVE_ADD_REQUEST.value -> {
                val reserveAdd = recvPacket.string
                ScreenInfoManager.instance.setAddReserve(reserveAdd)
                LogFile.write("IQS Response ReserveAddRequest Data : $reserveAdd")
                commResultReceiver?.send(ProtocolDefine.RESERVE_ADD_REQUEST.value.toInt(), bundleData)
            }

            // 예약 수정 요청 패킷
            ProtocolDefine.RESERVE_UPDATE_REQUEST.value -> {
                val reserveUpdate = recvPacket.string
                ScreenInfoManager.instance.setUpdateReserve(reserveUpdate)
                LogFile.write("IQS Response ReserveUpdateRequest Data : $reserveUpdate")
                commResultReceiver?.send(ProtocolDefine.RESERVE_UPDATE_REQUEST.value.toInt(), bundleData)
            }

            // 예약 취소 요청 패킷
            ProtocolDefine.RESERVE_CANCEL_REQUEST.value -> {
                val reserveCancel = recvPacket.string
                ScreenInfoManager.instance.setCancelReserve(reserveCancel)
                LogFile.write("IQS Response ReserveCancleRequest Data : $reserveCancel")
                commResultReceiver?.send(ProtocolDefine.RESERVE_CANCEL_REQUEST.value.toInt(), bundleData)
            }

            // 예약 도착 등록 요청 패킷
            ProtocolDefine.RESERVE_ARRIVE_REQUEST.value -> {
                val arriveData = recvPacket.string
                ScreenInfoManager.instance.setReserveArrive(arriveData)
                LogFile.write("IQS Response ReservArriveReqeust Data : $arriveData")
                commResultReceiver?.send(ProtocolDefine.RESERVE_ARRIVE_REQUEST.value.toInt(), bundleData)
            }

            // 예약 호출 요청 패킷
            ProtocolDefine.RESERVE_CALL_REQUEST.value -> {
                var nReservCall = 0
                val reserveCall = recvPacket.string
                nReservCall = ScreenInfoManager.instance.setCallReserve(reserveCall)

                if (nReservCall == ScreenInfoManager.instance.winNum || Const.CommunicationInfo.CALLVIEW_MODE == "3") {
                    LogFile.write("IQS Response ReservCallRequest Data : $reserveCall")
                    commResultReceiver?.send(ProtocolDefine.RESERVE_CALL_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            // 예약 재호출 요청 패킷
            ProtocolDefine.RESERVE_RE_CALL_REQUEST.value -> {
                val reserveReCall = recvPacket.string

                var nReservReCall = 0
                nReservReCall = ScreenInfoManager.instance.setCallReserve(reserveReCall)

                if (nReservReCall == ScreenInfoManager.instance.winNum || Const.CommunicationInfo.CALLVIEW_MODE == "3") {
                    LogFile.write("IQS Response ReservReCallRequest Data : $reserveReCall")
                    commResultReceiver?.send(ProtocolDefine.RESERVE_RE_CALL_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            // 직원 정보 갱신
            ProtocolDefine.TELLER_RENEW_REQUEST.value -> {
                val renewWinNum = recvPacket.integer
                val tellerNum = recvPacket.integer
                val tellerName = recvPacket.string

                if (renewWinNum == ScreenInfoManager.instance.winNum) {
                    LogFile.write("IQS Response TellerRenewRequest  WinNum : $renewWinNum Teller Num : $tellerNum TellerName : $tellerName")
                    ScreenInfoManager.instance.setRenewTeller(renewWinNum, tellerNum, tellerName)
                    commResultReceiver?.send(ProtocolDefine.TELLER_RENEW_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }
            // =====================================================================================
            //2022.07.21 dyyoon 업데이트 해야 되는지 확인
            // - 수신된 Packet 구조
            //   length(2byte) + id(2byte) + data(n byte)
            // -------------------------------------------------------------------------------------
            // - *.APK 또는 *.WAV 를 업데이트 하는 경우 처리 순서
            //   (1) Header 부(length(2byte) + id(2byte))는 AnalysisID()에서 처리되었음.
            //       따라서 아래의 부분에서 따로 처리할 것 없음.
            //   (2) 첫번째 Packet data 부분 처리
            //       1) Packet 구조 : command(4byte) + download될 file size(4byte) + file name(nbyte)
            //       2) command(update)가 1인 경우로 각 파일 사이스와 이름을 클래스 변수에 저장한다.
            //   (3) 두번째 Packet data 부분 부터 마지막 Packet 처리(command(update)가 2인 경우)
            //       1) Packet 구조 : command(4byte) + file 내용(nbyte)
            //       2) 첫번째 Packet에서 만든 파일에 파일 내용을 저장한다.
            //       3) 마지막 까지 저장하고 첫번째 수신한 파일 사이즈와 비교하여 같으면 정상 처리한다.
            // -------------------------------------------------------------------------------------
            // - update 명령어 의미
            //   (1) update 0 인 경우 : 다운로드할 파일이 없는 경우로 MainActivity 로 전환한다.
            //   (2) update 1 인 경우 : 다운로드할 파일의 첫번째 처리 부분이다.
            //   (3) update 2 인 경우 : 다운로드할 파일을 반복해서 처리하는 부분이다.
            //   (4) update 3 인 경우 : 실제 다운로드 파일의 크기를 가져올 수 없는 경우이다.
            //                         따라서, 사운드디스플레이 정상 작동을 위해 MainActivity 로 전환한다.
            // =====================================================================================
            ProtocolDefine.RESERVE_UPDATE_INFO_RESPONSE.value -> {
                val update = recvPacket.integer
                var downloadFileSize = 0
                var downloadFileName = ""
                when (update) {
                    0 -> {
                        // 다운로드할 파일이 없는 경우 MainActivity로 전환
                        LogFile.write("ReserveUpdateInfoResponse : update = $update. 다운로드할 파일이 없는 경우로 MainActivity 로 전환해야 한다.")
                        installAPKFile(InstallAction.FINISH, downloadFileName)
                        Log.d("update value is [0]")
                    }
                    1 -> {
                        // 다운로드할 파일의 첫 번째 처리 부분
                        downloadFileSize = recvPacket.integer
                        downloadFileName = recvPacket.string

                        LogFile.write("[파일다운로드 시작] ReserveUpdateInfoResponse : default ID = $protocolID, update = $update, fileSize = $downloadFileSize, fileName = $downloadFileName")

                        val fileExtension = downloadFileName.substringAfterLast(".", "").lowercase()
                        val downloadDir = when (fileExtension) {
                            "apk" -> Const.Path.DIR_PATCH
                            "wav" -> Const.Path.DIR_DOWNLOAD_SOUND
                            "jpg", "jpeg", "png", "mp4" -> Const.Path.DIR_DOWNLOAD_VIDEO
                            else -> {
                                Log.e("Unsupported file extension: $fileExtension")
                                return // 지원하지 않는 파일 형식인 경우 함수 종료
                            }
                        }

                        val deleteFilePath = downloadDir + downloadFileName
                        File(deleteFilePath).delete() // 파일 삭제

                        LogFile.write("ReserveUpdateInfoResponse : Delete fileName = $downloadFileName. 다운로드 처음 시작하는 것으로 해당 파일을 지운다.")

                        installAPKFile(InstallAction.SHOW_FILENAME, downloadFileName)
                        Log.d("update value is [1]")
                    }

                    2 -> {
                        // 다운로드할 파일을 반복해서 처리하는 부분
                        Log.d("update value is [2]")

                        recvPacket.getFileAndSave(downloadFileName)

                        val fileExtension = downloadFileName.substringAfterLast(".", "").lowercase()
                        val (sourceDir, targetDir) = when (fileExtension) {
                            "apk" -> Const.Path.DIR_PATCH to Const.Path.DIR_PATCH // apk는 같은 디렉토리에 저장되므로 sourceDir와 targetDir가 같습니다.
                            "wav" -> Const.Path.DIR_DOWNLOAD_SOUND to Const.Path.DIR_SOUND
                            "jpg", "jpeg", "png", "mp4" -> Const.Path.DIR_DOWNLOAD_VIDEO to Const.Path.DIR_VIDEO
                            else -> {
                                Log.e("Unsupported file extension: $fileExtension")
                                return // 지원하지 않는 파일 형식인 경우 함수 종료
                            }
                        }

                        val sizeCheckFile = File(sourceDir + downloadFileName)
                        if (sizeCheckFile.length().toInt() == downloadFileSize) {
                            copyFile(applicationContext, sourceDir + downloadFileName, targetDir + downloadFileName)

                            val sourceFile = File(sourceDir + downloadFileName)
                            sourceFile.delete()

                            if (fileExtension == "apk") {
                                installAPKFile(InstallAction.INSTALL, downloadFileName)
                            }
                        }
                    }
                    3 -> {
                        // 업데이트를 해야 하지만, 실제 업데이트 파일이 존재하지 않는 경우
                        LogFile.write("ReserveUpdateInfoResponse : update = $update. 실제 다운로드 파일의 크기를 가져올 수 없는 경우.")
                        installAPKFile(InstallAction.FINISH, downloadFileName)
                        Log.d("update value is [3]")
                    }
                    else -> {
                        // 그 밖의 경우 처리
                        LogFile.write("ReserveUpdateInfoResponse : update = $update. 그밖의 경우로 현재 정의된 값이 없어서, else 인 경우는 발생되지 않음.")
                        Log.d("update value is [other value]")
                    }
                }
            }
            else -> {
                Log.d("default ID  :  $protocolID")
                LogFile.write("IQS Response default ID  :  $protocolID")
            }
        }
    }

    private enum class InstallAction {
        INSTALL, // APK 파일 다운로드 완료 및 설치 시작
        FINISH, // APK 파일 다운로드 또는 설치 실패
        SHOW_FILENAME // APK 파일 다운로드 시작 및 파일 이름 표시
    }

    private fun installAPKFile(action: InstallAction, downloadFileName: String = "") {
        val startProtocol = ProtocolDefine.START_PATCH.value
        val endProtocol = ProtocolDefine.END_PATCH.value

        when (action) {
            InstallAction.INSTALL -> {
                val sendBundle = Bundle().apply {
                    putString("FileName", downloadFileName)
                }
                commResultReceiver?.send(startProtocol.toInt(), sendBundle)

                val recvBundle = Bundle().apply {
                    putBoolean("result", true)
                    putString("FileName", downloadFileName)
                }
                commResultReceiver?.send(endProtocol.toInt(), recvBundle)
            }

            InstallAction.FINISH -> {
                val sendBundle = Bundle().apply {
                    putBoolean("result", false)
                }
                commResultReceiver?.send(endProtocol.toInt(), sendBundle)
            }

            InstallAction.SHOW_FILENAME -> {
                val sendBundle = Bundle().apply {
                    putString("FileName", downloadFileName)
                }
                commResultReceiver?.send(startProtocol.toInt(), sendBundle)
            }
        }
    }

    private fun uploadLogFileToServer() {
        val logFilePath = Const.Path.DIR_LOG
        val currentDateFileName = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        Log.d("uploadLogFileToServer() 시작하기")
        LogFile.write("uploadLogFileToServer() 시작하기")

        val logDir = File(logFilePath)
        val fileList = logDir.listFiles()?.filter { it.isFile } // 파일만 필터링

        // fileList?.reversed()를 사용하여 역순으로 파일 처리 (최신 파일부터)
        fileList?.reversed()?.forEach { file ->
            val fileName = file.name
            // 현재 날짜 파일은 제외
            if (fileName != "$currentDateFileName.txt") {
                Log.d("uploadLogFileToServerSub() : 현재날짜 = $currentDateFileName, 업로드 파일 = $fileName")
                LogFile.write("uploadLogFileToServerSub() : 현재날짜 = $currentDateFileName, 업로드 파일 = $fileName")
                uploadLogFileToServerSub(fileName)
            }
        }
    }


    private fun uploadLogFileToServerSub(fileName: String) {
        val code = ProtocolDefine.UPLOAD_LOG_FILE_TO_SERVER.value

        Log.d("uploadLogFileToServerSub() 시작하기 : 업로드 파일이름 = $fileName")
        LogFile.write("uploadLogFileToServerSub() 시작하기 : 업로드 파일이름 = $fileName")

        try {
            val uploadFile = File(Const.Path.DIR_LOG + fileName)

            if (!uploadFile.exists()) {
                Log.d("uploadLogFileToServerSub() : 실제 파일이 없어 리턴")
                LogFile.write("uploadLogFileToServerSub() : 실제 파일이 없어 리턴")
                return
            }

            FileInputStream(uploadFile).use { fis ->
                val readBuffer = ByteArray(1024 * 7) // 한 번에 읽어서 전송하기 위한 길이
                var bytesRead: Int

                Log.d("uploadLogFileToServerSub() 데이터 전송: 업로드 파일이름 = $fileName")
                while (fis.read(readBuffer).also { bytesRead = it } != -1) {
                    // ================================================================================================================
                    // 전송할 팩킷 구조
                    // datasize(2byte) + code(2byte) + sFileName(n byte) + 구분자(Null 1byte) + File contents(nReadLength와 같거나 작은값) + 구분자(Null 1byte)
                    // ----------------------------------------------------------------------------------------------------------------
                    val dataSize = (fileName.length + 1 + bytesRead + 1).toShort()

                    val sendByteBuffer = ByteBuffer.allocate(dataSize.toInt() + 4).apply {
                        order(ByteOrder.LITTLE_ENDIAN)
                            .putShort(dataSize)
                            .putShort(code)
                            .put(fileName.toByteArray())
                            .put(0x00.toByte())
                            .put(readBuffer, 0, bytesRead)
                            .put(0x00.toByte())
                    }

                    SendProtocol(sendByteBuffer).start()
                }

                // 파일 읽기가 끝났으므로 해당 파일 삭제
                uploadFile.delete()
            }
        } catch (e: Exception) {
            Log.e("uploadLogFileToServerSub() 예외 발생: ${e.message}", e)
        }
    }

}