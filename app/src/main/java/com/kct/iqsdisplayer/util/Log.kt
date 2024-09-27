package com.kct.iqsdisplayer.util

import android.util.Log
import android.widget.TextView
import com.kct.iqsdisplayer.BuildConfig
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.ProtocolDefine
import java.io.BufferedWriter
import java.io.File
import java.io.File.separator
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Log {
    private const val TAG = "HISON"
    private var isEnabled = BuildConfig.DEBUG // 빌드 타입에 따라 로그 출력 여부 설정
    private const val MAX_LOG_LINES = 1000 // View에 보여줄 최대 로그 줄 수
    private val logLines = ArrayList<String>()
    private var logListener: OnLogEventListener? = null

    private val logFile = LogFile()
    private var isFileWriteEnabled = true
    private var isFileWriteLevel = Log.VERBOSE

    fun enable(isEnable: Boolean) {
        isEnabled = isEnable
    }

    private fun log(level: Int, strMsg: String, tr: Throwable? = null) {
        if (isEnabled) {
            val callerElement = Exception().stackTrace[3]
            val logMsg = "[${callerElement.fileName} ${callerElement.lineNumber}] $strMsg"
            when (level) {
                Log.ERROR -> Log.e(TAG, logMsg, tr)
                Log.WARN -> Log.w(TAG, logMsg, tr)
                Log.INFO -> Log.i(TAG, logMsg, tr)
                Log.DEBUG -> Log.d(TAG, logMsg, tr)
                Log.VERBOSE -> Log.v(TAG, logMsg, tr)
            }

            if (isFileWriteEnabled) {
                logFile.write(logMsg)
            }

            logLines.add(logMsg)
            if (logLines.size > MAX_LOG_LINES) {
                logLines.removeAt(0) // 가장 오래된 로그 삭제
            }

            logListener?.onLog(logMsg)
        }
    }

    fun enableLogFile(isFileWrite: Boolean, level: Int = Log.VERBOSE) {
        isFileWriteEnabled = isFileWrite
        isFileWriteLevel = level
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

    fun getLogHistory() = logLines.joinToString(separator = System.lineSeparator() )
    fun setOnLogEventListener(listener: OnLogEventListener?) {
        this.logListener = listener
    }
    interface OnLogEventListener {
        fun onLog(logMessage: String)
    }

    private class LogFile {
        private val logFullPath = Const.Path.DIR_LOG + Const.Name.getLogFileName()

        init {
            createDir()
        }

        fun write(data: String) {
            val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logLine = "$nowTime : $data"

            val buf = BufferedWriter(FileWriter(logFullPath, true))
            try {
                buf.append(logLine)
                buf.newLine()
                buf.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                buf.close()
            }
        }

        private fun createDir() {
            val dir = File(Const.Path.DIR_LOG)

            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
    }
}