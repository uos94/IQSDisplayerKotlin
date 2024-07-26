package com.kct.iqsdisplayer.util

import android.os.Environment
import com.kct.iqsdisplayer.common.Const
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LogFile {
    private val logFullPath = Const.Path.DIR_LOG + Const.Path.getLogFileName()

    init {
        createDir()
    }

    fun write(data: String) {
        val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val data = "$nowTime : $data\r"

        val buf = BufferedWriter(FileWriter(logFullPath, true))
        try {
            buf.append(data)
            buf.newLine()
            buf.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        finally {
            buf.close()
        }
    }

    private fun createDir() {
        val logFolderPath = Environment.getExternalStorageDirectory().absolutePath + "/IQS/Log/"

        val dir = File(logFolderPath)

        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    companion object {
        fun write(s: String) {

        }
    }

}