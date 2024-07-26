package com.kct.iqsdisplayer.common

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
         * @see android.os.Environment.getExternalStorageDirectory()
         * **/
        lateinit var DIR_ROOT: String
        val DIR_IQS = "${DIR_ROOT}/IQS/"
        val DIR_LOG = "$DIR_IQS/LOG/"
        fun getLogFileName(): String {
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            return "$today.txt"
        }
    }

    object File {
        val logFile: java.io.File
            get() = File(Path.DIR_LOG, Path.getLogFileName())

        val someDir: java.io.File
            get() = File(Path.DIR_ROOT, "some_dir")
    }

    object Version {
        val API_LEVEL = Build.VERSION.SDK_INT
    }

    enum class Error(val message: String) {
        NONE(""),
        FAIL_GET_EXTERNAL_STORAGE("외부 저장소 경로(Root)를 가져오는 데 실패했습니다.")
    }
}