package com.kct.iqsdisplayer.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kct.iqsdisplayer.BuildConfig
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.CommResultReceiver
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.Const.CommunicationInfo.loadCommunicationInfo
import com.kct.iqsdisplayer.databinding.FragmentInitBinding
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.service.IQSComClass
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.copyFile
import com.kct.iqsdisplayer.util.getLocalIpAddress
import com.kct.iqsdisplayer.util.getMacAddress
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader


class FragmentInit : Fragment() {

    private var _binding: FragmentInitBinding? = null
    private val binding get() = _binding!!

    private var listener: FragmentResultListener? = null
    private lateinit var checkService: CheckService
    private var mainActivity: MainActivity? = null
    // 서비스 결과 수신 Receiver
    private var commResultReceiver = CommResultReceiver(Handler(Looper.getMainLooper()))
        .apply { setReceiver(receiver) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity
        if (context is FragmentResultListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FragmentResultListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 설정 파일 복구
        restoreSharedPreferencesFiles()

        startInit()
    }

    private fun startInit() {
        // SharedPreferences 값 CommunicationInfo에 저장
        initCommunicationInfo()

        binding.btSetting.setOnClickListener(clickListener)
        binding.btReboot.setOnClickListener(clickListener)
        binding.btShutdown.setOnClickListener(clickListener)
        binding.pbLoading.visibility = View.VISIBLE
        binding.tvVersionInfo.text = BuildConfig.VERSION_NAME

        mainActivity?.startIQSService(commResultReceiver)

        checkService = CheckService(mainActivity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val clickListener = View.OnClickListener {
        when (it.id) {
            R.id.btSetting -> {
                Log.i("설정버튼 선택")
                mainActivity?.stopIQSService()
                timerHandler.removeMessages(Const.Handle.TIMEOUT_CHANGE_FRAGMENT_MESSAGE)
                mainActivity?.showFragment(FragmentFactory.Index.FRAGMENT_SETTING)
            }
            R.id.btReboot -> {
                Log.i("재부팅버튼 선택")
                mainActivity?.rebootIQSDisplayer()
            }
            R.id.btShutdown -> {
                Log.i("프로그램종료버튼 선택")
                mainActivity?.finishApp("프로그램종료버튼 선택")
            }
            else -> {
                Log.e("알 수 없는 버튼")
            }
        }
    }

    /**
     * SharedPreference 파일이 없을 경우 백업본 카피 복구
     */
    private fun restoreSharedPreferencesFiles() {
        var prefFileName = Const.Name.getPrefDisplayerSettingName()
        val prefDisplayerSetting = File(Const.Path.DIR_SHARED_PREFS, prefFileName)

        if (!prefDisplayerSetting.exists()) {
            val sourcePath = "${Const.Path.DIR_IQS}$prefFileName"
            val destPath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"
            context?.let { copyFile(it, sourcePath, destPath) }
        } else {
            Log.d("설정정보파일 정상[${Const.Name.PREF_DISPLAYER_SETTING}]")
        }

        prefFileName = Const.Name.getPrefDisplayInfoName()
        val prefDisplayInfo = File(Const.Path.DIR_SHARED_PREFS, prefFileName)

        if (!prefDisplayInfo.exists()) {
            val sourcePath = "${Const.Path.DIR_IQS}$prefFileName"
            val destPath = "${Const.Path.DIR_SHARED_PREFS}$prefFileName"
            context?.let { copyFile(it, sourcePath, destPath) }
        } else {
            Log.d("화면정보파일 정상[${Const.Name.PREF_DISPLAY_INFO}]")
        }
    }

    private fun backupSharedPreferencesFiles() {
        // 설정 정보 파일 백업
        val displayerSettingFile = File(Const.Path.DIR_SHARED_PREFS + Const.Name.getPrefDisplayerSettingName())
        if (displayerSettingFile.exists()) {
            context?.let {
                copyFile(it, displayerSettingFile.absolutePath, Const.Path.DIR_IQS)
            }
        } else {
            Log.i("backupSharedPreferencesFiles : ${Const.Name.PREF_DISPLAYER_SETTING} is not exist.. backup failed")
        }

        // 화면 정보 파일 백업
        val displayInfoFile = File(Const.Path.DIR_SHARED_PREFS + Const.Name.getPrefDisplayInfoName())
        if (displayInfoFile.exists()) {
            context?.let {
                copyFile(it, displayInfoFile.absolutePath, Const.Path.DIR_IQS)
            }
        } else {
            Log.i("backupSharedPreferencesFiles : ${Const.Name.PREF_DISPLAY_INFO} is not exist.. backup failed")
        }
    }

    /**
     * 설정 파일 값 저장
     */
    private fun initCommunicationInfo() {
        context?.let {
            val pref = it.getSharedPreferences(Const.Name.PREF_DISPLAYER_SETTING, Context.MODE_PRIVATE)
            pref.loadCommunicationInfo()
        }

        Const.CommunicationInfo.MY_IP = getLocalIpAddress()
        Const.CommunicationInfo.MY_MAC = getMacAddress()
    }

    private var bFTPSuccess = false
    private var strResult = ""

    /**
     * 추후 삭제 예정
     * MainActivity 에서 하면 될 것으로 보인다.
     */
    // 서비스 결과 수신 리스너
    private val receiver = CommResultReceiver.Receiver { resultCode, resultData ->
        val code = resultCode.toShort()
        val protocolName = ProtocolDefine.entries.find { it.value == code }?.name ?: "Unknown"
        Log.d(protocolName) // 실제 protocolName 출력

        when (code) {
            // 접속 승인
            ProtocolDefine.ACCEPT_AUTH_RESPONSE.value -> {
                strResult = ""
                strResult += "서버 접속 완료"
                binding.tvInfo.text = strResult
                bFTPSuccess = true // ftp 다운로드 결과 초기화
            }

            ProtocolDefine.START_PATCH.value -> {
                //binding.tvInfo.text = "패치파일 다운로드 시작...\n" + resultData.getString("FileName", "")
                val text = "패치파일 다운로드 시작...\n${resultData.getString("FileName", "")}"
                binding.tvInfo.text = text
            }

            ProtocolDefine.END_PATCH.value -> {
                Log.d("End Patch => ${resultData.getBoolean("result")}")
                if (resultData.getBoolean("result")) {
                    strResult += "\r\n패치파일 다운로드 완료"
                    binding.tvInfo.text = strResult
                    val strPatchFileName = resultData.getString("FileName", "")
                    if (strPatchFileName.isNotEmpty()) {
                        Log.d("start install $strPatchFileName file...")

                        // 패치 프로그램 설치 부분 임시 주석 처리 *나중에 풀어야 함
                        checkService.setRunning(false)
                        mainActivity?.stopIQSService()

                        backupSharedPreferencesFiles()

                        Log.d("Start Patch File install")

                        installSilent(strPatchFileName)

                        val message = timerHandler.obtainMessage(Const.Handle.TIMEOUT_CHANGE_FRAGMENT_MESSAGE, Const.FragmentResult.INIT_PATCH)
                        timerHandler.sendMessageDelayed(message, Const.Handle.TIMEOUT_CHANGE_FRAGMENT_TIME) // 엑티비티 전환 타이머 시작
                    }
                } else {
                    strResult += "\r\n패치파일 설치안함(서버 버전 <= 패치 파일 버전)"
                    binding.tvInfo.text = strResult
                    bFTPSuccess = false

                    val message = timerHandler.obtainMessage(Const.Handle.TIMEOUT_CHANGE_FRAGMENT_MESSAGE, Const.FragmentResult.INIT_NONE_PATCH)
                    timerHandler.sendMessageDelayed(message, Const.Handle.TIMEOUT_CHANGE_FRAGMENT_TIME) // 엑티비티 전환 타이머 시작
                }
            }

            ProtocolDefine.START_IMAGE.value -> {
                binding.tvInfo.text = "이미지파일 다운로드 시작...\n" + Const.Path.DIR_IMAGE
            }

            ProtocolDefine.END_IMAGE.value -> {
                Log.d("End Image => ${resultData.getBoolean("result")}")

                strResult += if (resultData.getBoolean("result"))
                    "\r\n이미지파일 다운로드 완료"
                else {
                    "\r\n이미지파일 다운로드 실패"
                }.also { bFTPSuccess = false }

                binding.tvInfo.text = strResult
            }

            ProtocolDefine.START_VIDEO.value -> {
                binding.tvInfo.text = "동영상파일 다운로드 시작...\n" + Const.Path.DIR_VIDEO
            }

            ProtocolDefine.END_VIDEO.value -> {
                Log.d("End Video => ${resultData.getBoolean("result")}")
                strResult += if (resultData.getBoolean("result"))
                    "\r\n동영상파일 다운로드 완료"
                else {
                    "\r\n동영상파일 다운로드 실패"
                }.also { bFTPSuccess = false }

                binding.tvInfo.text = strResult
            }

            ProtocolDefine.START_SOUND.value -> {
                Log.d("Start Sound")
                binding.tvInfo.text = "사운드파일 다운로드 시작...\n" + Const.Path.DIR_SOUND
            }

            ProtocolDefine.END_SOUND.value -> {
                Log.d("End Sound => ${resultData.getBoolean("result")}")

                if (resultData.getBoolean("result")) {
                    binding.tvInfo.text = "$strResult\n사운드파일 다운로드 완료"

                    if (bFTPSuccess) {
                        val message = timerHandler.obtainMessage(Const.Handle.TIMEOUT_CHANGE_FRAGMENT_MESSAGE, Const.FragmentResult.INIT_NONE_PATCH)
                        timerHandler.sendMessageDelayed(message, Const.Handle.TIMEOUT_CHANGE_FRAGMENT_TIME) // 엑티비티 전환 타이머 시작
                    } else {
                        binding.pbLoading.visibility = View.INVISIBLE
                        timerHandler.sendEmptyMessageDelayed(
                            Const.Handle.RETRY_SERVICE_MESSAGE,
                            Const.Handle.RETRY_SERVICE_TIME
                        )
                    }
                } else {
                    binding.tvInfo.text = "$strResult\n사운드파일 다운로드 실패"
                    binding.pbLoading.visibility = View.INVISIBLE
                    timerHandler.sendEmptyMessageDelayed(
                        Const.Handle.RETRY_SERVICE_MESSAGE,
                        Const.Handle.RETRY_SERVICE_TIME
                    )
                }
            }

            ProtocolDefine.SERVICE_RETRY.value -> {
                /*
                    Log.d(TAG, "ServiceRetry timer start... (" + Define.RETRY_SERVICE_TIME + "msec)");
                    writeLog.WriteLog("Service Restart");
                    binding.tvInfo.setText("서버접속 중...");
                    timerHandler.sendEmptyMessageDelayed(Define.RETRY_SERVICE_MESSAGE, Define.RETRY_SERVICE_TIME); //서비스 retry 타이머 시작
                    */
            }

            ProtocolDefine.RESERVE_LIST_RESPONSE.value -> {
                Log.d("ReservListResponse ... 상담예약리스트 수신")
            }

            else -> {
                Log.d("default $resultCode")
            }
        }
    }

    // 업체 패치 파일 업데이트 함수
    private fun installSilent(fileName: String): Int {
        Log.d("Start Patch File install..File Name : $fileName")
        
        val filePath = Const.Path.DIR_PATCH + fileName
        val file = File(filePath)
        if (fileName.isEmpty() || file.length() <= 0 || !file.exists() || !file.isFile) {
            Log.d("Not exist File")
            return 1
        }

        val args = arrayOf("pm", "install", "-i", requireContext().packageName, filePath)

        val processBuilder = ProcessBuilder(*args) // *args를 사용하여 배열을 펼쳐서 전달
        var process: Process? = null
        var successResult: BufferedReader? = null
        var errorResult: BufferedReader? = null
        val successMsg = StringBuilder()
        val errorMsg = StringBuilder()
        var result: Int

        try {
            process = processBuilder.start()
            successResult = BufferedReader(InputStreamReader(process.inputStream))
            errorResult = BufferedReader(InputStreamReader(process.errorStream))
            var s: String?

            while (successResult.readLine().also { s = it } != null) {
                successMsg.append(s)
            }

            while (errorResult.readLine().also { s = it } != null) {
                errorMsg.append(s)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("Fail Process IOException")
            result = 2
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Fail Process Exception")
            result = 2
        } finally {
            successResult?.close()
            errorResult?.close()
            process?.destroy()
        }

        // mHandler 관련 코드 제거
        result = if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            0
        } else {
            2
        }
        Log.d("successMsg: $successMsg, ErrorMsg: $errorMsg")

        return result
    }

    // 액티비티 타이머 핸들러
    private val timerHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Const.Handle.TIMEOUT_CHANGE_FRAGMENT_MESSAGE -> {
                    Log.i("TimerHandler : 초기화 Fragment 종료 => 메인 액티비티로 전환")
                    checkService.setRunning(false)
                    if(msg.obj != null && msg.obj is Const.FragmentResult) {
                        listener?.onResult(msg.obj as Const.FragmentResult)
                    }
                }

                Const.Handle.RETRY_SERVICE_MESSAGE -> {
                    Log.i("TimerHandler: 서비스 retry")
                    val mainActivity = activity as MainActivity
                    // 서비스 재시작
                    mainActivity.stopIQSService() // IQSComClass 서비스 중지
                    startInit()
                }
            }
        }
    }


    /**
     * 추후 삭제 예정
     */
    // 서비스 감시
    private inner class CheckService(val mainActivity: MainActivity?) : Thread() {
        var isRunning = true

        fun setRunning(isRunning: Boolean) {
            this.isRunning = isRunning
        }

        override fun run() {
            while (isRunning) {
                if (mainActivity?.isMyServiceRunning(IQSComClass::class.java) == false) {
                    Log.i("InitializeFragment : ServiceRestart")
                    // timerHandler.sendEmptyMessageDelayed(Define.RETRY_SERVICE_MESSAGE, Define.RETRY_SERVICE_TIME) //서비스 retry 타이머 시작

                    Handler(Looper.getMainLooper()).post {
                        binding.tvInfo.text = "서버 재 접속 중.."
                    }
                    mainActivity.startIQSService(commResultReceiver)
                }

                try {
                    sleep(5000)
                } catch (e: InterruptedException) {
                    Log.e("CheckService interrupted: ${e.message}", e) // 로그 개선
                    break // 인터럽트 발생 시 루프 종료
                }
            }
        }

    }



}
