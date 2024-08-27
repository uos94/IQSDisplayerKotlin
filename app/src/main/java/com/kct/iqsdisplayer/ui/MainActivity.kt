package com.kct.iqsdisplayer.ui

import android.app.ActivityManager
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
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.CallSoundManager
import com.kct.iqsdisplayer.common.CommResultReceiver
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.service.IQSComClass
import com.kct.iqsdisplayer.ui.FragmentFactory.Index
import com.kct.iqsdisplayer.ui.FragmentFactory.replaceFragment
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.makeDir
import com.kct.iqsdisplayer.util.setFullScreen
import com.kct.iqsdisplayer.util.setPreference
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), FragmentResultListener {

    private var commResultReceiver = CommResultReceiver(Handler(Looper.getMainLooper()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        setFullScreen()

        FragmentFactory.setActivity(this)

        commResultReceiver.setReceiver(receiver)

        startSystem()
    }

    private fun startSystem() {
        if(checkStorage()) {
            //Storage 를 사용 할 준비가 되었다면 FRAGMENT_INIT부터 시작한다.
            replaceFragment(Index.FRAGMENT_INIT)
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
        else { //권한이 있으므로 초기 경로 Setting
            Const.Path.DIR_ROOT = Environment.getExternalStorageDirectory()!!.absolutePath
            Const.Path.DIR_SHARED_PREFS = "${filesDir.absolutePath}/shared_prefs/"
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

    fun rebootIQSDisplayer() {
        val pb = ProcessBuilder(*arrayOf("su", "-c", "/system/bin/reboot"))
        var process: Process? = null
        try {
            process = pb.start()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun finishApp(message: String = "앱 종료 호출") {
        Log.e(message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finishAffinity()
    }


    /**
     * 서버에서 받아온 값으로 돌아감.
     * 일반적으로 Call 이 온 경우만 20초, 나머지는 10초
     * 기존코드에 Call 이 왔을 경우 screenInfo와 상관없이 20초로 셋팅하고 있어 hardSetDelayTime을 따로 두었음.
     */


    override fun onResult(result: Const.FragmentResult) {
        when(result) {
            Const.FragmentResult.INIT_NONE_PATCH -> { //상태정상이면 화면 초기화진행
                Log.i("onActivityResult : 초기화 정상 반환... (RESULT_OK)")
                //onAcceptAuthResponse() //startService후 ACCEPT_AUTH_RESPONSE에서 진행되므로 삭제함.
                startIQSService(commResultReceiver)
            }
            Const.FragmentResult.INIT_PATCH -> {  //상태 비정상이면 프로그램 종료
                finishApp("앱 설치로 인한 앱 종료")
            }
            else -> {

            }
        }
    }

    /**
     * 서비스를 MainActivity에서 돌리고 각각의 fragment에서는 bind만해서 동작하도록 하는것이 좋을 것 같으나
     * 우선 동작하는게 우선이라 그대로 로직을 따라간다.
     */
    fun startIQSService(commResultReceiver: CommResultReceiver) {
        Log.i("startIQSService")
        if(isMyServiceRunning(IQSComClass::class.java)) {
            stopIQSService()
        }
        val intent = Intent(this, IQSComClass::class.java)
        intent.putExtra("receiver", commResultReceiver)
        startService(intent)
    }

    fun stopIQSService() {
        Log.i("stopIQSService")
        val intent = Intent(this, IQSComClass::class.java)
        stopService(intent)
    }
    /**
     * Android 8.0(API 레벨 26)부터 사용 안되는 것으로 보여 확인 요망, 시스템 App이라 될 수도 있음.
     */
    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    //===========================================================================================
    //Receiver 에 수신된 데이터 처리 및 관련 함수들
    //===========================================================================================
    private val receiver = CommResultReceiver.Receiver { resultCode, resultData ->
        val code = resultCode.toShort()
        val protocolName = ProtocolDefine.entries.find { it.value == code }?.name ?: "Unknown"
        Log.d(protocolName)

        when (code) {
            ProtocolDefine.ACCEPT_AUTH_RESPONSE.value -> onAcceptAuthResponse()
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
            ProtocolDefine.TELLER.value -> {
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

    private fun onAcceptAuthResponse() {
        Log.e( "onAcceptAuthResponse : 정상접속 완료...")

        setPreference(Const.Name.PREF_DISPLAY_INFO, Const.Key.DisplayInfo.STATUS_TEXT, ScreenInfo.instance.tellerMent.value)

        replaceFragment(Index.FRAGMENT_MAIN)
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
}

interface FragmentResultListener {
    fun onResult(result: Const.FragmentResult)
}