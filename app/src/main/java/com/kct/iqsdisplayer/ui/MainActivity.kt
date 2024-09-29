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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.CallSoundManager
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.Const.ConnectionInfo.loadCommunicationInfo
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.common.SystemReadyModel
import com.kct.iqsdisplayer.common.UpdateManager
import com.kct.iqsdisplayer.data.BackupCallInfo
import com.kct.iqsdisplayer.data.Call
import com.kct.iqsdisplayer.data.Reserve
import com.kct.iqsdisplayer.data.ReserveCall
import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.data.packet.receive.AcceptAuthResponse
import com.kct.iqsdisplayer.data.packet.receive.CrowdedRequest
import com.kct.iqsdisplayer.data.packet.receive.InfoMessageRequest
import com.kct.iqsdisplayer.data.packet.receive.MediaListResponse
import com.kct.iqsdisplayer.data.packet.receive.PausedWorkRequest
import com.kct.iqsdisplayer.data.packet.receive.ReserveListResponse
import com.kct.iqsdisplayer.data.packet.receive.TellerListResponse
import com.kct.iqsdisplayer.data.packet.receive.TellerRenewRequest
import com.kct.iqsdisplayer.data.packet.receive.UpdateInfoResponse
import com.kct.iqsdisplayer.data.packet.receive.WaitResponse
import com.kct.iqsdisplayer.data.packet.receive.WinResponse
import com.kct.iqsdisplayer.data.packet.send.AcceptAuthRequest
import com.kct.iqsdisplayer.data.packet.send.MediaListRequest
import com.kct.iqsdisplayer.data.packet.send.ReserveListRequest
import com.kct.iqsdisplayer.data.packet.send.UpdateInfoRequest
import com.kct.iqsdisplayer.data.packet.send.WaitRequest
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private lateinit var vmSystemReady: SystemReadyModel

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
            vmSystemReady = ViewModelProvider(this)[SystemReadyModel::class.java]
            vmSystemReady.systemReadyLiveData.observe(this) {
                Log.i("systemReady : $it")
                Log.i(""" 
                        |시스템 준비상태 
                        |   접 속 완 료:${vmSystemReady.isConnect.value}
                        |   접속승인응답:${vmSystemReady.isAuthPacket.value}
                        |   예약정보수신:${vmSystemReady.isReservePacket.value}
                        |   대기인원수신:${vmSystemReady.isWaitPacket.value}
                        |   영상정보수신:${vmSystemReady.isMediaPacket.value}
                        |   로그파일전송:${vmSystemReady.isUploadLog.value}
                    """.trimMargin())
                if(it) { replaceFragment(Index.FRAGMENT_MAIN) }
            }

            replaceFragment(Index.FRAGMENT_READY)

            restoreSharedPreferencesFiles()

            initConstInfo()

            tcpClient = TCPClient(Const.ConnectionInfo.IQS_IP, Const.ConnectionInfo.IQS_PORT)
            tcpClient.setOnTcpEventListener(tcpEventListener)
            // 백그라운드 스레드에서 연결 시작
            lifecycleScope.launch(Dispatchers.IO) { tcpClient.start() }
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
                Log.d("권한확인 완료, 시스템 시작")
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
        getSharedPreferences(Const.Name.PREF_DISPLAYER_SETTING, Context.MODE_PRIVATE).loadCommunicationInfo()

        if(Const.ConnectionInfo.DISPLAY_IP == null) {
            Const.ConnectionInfo.DISPLAY_IP = getLocalIpAddress()
            setPreference(Const.Name.PREF_DISPLAYER_SETTING, Const.Key.DisplaySetting.DISPLAY_IP, "")
        }
        if(Const.ConnectionInfo.DISPLAY_MAC == null) {
            Const.ConnectionInfo.DISPLAY_MAC = getMacAddress()
            setPreference(Const.Name.PREF_DISPLAYER_SETTING, Const.Key.DisplaySetting.DISPLAY_MAC, "")
        }
    }

    private fun restoreSharedPreferencesFiles() {
        Const.Path.DIR_SHARED_PREFS = "${filesDir.absolutePath}/shared_prefs/"

        var prefFileName = Const.Name.getPrefDisplayerSettingName()
        var sourcePath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"
        var destPath: String

        val prefDisplayerSetting = File(sourcePath)

        if (!prefDisplayerSetting.exists()) {
            sourcePath = "${Const.Path.DIR_IQS}$prefFileName"
            destPath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"

            if(File(sourcePath).exists()) copyFile(sourcePath, destPath)

        } else {
            Log.d("설정정보파일 정상[${Const.Name.PREF_DISPLAYER_SETTING}]")
        }

        prefFileName = Const.Name.getPrefDisplayInfoName()
        sourcePath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"

        val prefDisplayInfo = File(sourcePath)

        if (!prefDisplayInfo.exists()) {
            sourcePath = "${Const.Path.DIR_IQS}$prefFileName"
            destPath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"

            if(File(sourcePath).exists()) copyFile(sourcePath, destPath)

        } else {
            Log.d("화면정보파일 정상[${Const.Name.PREF_DISPLAY_INFO}]")
        }
    }

    /**=====================================================================================================
     * 실제 로직 시작
     * 주고받는 데이터 순서에 주의
     * TCPClient로 접속시작
     * CONNECT_SUCCESS가 내려오면 ACCEPT_AUTH_REQUEST요청
     * 이후에 다른 패킷들 요청이 가능하다고 함.
    =====================================================================================================*/
    private val tcpEventListener = object : TCPClient.OnTcpEventListener {
        override fun onConnected() {
            Log.d("onConnected")
            ScreenInfo.setSocketConnected(true)
            vmSystemReady.setIsConnect(true)
            Log.i("systemReady : setIsConnect(true)")
        }

        override fun onReceivedData(protocolDefine: ProtocolDefine, receivedData: BaseReceivePacket) {
            //Log.d("${protocolDefine}$receivedData")
            when(protocolDefine) {
                ProtocolDefine.CONNECT_SUCCESS      -> onConnectSuccess() //여기에서 ACCEPT_AUTH_REQUEST 보냄. 지저분해서 함수 안에 넣었음.
                ProtocolDefine.CONNECT_REJECT       -> Log.e("접속 실패 - protocol:${protocolDefine.name}[${protocolDefine.value}]")
                ProtocolDefine.ACCEPT_AUTH_RESPONSE -> {
                                                            onAcceptAuthResponse(receivedData)
                                                            Log.d("업데이트 정보 요청")
                                                            tcpClient.sendData(UpdateInfoRequest().toByteBuffer())
                                                        }
                ProtocolDefine.WAIT_RESPONSE        -> onWaitResponse(receivedData)
                ProtocolDefine.CALL_REQUEST         -> onCallRequest(receivedData)
                ProtocolDefine.RE_CALL_REQUEST      -> onCallRequest(receivedData)
                ProtocolDefine.PAUSED_WORK_REQUEST  -> onPausedWork(receivedData)
                ProtocolDefine.INFO_MESSAGE_REQUEST -> onInfoMessage(receivedData)
                ProtocolDefine.TELLER_LIST          -> onTellerList(receivedData)
                ProtocolDefine.SYSTEM_OFF           -> onSystemOff()
                ProtocolDefine.RESTART_REQUEST      -> onRestartRequest()
                ProtocolDefine.CROWDED_REQUEST      -> onCrowedRequest(receivedData)
                ProtocolDefine.WIN_RESPONSE         -> onWinResponse(receivedData)
                ProtocolDefine.MEDIA_LIST_RESPONSE  -> onMediaListResponse(receivedData)
                ProtocolDefine.RESERVE_LIST_RESPONSE  -> onReserveListResponse(receivedData)
                ProtocolDefine.RESERVE_ADD_REQUEST    -> onReserveAddRequest(receivedData)
                ProtocolDefine.RESERVE_UPDATE_REQUEST -> onReserveUpdateRequest(receivedData)
                ProtocolDefine.RESERVE_CANCEL_REQUEST -> onReserveCancelRequest(receivedData)
                ProtocolDefine.RESERVE_ARRIVE_REQUEST -> onReserveArriveRequest(receivedData)
                ProtocolDefine.RESERVE_CALL_REQUEST   -> onReserveCallRequest(receivedData)
                ProtocolDefine.RESERVE_RE_CALL_REQUEST -> onReserveCallRequest(receivedData)
                /** 업데이트 정보를 수신하고, 업데이트를 할지, 이후 정상동작을 할지 분기를 탄다. */
                ProtocolDefine.UPDATE_INFO_RESPONSE -> onUpdateInfoResponse(receivedData)
                ProtocolDefine.SERVICE_RETRY        -> onConnectRetry()
                ProtocolDefine.TELLER_RENEW_REQUEST -> onTellerRenewRequest(receivedData)
                ProtocolDefine.KEEP_ALIVE_RESPONSE -> {}
                else -> {
                    Log.e("잘못 처리된 Protocol이 존재함. $protocolDefine")
                }
            }
        }

        override fun onDisconnected() {
            Log.d("onDisconnected")
            ScreenInfo.setSocketConnected(false)
        }
    }
    /** 업데이트 여부 패킷까지 완료 하면 각 요청을 보내 데이터를 받아온다.*/
    private fun requestOther() {
        Log.d("상담예약리스트 요청")
        tcpClient.sendData(ReserveListRequest().toByteBuffer())
        Log.d("영상 재생 리스트 요청")
        tcpClient.sendData(MediaListRequest().toByteBuffer())
        Log.d("대기인수 리스트 요청")
        tcpClient.sendData(WaitRequest(winNum = ScreenInfo.winNum).toByteBuffer())
        Log.d("로그파일 업로드")
        uploadLogFileToServer()
    }

    /*
    private suspend fun requestOther() {
        coroutineScope { // 새로운 CoroutineScope 생성
            val reserveListFlow = flow {
                tcpClient.sendData(ReserveListRequest().toByteBuffer())
                emit(tcpClient.awaitResponse(ReserveListResponse::class))
            }

            val mediaListFlow = flow {
                tcpClient.sendData(MediaListRequest().toByteBuffer())
                emit(tcpClient.awaitResponse(MediaListResponse::class))
            }

            val waitListFlow = flow {
                tcpClient.sendData(WaitRequest(winNum = ScreenInfo.winNum).toByteBuffer())
                emit(tcpClient.awaitResponse(WaitResponse::class)) // WaitResponse 클래스가 있다고 가정
            }

            val logUploadFlow = flow {
                uploadLogFileToServer()
                emit(true) // 로그 업로드 완료 시 true emit
            }

            // 모든 flow가 완료될 때까지 기다림
            combine(reserveListFlow, mediaListFlow, waitListFlow, logUploadFlow) { _, _, _, _ ->  }.collect {
                // 모든 요청 및 로그 업로드 완료 시 FragmentMain으로 이동
                replaceFragment(Index.FRAGMENT_MAIN)
            }
        }
    }*/

    private fun onConnectSuccess() {
        val ip = getLocalIpAddress()
        val mac = getMacAddress()
        if(ip.isNullOrEmpty() || mac.isNullOrEmpty()) {
            Log.e("승인요청 취소 DISPLAY_IP:$ip, DISPLAY_MAC:$mac")
        }
        else {
            val sendData = AcceptAuthRequest(ip, mac)
            Log.d("SendProtocol : AcceptAuthRequest")
            tcpClient.sendData(sendData.toByteBuffer())
        }
    }

    private fun onAcceptAuthResponse(receivedData: BaseReceivePacket) {
        val data = receivedData as AcceptAuthResponse
        Log.i( "onAcceptAuthResponse : 정상접속 완료...$data")
        vmSystemReady.setIsAuthPacket(true)
        ScreenInfo.updateDefaultInfo(data)
    }

    private fun onUpdateInfoResponse(receivedData: BaseReceivePacket) {
        val data = receivedData as UpdateInfoResponse
        Log.i( "onUpdateInfoResponse :업데이트정보 수신 완료...$data")

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
        val data = receivedData as ReserveListResponse
        Log.i( "onReserveListResponse :상담예약리스트 수신 완료...$data")
        vmSystemReady.setIsReservePacket(true)
        ScreenInfo.updateReserveList(data)
    }

    private fun onReserveAddRequest(receivedData: BaseReceivePacket) {
        val data = receivedData as Reserve
        Log.i( "onReserveAddRequest :상담예약 추가 수신 완료...$data")
        ScreenInfo.addReserveList(data)
    }

    private fun onReserveUpdateRequest(receivedData: BaseReceivePacket) {
        val data = receivedData as Reserve
        Log.i( "onReserveUpdateRequest :상담예약 수정 수신 완료...$data")
        ScreenInfo.updateReserveList(data)
    }
    
    private fun onReserveCancelRequest(receivedData: BaseReceivePacket) {
        val data = receivedData as Reserve
        Log.i( "onReserveCancelRequest :상담예약 취소 수신 완료...$data")
        ScreenInfo.cancelReserve(data)
    }
    
    private fun onReserveArriveRequest(receivedData: BaseReceivePacket) {
        val data = receivedData as Reserve
        Log.i( "onReserveArriveRequest :상담예약 도착정보 수신 완료...$data")
        ScreenInfo.arriveReserve(data)
    }

    private fun onMediaListResponse(receivedData: BaseReceivePacket) {
        val data = receivedData as MediaListResponse
        Log.i( "onMediaListResponse : 영상리스트 수신 완료...$data")

        vmSystemReady.setIsMediaPacket(true)
        ScreenInfo.updateMediaList(data)
    }

    /** 다른창구에 발권이 되어도 Broadcast 같이 날아옴 */
    private fun onWaitResponse(receivedData: BaseReceivePacket) {
        val data = receivedData as WaitResponse
        Log.i( "onWaitResopnse : 대기자수 응답 현재 창구ID:${ScreenInfo.winId}...$data")
        if(ScreenInfo.winId == data.winId) {
            vmSystemReady.setIsWaitPacket(true)
            ScreenInfo.updateWaitNum(data.waitNum)
        }
    }

    private fun onConnectRetry() {
        Log.i( "onServiceRetry : TCPClient 재시작")
        Handler(Looper.getMainLooper()).postDelayed({
            tcpClient.onDestroy()
            tcpClient = TCPClient(Const.ConnectionInfo.IQS_IP, Const.ConnectionInfo.IQS_PORT)
            tcpClient.setOnTcpEventListener(tcpEventListener)
        }, Const.Handle.RETRY_SERVICE_TIME)
    }

    private fun onCallRequest(receivedData: BaseReceivePacket) {
        //LiveData observe 로 처리됨. 음성호출만 처리함.
        val data = receivedData as Call
        Log.i("onCall : 호출 수신... data:$data")

        val viewMode    = Const.ConnectionInfo.CALLVIEW_MODE //나의 ViewMode
        val isStopWork  = ScreenInfo.isStopWork.value ?: false //나의 업무상태, 구 pjt
        val isMyCall    = data.bkDisplayNum == ScreenInfo.winNum || data.callWinNum == ScreenInfo.winNum

        if(viewMode == Const.CallViewMode.MAIN) {
            if(isStopWork) {    //내가 공석이면 처리안함.
                Log.i("onCall PASS - 공석 상태")
            }
            else {  //공석이 아님
                if(data.isError) { // Call이 장애상황에 해당하면
                    if(data.bkDisplayNum == ScreenInfo.winNum) { //백업표시로 나에게 할당 되었다면

                        val backupData = BackupCallInfo(
                            callNum         = data.callNum,
                            backupWinNum    = data.callWinNum,
                            backupWinName   = ScreenInfo.getWinName(data.callWinId),
                            bkWay           = data.bkWay
                        )

                        ScreenInfo.updateBackupCall(backupData)

                        replaceFragment(Index.FRAGMENT_BACKUP_CALL, 20000)
                    }
                }
                else { //정상 Call이면
                    if(data.callWinNum == ScreenInfo.winNum) { //나의 Call이면 처리, 다른사람 Call은 Pass
                        ScreenInfo.updateCallInfo(data)
                        replaceFragment(Index.FRAGMENT_MAIN, 20000)
                    }
                }
            }
        }
//        else { //내 표시기 상태가 보조거나 음성호출기라면
//            replaceFragment(Index.FRAGMENT_SUB_SCREEN, 20000)
//        }

        if(!isStopWork && isMyCall) {
            CallSoundManager().play(callNum = data.callNum,
                callWinNum = data.callWinNum,
                flagVIP = data.flagVip)
        }
    }

    private fun onPausedWork(receivedData: BaseReceivePacket) {
        val data = receivedData as PausedWorkRequest
        Log.i("onPausedWork : 호출 수신... data:$data")

        if(ScreenInfo.winNum ==  receivedData.pausedWinNum) {
            ScreenInfo.updatePausedWork(receivedData)
            replaceFragment(Index.FRAGMENT_MAIN)
        }

        val isPausedWork = ScreenInfo.isPausedWork.value ?: false
        val logMessage = if(!isPausedWork) { "부재해제 수신 ... 업무중 메세지 : ${ScreenInfo.tellerMent.value}"}
        else { "부재중 수신 ... 부재중 메세지 : ${ScreenInfo.pausedWorkMessage}"}
        Log.d(logMessage)
    }

    private fun onInfoMessage(receivedData: BaseReceivePacket) {
        val data = receivedData as InfoMessageRequest
        Log.i("onInfoMessage : 안내문구 수신... (${data})")
        if(ScreenInfo.winNum == data.infoMessageWinNum) {
            ScreenInfo.updateTellerMent(data.infoMessage)
            setPreference(Const.Name.PREF_DISPLAY_INFO, Const.Key.DisplayInfo.STATUS_TEXT, data.infoMessage)
        }
    }

    private fun onTellerList(receivedData: BaseReceivePacket) {
        val data = receivedData as TellerListResponse
        Log.i("onTellerList : 직원정보 수신... (${data})")

        val teller = data.tellerList.find { teller -> teller.displayIP == Const.ConnectionInfo.DISPLAY_IP }
        if(teller != null) {
            ScreenInfo.tellerInfo = teller
            ScreenInfo.winId = teller.winId
        }
    }

    private fun onSystemOff() {
        Log.i("onSystemOff : 시스템종료 수신...")

        val pb = ProcessBuilder("su", "-c", "/system/bin/reboot -p")
        val process: Process?
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

        val pb = ProcessBuilder("su", "-c", "/system/bin/reboot")
        val process: Process?
        try {
            process = pb.start()
            process.waitFor()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //창구 대기인수가 혼잡상태 및 해제 상태 발생시 순번발행기에서 전송하는 패킷
    private fun onCrowedRequest(receivedData: BaseReceivePacket) {
        val data = receivedData as CrowdedRequest
        Log.i("onCrowedRequest : 창구혼잡 수신... (${data})")
        if(ScreenInfo.winId == data.crowdedWinID) {
            ScreenInfo.updateCrowded(data.isCrowded)
            ScreenInfo.crowdedMsg = data.crowdedMsg
        }
    }
    //순번발행기에서 창구정보 변경 시 전송하는 패킷
    private fun onWinResponse(receivedData: BaseReceivePacket) {
        val data = receivedData as WinResponse
        Log.i("onWinResponse : 창구정보 수신... (${data})")
        ScreenInfo.updateWinInfos(data.winIds, data.winNames, data.waitNums)
    }

    /** 직원정보를 다주는 것이 아니라서 안하는것이 나을 것 같은데..
     * TODO : 쓰는 패킷인지 확인요망 */
    private fun onTellerRenewRequest(receivedData: BaseReceivePacket) {
        val data = receivedData as TellerRenewRequest
        Log.i("onTellerRenewRequest : 직원정보갱신 수신... (${data})")

        ScreenInfo.winNum = data.renewWinNum
        ScreenInfo.tellerInfo.tellerName = data.tellerName
        ScreenInfo.tellerInfo.tellerNum = data.tellerNum
    }

    private fun onReserveCallRequest(receivedData: BaseReceivePacket) {

        val data = receivedData as ReserveCall
        Log.i( "onReserveCallRequest :상담예약 호출 수신 완료...$data")

        val viewMode    = Const.ConnectionInfo.CALLVIEW_MODE  //나의 ViewMode
        val isStopWork  = ScreenInfo.isStopWork.value ?: false //나의 업무상태, 구 pjt
        val isMyCall    = data.reserveBkDisplayNum == ScreenInfo.winNum || data.reserveCallWinNum == ScreenInfo.winNum

        if(viewMode == Const.CallViewMode.MAIN) {
            if(isStopWork) {    //내가 공석이면 처리안함.
                Log.i("onReserveCall PASS - 공석 상태")
            }
            else {  //공석이 아님
                if(data.isError) { // Call이 장애상황에 해당하면
                    if(data.reserveBkDisplayNum == ScreenInfo.winNum) { //백업표시로 나에게 할당 되었다면

                        val backupData = BackupCallInfo(
                            callNum         = data.reserveCallNum,
                            backupWinNum    = data.reserveCallWinNum,
                            backupWinName   = ScreenInfo.getWinName(data.reserveCallWinID),
                            bkWay           = data.reserveBkWay
                        )

                        ScreenInfo.updateBackupCall(backupData)

                        replaceFragment(Index.FRAGMENT_BACKUP_CALL, 20000)
                    }
                }
                else { //정상 Call이면
                    if(data.reserveCallWinNum == ScreenInfo.winNum) { //나의 Call이면 처리, 다른사람 Call은 Pass
                        ScreenInfo.updateReserveCallInfo(data)
                        replaceFragment(Index.FRAGMENT_RECENT_CALL, 20000)
                    }
                }
            }
        }

        if(!isStopWork && isMyCall) {
            CallSoundManager().play(
                callNum     = data.reserveNum,
                callWinNum  = data.reserveCallWinNum,
                flagVIP     = data.flagVip)
        }
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
        vmSystemReady.setIsUploadLog(true)
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

                    tcpClient.sendData(sendByteBuffer)
                }

                // 파일 읽기가 끝났으므로 해당 파일 삭제, TODO : 임시로 삭제안함 업로드 로직 확인용
                // uploadFile.delete()
            }
        } catch (e: Exception) {
            Log.e("uploadLogFileToServerSub() 예외 발생: ${e.message}", e)
        }
    }
}
