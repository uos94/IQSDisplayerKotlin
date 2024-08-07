package com.kct.iqsdisplayer.common

import android.content.SharedPreferences
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Const {
    object Path {

        /**
         * 최초 앱 시작 시 권한이 없을 경우 init되지 않았음에 주의.
         * ExternalStorage 사용 권한을 얻은 후에 경로가 셋팅 됨. 경로는 다음과 같음.
         * 예상경로 : /storage/emulated/0
         * @see android.os.Environment.getExternalStorageDirectory()
         * **/
        lateinit var DIR_ROOT: String

        /** 예상경로 : /storage/emulated/0/IQS/ */
        val DIR_IQS = "${DIR_ROOT}/IQS/"

        /** 예상경로 : /storage/emulated/0/IQS/LOG/ */
        val DIR_LOG = "${DIR_IQS}LOG/"

        // 이미지, 비디오, 사운드, 패치파일을 저장하는 디렉토리 경로(디바이스)
        /** 예상경로 : /storage/emulated/0/IQS/Image/ */
        val DIR_IMAGE = "${DIR_IQS}Image/"
        /** 예상경로 : /storage/emulated/0/IQS/Video/ */
        val DIR_VIDEO = "${DIR_IQS}Video/"
        /** 예상경로 : /storage/emulated/0/IQS/Sound/ */
        val DIR_SOUND = "${DIR_IQS}Sound/"
        /** 예상경로 : /storage/emulated/0/IQS/Patch/ */
        val DIR_PATCH = "${DIR_IQS}Patch/"
        /** 경로 : dicontrol/agent/resource/image/ */
        val SUB_PATH_IMAGE = "dicontrol/agent/resource/image/"
        /** 경로 : dicontrol/agent/resource/movie/ */
        val SUB_PATH_VIDEO = "dicontrol/agent/resource/movie/"
        /** 경로 : dicontrol/agent/resource/sound/ */
        val SUB_PATH_SOUND = "dicontrol/agent/resource/sound/"
        /** 경로 : dicontrol/agent/patch/ */
        val SUB_PATH_PATCH = "dicontrol/agent/patch/"
        /** 경로 : dicontrol/agent/DisplayLog/ */
        val SUB_PATH_LOG: String = "dicontrol/agent/DisplayLog/"
        /** 예상경로 : /storage/emulated/0/IQS/DownLoadSound/ */
        val DIR_DOWNLOAD_SOUND = "${DIR_IQS}DownLoadSound/"
        /** 예상경로 : /storage/emulated/0/IQS/DownLoadVideo/ */
        val DIR_DOWNLOAD_VIDEO = "${DIR_IQS}DownLoadVideo/"
        /** 경로 /sys/class/net/eth0/address */
        val FILE_MAC_ADDRESS = "/sys/class/net/eth0/address"
        // 이미지 비디오 사운드 패치파일을 가지고 있는 디렉토리 경로(IQS)

        /** 예상경로 : /data/data/com.kct.iqsdisplayer/shared_prefs/ */
        lateinit var DIR_SHARED_PREFS: String

        object Server {

        }
    }

    object Name {
        fun getLogFileName(): String {
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            return "$today.txt"
        }
        /**
         *     //  내용                       KEY
         *       표시기IP                DisplayerIP
         *       사운드 파일 위치          PathSoundFile
         *       홍보영상 파일 위치        PathVideoFile
         *       이미지 파일 위치          PathImgFile
         *       발행기 IP               Iqs_IP
         *       발행기 PORT             Iqs_PORT
         *       FTP 서버 IP             FTPServerIP
         *       FTP 서버 PORT           FTPServerPORT
         *       FTP 설정 파일 위치        FTPServerSettingPath
         *       FTP 사용자명             FTPUserID
         *       FTP 비밀번호             FTPUserPW
         *       백업 서버 IP             BkServerIP
         *       백업 서버 PORT           BkServerPORT
         *       보조 순번 표시            SubDisplayer
         */
        const val PREF_DISPLAYER_SETTING = "iqs_displayer_setting"
        fun getPrefDisplayerSettingName() = "$PREF_DISPLAYER_SETTING.xml"
        /**
         *    // 내용                       KEY
         *    하단안내문구               StatusText
         */
        const val PREF_DISPLAY_INFO = "iqs_display_info"
        fun getPrefDisplayInfoName() = "$PREF_DISPLAY_INFO.xml"

        object Key {
            object DisplayerSettingKeys {
                /** 실제 키값 : DisplayerIP */
                const val DISPLAYER_IP = "DisplayerIP"
                /** 실제 키값 :  PathSoundFile */
                const val PATH_SOUND_FILE = "PathSoundFile"
                /** 실제 키값 :  PathVideoFile */
                const val PATH_VIDEO_FILE = "PathVideoFile"
                /** 실제 키값 :  PathImgFile */
                const val PATH_IMG_FILE = "PathImgFile"
                /** 실제 키값 :  Iqs_IP */
                const val IQS_IP = "Iqs_IP"
                /** 실제 키값 :  Iqs_PORT */
                const val IQS_PORT = "Iqs_PORT"
                /** 실제 키값 :  FTPServerIP */
                const val FTP_SERVER_IP = "FTPServerIP"
                /** 실제 키값 :  FTPServerPORT */
                const val FTP_SERVER_PORT = "FTPServerPORT"
                /** 실제 키값 :  FTPServerSettingPath */
                const val FTP_SERVER_SETTING_PATH = "FTPServerSettingPath"
                /** 실제 키값 :  FTPUserID */
                const val FTP_USER_ID = "FTPUserID"
                /** 실제 키값 :  FTPUserPW */
                const val FTP_USER_PW = "FTPUserPW"
                /** 실제 키값 :  BkServerIP */
                const val BK_SERVER_IP = "BkServerIP"
                /** 실제 키값 :  BkServerPORT */
                const val BK_SERVER_PORT = "BkServerPORT"
                /** 실제 키값 :  CallView */
                const val CALL_VIEW = "CallView"
            }

            object DisplayInfoKeys {
                /** 실제 키값 :  StatusText */
                const val STATUS_TEXT = "StatusText"
            }
        }
    }

    object CommunicationInfo {
        /** 기본값 1.1.1.100 */
        var IQS_IP: String = "1.1.1.100"
        /** 기본값 8697 */
        var IQS_PORT: Int = 8697
        /** 기본값 8697 */
        //var TCP_PORT: Int = 8697
        /** 기본값 8696 */
        var UDP_PORT: Int = 8696
        // 2022.07.20 dyyoon 파일 서버 배포 포트
        /** 기본값 9901 */
        var FILE_SERVER_PORT: Int = 9901
        /** 기본값 21 */
        var FTP_PORT: Int = 21
        /** 기본값 anonymous */
        var FTP_ID: String = "anonymous"
        /** 기본값 kcikci */
        var FTP_PW: String = "kcikci"

        /** 기본값 null */
        var MY_IP: String? = null
        /** 기본값 null */
        var MY_MAC: String? = null
        /** 기본값 0x02 */
        var MODE: Int = 0x02
        /** 기본값 10,08,07,1000 */
        var VERSION: String = "10,08,07,1000"
        /** 기본값 10,1,102,64 */
        var FLASH_VERSION: String = "10,1,102,64"

        // 20191216 symoon 호출 확대화면 추가
        /** 기본값 0 */
        var CALLVIEW_MODE: String = "0"     // 표시기 화면 타입, ScreenInfo에 DisplayType과 혼용되고 있어 ScreenInfo에서는 삭제하였음.

        /** SharedPreferences값을 CommunicationInfo로 복사한다.
         * SharedPreferences에 없으면 기본값을 유지한다.*/
        fun  SharedPreferences.loadCommunicationInfo() {
            IQS_IP = getString(Name.Key.DisplayerSettingKeys.IQS_IP, IQS_IP)!!
            IQS_PORT = getInt(Name.Key.DisplayerSettingKeys.IQS_PORT, IQS_PORT)

            FTP_PORT = getInt(Name.Key.DisplayerSettingKeys.FTP_SERVER_PORT, FTP_PORT)
            FTP_ID = getString(Name.Key.DisplayerSettingKeys.FTP_USER_ID, FTP_ID) ?: FTP_ID
            FTP_PW = getString(Name.Key.DisplayerSettingKeys.FTP_USER_PW, FTP_PW) ?: FTP_PW

            CALLVIEW_MODE = getString(Name.Key.DisplayerSettingKeys.CALL_VIEW, CALLVIEW_MODE) ?: CALLVIEW_MODE
            //TODO : 우선은 보이는 것들만 옮겨두었음. 추가로 Load할 것이 있을 수 있음.
        }
    }

    object Handle {
        const val TIMEOUT_CAHNGE_FRAGMENT_TIME = 5000L //화면 전환 타임
        const val TIMEOUT_CAHNGE_FRAGMENT_MESSAGE = 0 //화면 전환 메시지 define
        const val RETRY_SERVICE_TIME = 5000L // 서비스 retry 타임
        const val RETRY_SERVICE_MESSAGE = 1001 //서비스 retry 메시지
    }

    //==============================================================================================
    // 예비로 남겨둠.
    //==============================================================================================
    @Deprecated("버전들을 정리하려고 만들었으나 별로 필요없을 것으로 보임")
    object Version {
        val API_LEVEL = Build.VERSION.SDK_INT
    }

    @Deprecated("Error들을 정리하려고 만들었으나 별로 필요없을 것으로 보임")
    enum class Error(val message: String) {
        NONE(""),
        FAIL_GET_EXTERNAL_STORAGE("외부 저장소 경로(Root)를 가져오는 데 실패했습니다.")
    }

    @Deprecated("File들을 정리하려고 만들었으나 별로 필요없을 것으로 보임")
    object File {
        val logFile: java.io.File
            get() = File(Path.DIR_LOG, Name.getLogFileName())

        val prefDisplayerSetting
            get() = File(Path.DIR_SHARED_PREFS, Name.getPrefDisplayerSettingName())

        val prefDisplayInfo
            get() = File(Path.DIR_SHARED_PREFS, Name.getPrefDisplayInfoName())
    }
}