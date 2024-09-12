package com.kct.iqsdisplayer.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.CallSoundManager
import com.kct.iqsdisplayer.common.CommResultReceiver
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.Const.ConnectionInfo.loadCommunicationInfo
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.common.SharedViewModel
import com.kct.iqsdisplayer.common.UpdateManager
import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.data.packet.receive.AcceptAuthResponse
import com.kct.iqsdisplayer.data.packet.receive.MediaListResponse
import com.kct.iqsdisplayer.data.packet.receive.ReserveListResponse
import com.kct.iqsdisplayer.data.packet.receive.UpdateInfoResponse
import com.kct.iqsdisplayer.data.packet.send.AcceptAuthRequest
import com.kct.iqsdisplayer.data.packet.send.MediaListRequest
import com.kct.iqsdisplayer.data.packet.send.ReserveListRequest
import com.kct.iqsdisplayer.data.packet.send.UpdateInfoRequest
import com.kct.iqsdisplayer.databinding.ActivityMainBinding
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.network.TCPClient
import com.kct.iqsdisplayer.ui.FragmentFactory.Index
import com.kct.iqsdisplayer.ui.FragmentFactory.replaceFragment
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.copyFile
import com.kct.iqsdisplayer.util.getLocalIpAddress
import com.kct.iqsdisplayer.util.getMacAddress
import com.kct.iqsdisplayer.util.installSilent
import com.kct.iqsdisplayer.util.makeDir
import com.kct.iqsdisplayer.util.setFullScreen
import com.kct.iqsdisplayer.util.setPreference
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * 모든 로직은 HardDisK의 권한을 획득한 후에 동작하도록 한다.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var tcpClient: TCPClient

    private val sharedViewModel: SharedViewModel by viewModels()

    private var commResultReceiver = CommResultReceiver(Handler(Looper.getMainLooper()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        setFullScreen()

        FragmentFactory.setActivity(this)

        startSystem()
    }

    override fun onDestroy() {
        super.onDestroy()
        tcpClient.onDestroy()
    }


    private fun startSystem() {
        if(checkStorage()) {
            //Storage 를 사용 할 준비가 되었다면 접속환경 설정 및 TCP접속부터 시작한다.
            restoreSharedPreferencesFiles()

            initConstInfo()

            tcpClient = TCPClient(Const.ConnectionInfo.IQS_IP, Const.ConnectionInfo.IQS_PORT)
            tcpClient.setOnTcpEventListener(tcpEventListener)
        }
    }

    private fun checkStorage(): Boolean {
        Log.d("OS의 API_LEVEL[${Build.VERSION.SDK_INT}]" )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            requestAllFilesAccessPermission()
            return false
        }

        //isExternalStorageManager()를 통과하면 Root디렉토리를 사용 할 수 있으나 확인차 체크함.
        if(Environment.getExternalStorageDirectory()?.absolutePath == null){
            finishApp("외부 저장소 경로(Root)를 가져오는 데 실패했습니다.")
            return false
        }

        if(makeDir(Const.Path.DIR_IQS) == null) {
            finishApp("PATH[${Const.Path.DIR_IQS}] 폴더 생성 실패")
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestAllFilesAccessPermission() {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.fromParts("package", packageName, null)
            requestAllFilesAccessLauncher.launch(intent)
        }
    }

    private val requestAllFilesAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // 모든 파일에 대한 접근 권한이 부여됨
                Log.e("isExternalStorageManager startSystem START FRAGMENT_INIT")
                startSystem()
            } else {
                // 모든 파일에 대한 접근 권한이 거부됨, 앱이 동작 할 수 없음. 앱종료.
                finishApp("모든 파일에 대한 접근 권한이 거부됨.")
            }
        }
    }

    fun finishApp(message: String = "앱 종료 호출") {
        Log.e(message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finishAffinity()
    }

    private fun initConstInfo() {
        Const.Path.DIR_SHARED_PREFS = "${filesDir.absolutePath}/shared_prefs/"
        getSharedPreferences(Const.Name.PREF_DISPLAYER_SETTING, Context.MODE_PRIVATE).loadCommunicationInfo()

        Const.ConnectionInfo.DISPLAY_IP = getLocalIpAddress()
        Const.ConnectionInfo.DISPLAY_MAC = getMacAddress()

        setPreference(Const.Name.PREF_DISPLAYER_SETTING, Const.Key.DisplayerSetting.IQS_IP, Const.ConnectionInfo.IQS_IP)
        setPreference(Const.Name.PREF_DISPLAYER_SETTING, Const.Key.DisplayerSetting.IQS_PORT, Const.ConnectionInfo.IQS_PORT)
    }

    private fun restoreSharedPreferencesFiles() {
        var prefFileName = Const.Name.getPrefDisplayerSettingName()
        var sourcePath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"
        var destPath: String

        val prefDisplayerSetting = File(sourcePath)

        if (!prefDisplayerSetting.exists()) {
            sourcePath = "${Const.Path.DIR_IQS}$prefFileName"
            destPath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"

            if(File(sourcePath).exists()) copyFile(this, sourcePath, destPath)

        } else {
            Log.d("설정정보파일 정상[${Const.Name.PREF_DISPLAYER_SETTING}]")
        }

        prefFileName = Const.Name.getPrefDisplayInfoName()
        sourcePath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"

        val prefDisplayInfo = File(sourcePath)

        if (!prefDisplayInfo.exists()) {
            sourcePath = "${Const.Path.DIR_IQS}$prefFileName"
            destPath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"

            if(File(sourcePath).exists()) copyFile(this, sourcePath, destPath)

        } else {
            Log.d("화면정보파일 정상[${Const.Name.PREF_DISPLAY_INFO}]")
        }
    }

//=====================================================================================================
// 실제 로직 시작
//=====================================================================================================
    private val tcpEventListener = object : TCPClient.OnTcpEventListener {
        override fun onConnected() {
            Log.d("onConnected")
        }

        override fun onReceivedData(protocolDefine: ProtocolDefine, receivedData: BaseReceivePacket) {
            Log.d("${protocolDefine}$receivedData")
            when(protocolDefine) {
                ProtocolDefine.CONNECT_SUCCESS -> onConnectSuccess()
                ProtocolDefine.CONNECT_REJECT -> Log.e("접속 실패 - protocol:${protocolDefine.name}[${protocolDefine.value}]")
                ProtocolDefine.ACCEPT_AUTH_RESPONSE -> {
                    onAcceptAuthResponse(receivedData)
                    Log.d("업데이트 정보 요청")
                    tcpClient.sendProtocol(UpdateInfoRequest().toByteBuffer())
                }
                ProtocolDefine.WAIT_RESPONSE -> TODO()
                ProtocolDefine.CALL_REQUEST -> TODO()
                ProtocolDefine.RE_CALL_REQUEST -> TODO()
                ProtocolDefine.EMPTY_REQUEST -> TODO()
                ProtocolDefine.INFO_MESSAGE_REQUEST -> TODO()
                ProtocolDefine.TELLER_LIST -> TODO()
                ProtocolDefine.SYSTEM_OFF -> TODO()
                ProtocolDefine.RESTART_REQUEST -> TODO()
                ProtocolDefine.RESTART_RESPONSE -> TODO()
                ProtocolDefine.CROWDED_REQUEST -> TODO()
                ProtocolDefine.WIN_REQUEST -> TODO()
                ProtocolDefine.WIN_RESPONSE -> TODO()
                ProtocolDefine.MEDIA_LIST_RESPONSE -> onMediaListResponse(receivedData)
                ProtocolDefine.RESERVE_LIST_RESPONSE -> onReserveListResponse(receivedData)
                ProtocolDefine.RESERVE_ADD_REQUEST -> TODO()
                ProtocolDefine.RESERVE_ADD_RESPONSE -> TODO()
                ProtocolDefine.RESERVE_UPDATE_REQUEST -> TODO()
                ProtocolDefine.RESERVE_UPDATE_RESPONSE -> TODO()
                ProtocolDefine.RESERVE_CANCEL_REQUEST -> TODO()
                ProtocolDefine.RESERVE_CANCEL_RESPONSE -> TODO()
                ProtocolDefine.RESERVE_ARRIVE_REQUEST -> TODO()
                ProtocolDefine.RESERVE_ARRIVE_RESPONSE -> TODO()
                ProtocolDefine.RESERVE_CALL_REQUEST -> TODO()
                ProtocolDefine.RESERVE_RE_CALL_REQUEST -> TODO()
                ProtocolDefine.UPDATE_INFO_RESPONSE -> onUpdateInfoResponse(receivedData)
                ProtocolDefine.UPLOAD_LOG_FILE_TO_SERVER -> TODO()
                ProtocolDefine.VIDEO_DOWNLOAD_REQUEST -> TODO()
                ProtocolDefine.VIDEO_DOWNLOAD_RESPONSE -> TODO()
                ProtocolDefine.START_PATCH -> TODO() //없는 패킷같은데
                ProtocolDefine.END_PATCH -> TODO()//없는 패킷같은데
                ProtocolDefine.START_IMAGE -> TODO()
                ProtocolDefine.END_IMAGE -> TODO()
                ProtocolDefine.START_VIDEO -> TODO()
                ProtocolDefine.END_VIDEO -> TODO()
                ProtocolDefine.START_SOUND -> TODO()
                ProtocolDefine.END_SOUND -> TODO()
                ProtocolDefine.SERVICE_RETRY -> TODO()
                ProtocolDefine.NO_IP_RETRY -> TODO()
                ProtocolDefine.TELLER_RENEW_REQUEST -> TODO()
                ProtocolDefine.TELLER_RENEW_RESPONSE -> TODO()
                null -> Log.i("정의되지 않은 Protocol이 존재함. PacketAnalyer 확인 요망.")
                else -> {
                    //Define된 Protocol중 Send에 해당하는 부분은 처리할 필요 없다.
                    //그리고 inputStream에만 listener가 오도록 되어있어 실제로 넘어오지도 않아야 한다.
                    Log.i("잘못 처리된 Protocol이 존재함. $protocolDefine")
                }
            }
        }

        override fun onDisconnected() {
            Log.d("onDisconnected")
        }
    }
    /** 업데이트 여부 패킷까지 완료 하면 각 요청을 보내 데이터를 받아온다.*/
    private fun requestOther() {
        Log.d("상담예약리스트 요청")
        tcpClient.sendProtocol(ReserveListRequest().toByteBuffer())
        Log.d("영상 재생 리스트 요청")
        tcpClient.sendProtocol(MediaListRequest().toByteBuffer())
        Log.d("로그파일 업로드")
        uploadLogFileToServer()
    }

    private fun onConnectSuccess() {
        val ip = getLocalIpAddress()
        val mac = getMacAddress()
        if(ip.isNullOrEmpty() || mac.isNullOrEmpty()) {
            Log.e("승인요청 취소 DISPLAY_IP:$ip, DISPLAY_MAC:$mac")
        }
        else {
            val sendData = AcceptAuthRequest(ip, mac)
            tcpClient.sendProtocol(sendData.toByteBuffer())
        }
    }

    private val receiver = CommResultReceiver.Receiver { resultCode, resultData ->
        val code = resultCode.toShort()
        val protocolName = ProtocolDefine.entries.find { it.value == code }?.name ?: "Unknown"
        Log.d(protocolName)

        when (code) {
            ProtocolDefine.ACCEPT_AUTH_RESPONSE.value -> onAcceptAuthResponse(receivedData)
            ProtocolDefine.WAIT_RESPONSE.value -> onWaitCount()
            ProtocolDefine.CALL_REQUEST.value -> //Display값이 Main, Bk 로 온다.
                if (resultData.getString("Display") == "Main") onCall(true) else onCall(false)

            ProtocolDefine.RE_CALL_REQUEST.value ->
                if (resultData.getString("Display") == "Main") onReCall(true) else onReCall(false)

            ProtocolDefine.EMPTY_REQUEST.value -> onAbsent()
            ProtocolDefine.INFO_MESSAGE_REQUEST.value -> onInfoText()
            ProtocolDefine.SYSTEM_OFF.value -> onSystemOFF()
            ProtocolDefine.RESTART_REQUEST.value -> onRestartRequest()
            ProtocolDefine.CROWDED_REQUEST.value -> onCrowedRequest()
            ProtocolDefine.WIN_RESPONSE.value -> onWinResponse()
            ProtocolDefine.SUB_SCREEN_RESPONSE.value -> onSubScreenResponse()
            ProtocolDefine.BGM_INFO.value -> onBGMInfo()
            ProtocolDefine.VIDEO_SET.value -> restartIQSDisplayer() // 230905, by HAHU 서버에서 onVideoSet 보낸 의도가 표시기 재시작을 위함인 것임
            ProtocolDefine.CALL_CANCEL.value -> onCallCancel()
            ProtocolDefine.CALL_COLLECT_SET.value -> onCallCollectSet()
            ProtocolDefine.ERROR_SET.value -> onErrorSet()
            ProtocolDefine.PJT_SET.value -> onPJTSet()
            ProtocolDefine.DISPLAY_INFO.value -> onDisplayInfo()
            ProtocolDefine.TELLER_LIST.value -> {
                // 230905, by HAHU 직원 정보 수정 내려오면 재접속 시키기
                stopIQSService()
                startIQSService(commResultReceiver)
            }

            ProtocolDefine.VOLUME_TEST.value -> onVolumeTest()
            ProtocolDefine.SOUND_SET.value -> onSoundSet()
            ProtocolDefine.SERVICE_RETRY.value -> {
                Log.d("ServiceRetry timer start... (${Const.Handle.RETRY_SERVICE_TIME}msec)")
                stopIQSService()
                Handler(Looper.getMainLooper()).postDelayed({
                    startIQSService(commResultReceiver)
                }, Const.Handle.RETRY_SERVICE_TIME)
            }

            ProtocolDefine.RESERVE_CALL_REQUEST.value -> { 
                Log.i("ReservCalRequest : 상담예약호출 수신... ${ScreenInfo.instance.reserveList.size}")
                onReserveCallRequest()
            }

            ProtocolDefine.RESERVE_RE_CALL_REQUEST.value -> { 
                Log.i("ReservReCallRequest : 상담예약재호출 수신...")
                onReserveReCallRequest()
            }

            ProtocolDefine.TELLER_RENEW_REQUEST.value -> onTellerRenewRequest()
            else -> Log.i("setupServiceReceiver : default 수신... $resultCode")
        }
    }

    private fun onAcceptAuthResponse(receivedData: BaseReceivePacket) {
        Log.e( "onAcceptAuthResponse : 정상접속 완료...")

        val data = receivedData as AcceptAuthResponse
        sharedViewModel.updateDefaultInfo(data)

        //setPreference(Const.Name.PREF_DISPLAY_INFO, Const.Key.DisplayInfo.STATUS_TEXT, ScreenInfo.instance.tellerMent.value)

        replaceFragment(Index.FRAGMENT_MAIN)
    }

    private fun onUpdateInfoResponse(receivedData: BaseReceivePacket) {
        Log.e( "onUpdateInfoResponse :업데이트정보 수신 완료...")

        val data = receivedData as UpdateInfoResponse

        when(data.updateType) {
            0 -> {
                Log.d("update = 0. 다운로드할 파일이 없음")
                requestOther()
            }
            1 -> { // 다운로드할 파일의 첫 번째 처리 부분
                UpdateManager.setUpdateFileInfo(receivedData.updateSize, receivedData.updateFileName)
            }

            2 -> { // 다운로드할 파일을 반복해서 처리하는 부분
                UpdateManager.writeData(receivedData.dataArray)

                if(UpdateManager.isCompleteDownload()) {
                    if(UpdateManager.getFileExtension() == "apk") {
                        installSilent(packageName, UpdateManager.getFileName())
                    }
                    else {
                        requestOther()
                    }
                }

            }
            3 -> { Log.d("업데이트 실패 : update = 3.") }
            else -> { // 그 밖의 경우 처리
                Log.d("알 수 없는 업데이트. update = ${data.updateType}. 그밖의 경우로 현재 정의된 값이 없어서, else 인 경우는 발생되지 않음.")
            }
        }
    }

    private fun onReserveListResponse(receivedData: BaseReceivePacket) {
        Log.e( "onReserveListResponse :상담예약리스트 수신 완료...")
        val data = receivedData as ReserveListResponse
        sharedViewModel.updateReserveList(data)
    }

    private fun onMediaListResponse(receivedData: BaseReceivePacket) {
        Log.e( "onMediaListResponse : 영상리스트 수신 완료...")
        val data = receivedData as MediaListResponse
        sharedViewModel.updateMediaList(data)
    }


    //대기자수 응답
    private fun onWaitCount() {
        Log.i("onWaitCount : 대기자수 수신..." + ScreenInfo.instance.waitNum.value)
        //LiveData observe 로 처리됨.
    }

    private fun onCall(isMain: Boolean) {
        Log.i("onCall : 호출 수신... isMain:$isMain")
        //LiveData observe 로 처리됨. 음성호출만 처리함.
        val screenInfo = ScreenInfo.instance

        //Call 이 왔을때 20초 강제 설정
        if(isMain) replaceFragment(Index.FRAGMENT_MAIN, 20000)
        else replaceFragment(Index.FRAGMENT_BACKUP_CALL, 20000)

        CallSoundManager().play(callNum = screenInfo.callNum.value!!,
                                callWinNum = screenInfo.callWinNum,
                                flagVIP = screenInfo.flagVIP == 1)
    }

    private fun onReCall(isMain: Boolean) {
        Log.i("onReCall : 호출 수신... isMain:$isMain")
        //LiveData observe 로 처리됨. 음성호출만 처리함.
        val screenInfo = ScreenInfo.instance

        //Call 이 왔을때 20초 강제 설정
        if(isMain) replaceFragment(Index.FRAGMENT_MAIN, 20000)
        else replaceFragment(Index.FRAGMENT_BACKUP_CALL, 20000)

        CallSoundManager().play(callNum = screenInfo.callNum.value!!,
            callWinNum = screenInfo.callWinNum,
            flagVIP = screenInfo.flagVIP == 1)
    }

    private fun onAbsent() {
        Log.i("onAbsent WinID ${ScreenInfo.instance.winID}")

        //화상 창구 일 경우 부재중 정보 무시
        if(ScreenInfo.instance.winID == 91) return

        replaceFragment(Index.FRAGMENT_MAIN)

        val logMessage = if(ScreenInfo.instance.flagEmpty.value == 0) { //부재해제
            "부재해제 수신 ... EmptyFlag : ${ScreenInfo.instance.flagEmpty.value}, TellerMent : ${ScreenInfo.instance.tellerMent.value}"
        }
        else { //부재중
            "부재중 수신 ... EmptyFlag : ${ScreenInfo.instance.flagEmpty.value}, EmptyMsg : ${ScreenInfo.instance.emptyMsg}"
        }
        Log.d(logMessage)
    }

    private fun onInfoText() {
        Log.i("onInfoText : 안내문구 수신... (${ScreenInfo.instance.tellerMent})")
        setPreference(Const.Name.PREF_DISPLAY_INFO, Const.Key.DisplayInfo.STATUS_TEXT, ScreenInfo.instance.tellerMent.value) //기존것 대로 가져왔으나 추후 필요성이 없으면 삭제하겠음.
    }

    private fun onSystemOFF() {
        Log.i("onSystemOFF : 시스템종료 수신...")

        val pb = ProcessBuilder(*arrayOf("su", "-c", "/system/bin/reboot -p"))
        var process: Process? = null
        try {
            process = pb.start()
            process.waitFor()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    // 230905, by HAHU  iqsdisplayer 재시작
    private fun restartIQSDisplayer() {
        Log.i("IQSDisplayerRestart : 재시작")
        val packageManager = packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        startActivity(mainIntent)
        exitProcess(0)
    }

    //시스템 재시작
    private fun onRestartRequest() {
        Log.i("onRestartRequest : 시스템재시작 수신...")

        val pb = ProcessBuilder(*arrayOf("su", "-c", "/system/bin/reboot"))
        var process: Process? = null
        try {
            process = pb.start()
            process.waitFor()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //창구혼잡
    private fun onCrowedRequest() {
        Log.i("onCrowedRequest : 창구혼잡 수신... " + ScreenInfo.instance.isCrowded)
    }
    //창구별 대기자수
    private fun onWinResponse() {
        Log.i("onCrowedRequest : 창구혼잡 수신... " + ScreenInfo.instance.isCrowded)
    }

    //보조표시기
    private fun onSubScreenResponse() {
        Log.i("onSubScreenResponse : 보조표시기 수신...")
    }

    //배경음악정보
    private fun onBGMInfo() {
        Log.i("onBGMInfo : 배경음악정보 수신...")
    }

    //호출취소 응답
    private fun onCallCancel() {
        Log.i("onCallCancel : 호출취소 수신...")
    }

    //호출횟수설정 응답
    private fun onCallCollectSet() {
        Log.i("onCallCollectSet: 호출횟수설정 수신...")
    }

    //전산장애설정 응답
    private fun onErrorSet() {
        Log.i("onErrorSet : 전산장애설정 수신... ${ScreenInfo.instance.systemError.value}")

        if (ScreenInfo.instance.systemError.value != 0) {
            replaceFragment(Index.FRAGMENT_MAIN)
        }
    }

    //공석설정 응답
    private fun onPJTSet() {
        Log.i("onPJTSet : 공석설정 수신... ${ScreenInfo.instance.pjt.value}")
    }

    //화면정보 응답
    private fun onDisplayInfo() {
        Log.i(("onDisplayInfo : 화면정보 수신... 대기자수:" + ScreenInfo.instance.waitNum)+ "    테마:" + ScreenInfo.instance.theme)
    }

    //호출음테스트 응답
    private fun onVolumeTest() {
        Log.i("onVolumeTest : 호출음테스트 수신...")
        CallSoundManager().playVolumeTest()
    }

    //호출음설정 응답
    private fun onSoundSet() {
        //호출사운드 설정
        val volume = ScreenInfo.instance.volumeInfo.toInt() //볼륨 설정
        val callCount = ScreenInfo.instance.callInfo.toInt() //호출 반복횟수 설정
        val bellSound = ScreenInfo.instance.bellInfo //벨소리 파일명 설정
        val ment = ScreenInfo.instance.ment //안내멘트 설정
        Log.i("onSoundSet : 호출음설정 수신...$volume/$callCount/$bellSound/$ment")
    }

    private fun onTellerRenewRequest() {
        Log.i("onTellerRenewRequest : 직원정보갱신 수신...")
    }

    private fun onReserveCallRequest() {
        val screenInfo = ScreenInfo.instance

        //Call 이 왔을때 20초 강제 설정
        replaceFragment(Index.FRAGMENT_MAIN, 20000)

        CallSoundManager().play(callNum = screenInfo.reserveCallNum.toInt(),
            callWinNum = screenInfo.reserveWinNum.toInt(),
            flagVIP = screenInfo.flagVIP == 1)
    }

    private fun onReserveReCallRequest() {
        val screenInfo = ScreenInfo.instance

        replaceFragment(Index.FRAGMENT_MAIN, 20000)

        CallSoundManager().play(callNum = screenInfo.reserveCallNum.toInt(),
            callWinNum = screenInfo.reserveWinNum.toInt(),
            flagVIP = screenInfo.flagVIP == 1)
    }

    private fun uploadLogFileToServer() {
        val logFilePath = Const.Path.DIR_LOG
        val currentDateFileName = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        Log.d("=================================")
        Log.d("   로그파일 서버전송 시작")
        val logDir = File(logFilePath)
        val fileList = logDir.listFiles()?.filter { it.isFile } // 파일만 필터링

        fileList?.reversed()?.forEach { file ->
            val fileName = file.name
            // 현재 날짜 파일은 제외
            if (fileName != "$currentDateFileName.txt") {
                Log.d("uploadLogFileToServerSub() : 현재날짜 = $currentDateFileName, 업로드 파일 = $fileName")
                uploadLogFileToServerSub(fileName)
            }
        }
        Log.d("   로그파일 서버전송 종료")
        Log.d("=================================")
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

                    tcpClient.sendProtocol(sendByteBuffer)
                }

                // 파일 읽기가 끝났으므로 해당 파일 삭제
                uploadFile.delete()
            }
        } catch (e: Exception) {
            Log.e("uploadLogFileToServerSub() 예외 발생: ${e.message}", e)
        }
    }
}
