package com.kct.iqsdisplayer.service

import SendBufferClass
import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.data.packet.receive.toAcceptAuthResponse
import com.kct.iqsdisplayer.data.packet.receive.toCrowdedRequest
import com.kct.iqsdisplayer.data.packet.receive.toPausedWorkRequest
import com.kct.iqsdisplayer.data.packet.receive.toInfoMessageRequest
import com.kct.iqsdisplayer.data.packet.receive.toReserveListResponse
import com.kct.iqsdisplayer.data.packet.receive.toRestartRequest
import com.kct.iqsdisplayer.data.packet.receive.toTellerRenewRequest
import com.kct.iqsdisplayer.data.packet.receive.toWaitResponse
import com.kct.iqsdisplayer.data.packet.receive.toWinResponse
import com.kct.iqsdisplayer.data.toCallRequest
import com.kct.iqsdisplayer.data.toTellerData
import com.kct.iqsdisplayer.network.ConnectFTP
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.copyFile
import java.io.File
import java.io.FileInputStream
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("IQSComClass onCreate")
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
            Log.i("FTP IP : ${Const.ConnectionInfo.IQS_IP} FTP PORT : ${Const.ConnectionInfo.FTP_PORT}")
            connectFTP = ConnectFTP()
            status = connectFTP?.ftpConnect(
                Const.ConnectionInfo.IQS_IP,
                Const.ConnectionInfo.FTP_ID,
                Const.ConnectionInfo.FTP_PW,
                Const.ConnectionInfo.FTP_PORT
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
                        Log.d("FTP DownLoad Start Category : $category")

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
                        Log.d("FTP DownLoad Result : $result mReConnect : ${connectFTP?.reConnect}")

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
            val serverLogFolderPath = "${Const.Path.SUB_PATH_LOG}Display${ScreenInfo.instance.winNum}/"
            Log.i("Try Send Log File ($serverLogFolderPath)")

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
                        uploadLogFileToServerSub(fileName)
                    }
                }
            } catch (e: Exception) {
                onDestroy()
                Log.e("Failed Send Log Files (${e.message})")
            }
        }
        /*
        override fun run() {
            val serverLogFolderPath = "${Const.Path.SUB_PATH_LOG}Display${ScreenInfoManager.instance.winNum}/"
            Log.d("Try Send Log File (${serverLogFolderPath})")

            try {
                // DisplayLog 디렉토리 생성
                if (connectFTP?.ftpCreateDirectory(Const.Path.SUB_PATH_LOG) == true) {
                    Log.d("Create dir ${Const.Path.SUB_PATH_LOG})")
                }

                // 내 표시기 번호 하위 디렉토리 생성
                if (connectFTP?.ftpCreateDirectory(serverLogFolderPath) == true) {
                    Log.d("Create dir")
                    Log.d("Create dir (${serverLogFolderPath})")
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
                Log.d("Failed Send Log Files (${e.message})")
            }
        }*/
    }

    /**
     * IQS에 요청하는 ByteBuffer 값을 얻는 함수
     * @param code IQS로 전송할 프로토콜 ID
     */
    private fun requestIQS(code: Short) {
        val protocolName = ProtocolDefine.entries.find { it.value == code }?.name ?: "Unknown"
        Log.i("IQS Request $protocolName")  // 실제 protocolName 출력

        val sendByteBuffer: ByteBuffer? = when (code) {
            ProtocolDefine.WAIT_REQUEST.value       -> SendBufferClass().waitRequest()      // 대기인수 정보 요청 패킷
            ProtocolDefine.KEEP_ALIVE_REQUEST.value -> null                                 // KEEPALIVE 요청
            //ProtocolDefine.INSTALL_INFO.value       -> null                                 // 설치 정보 패킷
            //ProtocolDefine.SUB_SCREEN_REQUEST.value -> null                                 // 보조 표시정보 요청
            ProtocolDefine.MEDIA_LIST_REQUEST.value -> SendBufferClass().videoListRequest() // 동영상 리스트 요청
            ProtocolDefine.VIDEO_DOWNLOAD_REQUEST.value -> SendBufferClass().videoDownLoadRequest() // 231211, by HAHU  광고파일 요청
            ProtocolDefine.CONNECT_SUCCESS.value -> {
                val acceptAuthRequest = SendBufferClass().acceptAuthRequest()
                if (acceptAuthRequest != null) {
                    acceptAuthRequest
                } else {
                    Log.d("IP or Mac is null")
                    commResultReceiver?.let { stopSelf() }
                    null
                }
            }

            ProtocolDefine.RESERVE_LIST_REQUEST.value -> SendBufferClass().reserveList()      // 상담 예약 관련 추가 ADD sblee 19-12-03
            ProtocolDefine.UPDATE_INFO_REQUEST.value -> SendBufferClass().appVersionRequest()// 2022.07.21 dyyoon 업데이트 정보 요청 add
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
    private fun analysisID(headerBytes: ByteArray, dataBytes: ByteArray) {
        val packet = Packet(headerBytes, dataBytes)
        val protocolID = packet.getId()
        val size = packet.getLength()
        val bundleData = Bundle()

        val protocolName = ProtocolDefine.entries.find { it.value == protocolID }?.name ?: "Unknown"
        Log.d("IQS Response $protocolName") // 실제 protocolName 출력

        when (protocolID) {
            ProtocolDefine.CONNECT_SUCCESS.value -> {
                // 접속 성공 응답
                requestIQS(ProtocolDefine.CONNECT_SUCCESS.value)
                SendKEEPALIVE().start()
            }
            // 접속 거부 응답
            ProtocolDefine.CONNECT_REJECT.value -> {}
            // 접속 승인 응답
            ProtocolDefine.ACCEPT_AUTH_REQUEST.value -> {
                Log.d("IQS Response AcceptAuthRequest data : ${packet.string}")
            }

            ProtocolDefine.ACCEPT_AUTH_RESPONSE.value -> {
                val data = packet.toAcceptAuthResponse()

                ScreenInfo.instance.setTellerInfo(data.tellerInfo)
                Log.d("SetTellerInfo")

                ScreenInfo.instance.setWinList(data.winIdList, data.winNameList, data.waitingNumList)
                Log.d("SetWinList")

                ScreenInfo.instance.setScreenInfo(data)

                Log.d("IQS Response $data")

                commResultReceiver?.send(ProtocolDefine.ACCEPT_AUTH_RESPONSE.value.toInt(), bundleData)

                requestIQS(ProtocolDefine.RESERVE_LIST_REQUEST.value)
            }

            ProtocolDefine.WAIT_RESPONSE.value -> {
                // 대기 인수 정보 응답 패킷
                val data = packet.toWaitResponse()
                Log.d("$data")

                if (data.winNum == ScreenInfo.instance.winID) {
                    // 같은 창구일 때
                    ScreenInfo.instance.setWaitResponse(data.ticketNum, data.waitNum)
                    Log.d("IQS Response $data")
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

                val data = packet.toCallRequest()

                var result = false
                Log.d("IQS Response $data")

                if (Const.ConnectionInfo.CALLVIEW_MODE == "2" || Const.ConnectionInfo.CALLVIEW_MODE == "3") { // 보조 순번
                    result = ScreenInfo.instance.setCallNum(data)
                    if (result) {
                        bundleData.putString("Display", "Main")
                        commResultReceiver?.send(resultCode.toInt(), bundleData)
                    }
                } else {
                    if (ScreenInfo.instance.pjt.value == 0) { // 사용 중
                        if (data.errorStatus == 0) {
                            // 정상일 때
                            if (data.callWinNum == ScreenInfo.instance.winNum) {
                                result = ScreenInfo.instance.setCallNum(data)
                                if (result) {
                                    bundleData.putString("Display", "Main")
                                    commResultReceiver?.send(resultCode.toInt(), bundleData)
                                }
                            } else {
                                Log.d("CallWinNum != screenInfo.getWinNum()   --->   ${data.callWinNum}, ${ScreenInfo.instance.winNum}")
                            }

                        } else {
                            // 장애 상황
                            if (data.bkDisplay == ScreenInfo.instance.winNum) {
                                result = ScreenInfo.instance.setCallNum(data)
                                if (result) {
                                    bundleData.putString("Display", "Bk")
                                    commResultReceiver?.send(resultCode.toInt(), bundleData)
                                }
                            }
                        }
                    } else {
                        // 공석 상태
                        Log.d("$resultCode but Display is PJT")
                    }
                }
            }

//            ProtocolDefine.DISPLAY_INFO.value -> {
//                // 화면 표시기 정보 패킷
//                val data = packet.string
//                ScreenInfo.instance.setDisplayInfo(data)
//                commResultReceiver?.send(ProtocolDefine.DISPLAY_INFO.value.toInt(), bundleData)
//            }

            ProtocolDefine.PAUSED_WORK_REQUEST.value -> {
                // 부재 정보 요청
                val data = packet.toPausedWorkRequest()

                if (data.winNum == ScreenInfo.instance.winNum) {
                    ScreenInfo.instance.setEmpty(data.emptyFlag, data.emptyMsg)
                    Log.d("IQS Response $data")
                    commResultReceiver?.send(ProtocolDefine.PAUSED_WORK_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            ProtocolDefine.INFO_MESSAGE_REQUEST.value -> {
                // 안내 메시지 설정 요청
                val data = packet.toInfoMessageRequest()

                if (data.infoMessageWinNum == ScreenInfo.instance.winNum) {
                    Log.d("infoMessage : $data")
                    ScreenInfo.instance.ment = data.infoMessage
                    commResultReceiver?.send(ProtocolDefine.INFO_MESSAGE_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            ProtocolDefine.TELLER_LIST.value -> {
                // 직원 정보

                val data = packet.string          // 직원 설정 정보 리스트
                Log.d("IQS Response Teller Data  : $data")
                val tellerList = packet.toTellerData()
                ScreenInfo.instance.setTellerList(tellerList)
                commResultReceiver?.send(ProtocolDefine.TELLER_LIST.value.toInt(), bundleData)
            }

            // 시스템 종료 패킷
            ProtocolDefine.SYSTEM_OFF.value -> {
                commResultReceiver?.send(ProtocolDefine.SYSTEM_OFF.value.toInt(), bundleData)
            }

            // 동영상 설정 패킷
            ProtocolDefine.VIDEO_SET.value -> {
                val videoInfo = packet.string
                ScreenInfo.instance.setVideoInfo(videoInfo)
                Log.d("IQS Response VideoSet Data : $videoInfo")
                try {
                    commResultReceiver?.send(ProtocolDefine.VIDEO_SET.value.toInt(), bundleData)
                    val list = arrayOf("Video")
                    val downLoadFTPFile = DownLoadFTPFile(list, false)
                    downLoadFTPFile.setFileList(ScreenInfo.instance.adFileList)
                    Thread(downLoadFTPFile).start()
                } catch (e: Exception) {
                    Log.e("Failed VideoSet")
                }
            }

//            ProtocolDefine.VOLUME_TEST.value -> {
//                // 볼륨 테스트 패킷
//                val volumeInfo = packet.string
//                try {
//                    val testVolume = ScreenInfo.instance.setVolumeTest(volumeInfo)
//
//                    Log.d("IQS Response VolumTest Data VolumeWin : $testVolume")
//
//                    if (ScreenInfo.instance.winNum == testVolume.volumeWin) {
//                        // 액티비티로 전달
//                        commResultReceiver?.send(ProtocolDefine.VOLUME_TEST.value.toInt(), bundleData)
//                    } else {
//                        // 다른 창구일 때 처리 (필요한 경우)
//                    }
//                } catch (e: Exception) {
//                    Log.e("Failed Volume Test")
//                }
//            }
//
//            // 음성 설정 패킷
//            ProtocolDefine.SOUND_SET.value -> {
//                val soundSetData = packet.string
//                ScreenInfo.instance.soundSet(soundSetData)
//                Log.d("IQS Response SoundSetData Data : $soundSetData")
//                commResultReceiver?.send(ProtocolDefine.SOUND_SET.value.toInt(), bundleData)
//            }

            // 재시작 요청
            ProtocolDefine.RESTART_REQUEST.value -> {
                val data = packet.toRestartRequest()

                if (data.restartWinNum == ScreenInfo.instance.winNum) {
                    Const.ConnectionInfo.MODE = data.mode
                    Log.d("IQS Response $data")
                    commResultReceiver?.send(ProtocolDefine.RESTART_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            ProtocolDefine.CROWDED_REQUEST.value -> {
                // 혼잡 요청
                val data = packet.toCrowdedRequest()

                if (data.crowdedWinID == ScreenInfo.instance.winID) {
                    ScreenInfo.instance.setCrowed(data.isCrowded, data.crowdedMsg)
                    Log.d("IQS Response $data")
                    commResultReceiver?.send(ProtocolDefine.CROWDED_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            ProtocolDefine.WIN_RESPONSE.value -> {
                // 창구 응답
                val data = packet.toWinResponse()

                ScreenInfo.instance.setWinList(data.winIds, data.winNames, data.waitNums)
                Log.d("IQS Response $data")
                commResultReceiver?.send(ProtocolDefine.WIN_RESPONSE.value.toInt(), bundleData)
            }

            // KEEPALIVE 응답
            ProtocolDefine.KEEP_ALIVE_RESPONSE.value -> {}

            // 보조 표시 정보 응답
//            ProtocolDefine.SUB_SCREEN_RESPONSE.value -> {
//                val waitListInfo = packet.string                   // 대기 인수 정보
//                Log.d("IQS Response SubScreenResponse Data : $waitListInfo")
//            }

            // 배경 음악 정보 패킷
//            ProtocolDefine.BGM_INFO.value -> {
//                val bgmInfo = packet.string                        // 배경음악 정보
//                Log.d("IQS Response BGMInfo Data : $bgmInfo")
//            }

            // 동영상 리스트 응답
            ProtocolDefine.MEDIA_LIST_RESPONSE.value -> {
                val videoList = packet.string                      // 동영상 리스트
                Log.d("IQS Response VideoListResponse Data : $videoList")

                // 231130, by HAHU  동영상 다운로드 후 앱 업데이트
                requestIQS(ProtocolDefine.UPDATE_INFO_REQUEST.value)

            }

            // 설치 정보 패킷
            /*ProtocolDefine.INSTALL_INFO.value -> {}

            // 호출 취소 요청
            ProtocolDefine.CALL_CANCEL.value -> {
                val data = packet.toCallCancelData()

                Log.d("IQS Response $data")

                if (data.cancelError == 1) {
                    // 장애 상태가 아닐 경우
                    if (data.callWinNum == ScreenInfo.instance.winNum) {
                        ScreenInfo.instance.setCallCancel(data)
                    } else {
                        // 다른 창구일 때 처리 (필요한 경우)
                    }
                } else {
                    // 장애 상태 처리
                    if (data.bkNum == ScreenInfo.instance.bkDisplay) {
                        // TODO: 장애 상태 처리 로직 추가
                    } else {
                        // 다른 백업 표시기일 때 처리 (필요한 경우)
                    }
                }
            }

            // 호출 횟수 설정 패킷
            ProtocolDefine.CALL_COLLECT_SET.value -> {
                val data = packet.toCallCollectSetData()

                if (data.collectWinNum == ScreenInfo.instance.winNum) {
                    ScreenInfo.instance.collectNum = data.collectNum
                    Log.d("IQS Response $data")
                    commResultReceiver?.send(ProtocolDefine.CALL_COLLECT_SET.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            // 전산 장애 설정
            ProtocolDefine.ERROR_SET.value -> {
                val errorInfo = packet.string                      // 전산 장애 설정 정보
                ScreenInfo.instance.setError(errorInfo)
                Log.d("IQS Response ErrorSet Data : $errorInfo")
                commResultReceiver?.send(ProtocolDefine.ERROR_SET.value.toInt(), bundleData)
            }

            // 공석 설정
            ProtocolDefine.PJT_SET.value -> {
                val data = packet.toPJTSetData()
                Log.d("IQS Response $data")
                when (data.pjtWinNum) {
                    ScreenInfo.instance.winNum -> {
                        ScreenInfo.instance.updatePjt(data.pjt)
                        commResultReceiver?.send(ProtocolDefine.PJT_SET.value.toInt(), bundleData)
                    }
                    //쓰는곳이 없음
//                    ScreenInfo.instance.mainWinNum -> {
//                        ScreenInfo.instance.mainPJT = data.pjt
//                    }
                    else -> {
                        Log.w("패킷 ProtocolDefine.PJT_SET의 data.pjtWinNum[${data.pjtWinNum}], ScreenInfoManager.instance.winNum[${ScreenInfo.instance.winNum}], ScreenInfoManager.instance.mainWinNum[${ScreenInfo.instance.mainWinNum}]")
                    }
                }
            }*/

            // 예약 리스트 응답
            ProtocolDefine.RESERVE_LIST_RESPONSE.value -> {
                val data = packet.toReserveListResponse()
                ScreenInfo.instance.setReserveList(data.mul, data.reserveListStr)
                Log.d("IQS Response $data")
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
                if (Const.ConnectionInfo.CALLVIEW_MODE == "0") {
                    requestIQS(ProtocolDefine.MEDIA_LIST_REQUEST.value)
                } else {
                    requestIQS(ProtocolDefine.UPDATE_INFO_REQUEST.value)
                }
            }

            // 예약 추가 요청 패킷
            ProtocolDefine.RESERVE_ADD_REQUEST.value -> {
                val reserveAdd = packet.string
                ScreenInfo.instance.setAddReserve(reserveAdd)
                Log.d("IQS Response ReserveAddRequest Data : $reserveAdd")
                commResultReceiver?.send(ProtocolDefine.RESERVE_ADD_REQUEST.value.toInt(), bundleData)
            }

            // 예약 수정 요청 패킷
            ProtocolDefine.RESERVE_UPDATE_REQUEST.value -> {
                val reserveUpdate = packet.string
                ScreenInfo.instance.setUpdateReserve(reserveUpdate)
                Log.d("IQS Response ReserveUpdateRequest Data : $reserveUpdate")
                commResultReceiver?.send(ProtocolDefine.RESERVE_UPDATE_REQUEST.value.toInt(), bundleData)
            }

            // 예약 취소 요청 패킷
            ProtocolDefine.RESERVE_CANCEL_REQUEST.value -> {
                val reserveCancel = packet.string
                ScreenInfo.instance.setCancelReserve(reserveCancel)
                Log.d("IQS Response ReserveCancleRequest Data : $reserveCancel")
                commResultReceiver?.send(ProtocolDefine.RESERVE_CANCEL_REQUEST.value.toInt(), bundleData)
            }

            // 예약 도착 등록 요청 패킷
            ProtocolDefine.RESERVE_ARRIVE_REQUEST.value -> {
                val arriveData = packet.string
                ScreenInfo.instance.setReserveArrive(arriveData)
                Log.d("IQS Response ReservArriveReqeust Data : $arriveData")
                commResultReceiver?.send(ProtocolDefine.RESERVE_ARRIVE_REQUEST.value.toInt(), bundleData)
            }

            // 예약 호출 요청 패킷
            ProtocolDefine.RESERVE_CALL_REQUEST.value -> {
                var nReserveCall = 0
                val reserveCall = packet.string
                nReserveCall = ScreenInfo.instance.setCallReserve(reserveCall)

                if (nReserveCall == ScreenInfo.instance.winNum || Const.ConnectionInfo.CALLVIEW_MODE == "3") {
                    Log.d("IQS Response ReservCallRequest Data : $reserveCall")
                    commResultReceiver?.send(ProtocolDefine.RESERVE_CALL_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            // 예약 재호출 요청 패킷
            ProtocolDefine.RESERVE_RE_CALL_REQUEST.value -> {
                val reserveReCall = packet.string

                var nReserveReCall = 0
                nReserveReCall = ScreenInfo.instance.setCallReserve(reserveReCall)

                if (nReserveReCall == ScreenInfo.instance.winNum || Const.ConnectionInfo.CALLVIEW_MODE == "3") {
                    Log.d("IQS Response ReservReCallRequest Data : $reserveReCall")
                    commResultReceiver?.send(ProtocolDefine.RESERVE_RE_CALL_REQUEST.value.toInt(), bundleData)
                } else {
                    // 다른 창구일 때 처리 (필요한 경우)
                }
            }

            // 직원 정보 갱신
            ProtocolDefine.TELLER_RENEW_REQUEST.value -> {
                val data = packet.toTellerRenewRequest()

                if (data.renewWinNum == ScreenInfo.instance.winNum) {
                    Log.d("IQS Response $data")
                    ScreenInfo.instance.setRenewTeller(data.renewWinNum, data.tellerNum, data.tellerName)
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
            ProtocolDefine.UPDATE_INFO_RESPONSE.value -> {
                val update = packet.integer
                var downloadFileSize = 0
                var downloadFileName = ""
                when (update) {
                    0 -> {
                        // 다운로드할 파일이 없는 경우 MainActivity로 전환
                        Log.d("ReserveUpdateInfoResponse : update = $update. 다운로드할 파일이 없는 경우로 MainActivity 로 전환해야 한다.")
                        installAPKFile(InstallAction.FINISH, downloadFileName)
                        Log.d("update value is [0]")
                    }
                    1 -> {
                        // 다운로드할 파일의 첫 번째 처리 부분
                        downloadFileSize = packet.integer
                        downloadFileName = packet.string

                        Log.d("[파일다운로드 시작] ReserveUpdateInfoResponse : default ID = $protocolID, update = $update, fileSize = $downloadFileSize, fileName = $downloadFileName")

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

                        Log.d("ReserveUpdateInfoResponse : Delete fileName = $downloadFileName. 다운로드 처음 시작하는 것으로 해당 파일을 지운다.")

                        installAPKFile(InstallAction.SHOW_FILENAME, downloadFileName)
                        Log.d("update value is [1]")
                    }

                    2 -> {
                        // 다운로드할 파일을 반복해서 처리하는 부분
                        Log.d("update value is [2]")

                        packet.getFileAndSave(downloadFileName)

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
                        Log.d("ReserveUpdateInfoResponse : update = $update. 실제 다운로드 파일의 크기를 가져올 수 없는 경우.")
                        installAPKFile(InstallAction.FINISH, downloadFileName)
                        Log.d("update value is [3]")
                    }
                    else -> {
                        // 그 밖의 경우 처리
                        Log.d("ReserveUpdateInfoResponse : update = $update. 그밖의 경우로 현재 정의된 값이 없어서, else 인 경우는 발생되지 않음.")
                        Log.d("update value is [other value]")
                    }
                }
            }
            else -> {
                Log.d("default ID  :  $protocolID")
                Log.d("IQS Response default ID  :  $protocolID")
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

        val logDir = File(logFilePath)
        val fileList = logDir.listFiles()?.filter { it.isFile } // 파일만 필터링

        // fileList?.reversed()를 사용하여 역순으로 파일 처리 (최신 파일부터)
        fileList?.reversed()?.forEach { file ->
            val fileName = file.name
            // 현재 날짜 파일은 제외
            if (fileName != "$currentDateFileName.txt") {
                Log.d("uploadLogFileToServerSub() : 현재날짜 = $currentDateFileName, 업로드 파일 = $fileName")
                uploadLogFileToServerSub(fileName)
            }
        }
    }


    private fun uploadLogFileToServerSub(fileName: String) {
        val code = ProtocolDefine.UPLOAD_LOG_FILE_TO_SERVER.value

        Log.d("uploadLogFileToServerSub() 시작하기 : 업로드 파일이름 = $fileName")

        try {
            val uploadFile = File(Const.Path.DIR_LOG + fileName)

            if (!uploadFile.exists()) {
                Log.d("uploadLogFileToServerSub() : 실제 파일이 없어 리턴")
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