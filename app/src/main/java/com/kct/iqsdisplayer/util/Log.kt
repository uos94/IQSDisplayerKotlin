package com.kct.iqsdisplayer.util

import android.util.Log
import com.kct.iqsdisplayer.BuildConfig

object Log {
    private const val TAG = "HISON"
    private var isEnabled = BuildConfig.DEBUG // 빌드 타입에 따라 로그 출력 여부 설정

    fun enable(isEnable: Boolean) {
        isEnabled = isEnable
    }

    private fun log(level: Int, strMsg: String, tr: Throwable? = null) {
        if (isEnabled) {
            val callerElement = Exception().stackTrace[1]
            val logMsg = "[${callerElement.fileName} ${callerElement.lineNumber}] $strMsg"
            when (level) {
                Log.ERROR -> Log.e(TAG, logMsg, tr)
                Log.WARN -> Log.w(TAG, logMsg, tr)
                Log.INFO -> Log.i(TAG, logMsg, tr)
                Log.DEBUG -> Log.d(TAG, logMsg, tr)
                Log.VERBOSE -> Log.v(TAG, logMsg, tr)
            }
        }
    }

    fun e(strMsg: String, tr: Throwable? = null) = log(Log.ERROR, strMsg, tr)
    fun w(strMsg: String, tr: Throwable? = null) = log(Log.WARN, strMsg, tr)
    fun i(strMsg: String, tr: Throwable? = null) = log(Log.INFO, strMsg, tr)
    fun d(strMsg: String, tr: Throwable? = null) = log(Log.DEBUG, strMsg, tr)
    fun v(strMsg: String, tr: Throwable? = null) = log(Log.VERBOSE, strMsg, tr)

    fun s() {
        if (isEnabled) {
            val e = Exception("STACK TRACE")
            Log.v(TAG, "Method called on the UI thread", e)
        }
    }
}