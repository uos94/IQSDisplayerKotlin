package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.util.Log
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.FileInputStream
import java.io.FileOutputStream

class ConnectFTP {

    private val ftpClient = FTPClient().apply {
        controlEncoding = "euc-kr"
    }
    var reConnect = false // 접속 시 기본 파일 다 받아왔었는지 확인 (서비스 재연결 시 중복 다운로드 방지)
    private var appVer: Int = 0 // 패치 파일 받아오기 전 앱 버전 확인 용

    fun setAppVer(appVer: Int) {
        this.appVer = appVer
    }

    fun ftpConnect(host: String, username: String, password: String, port: Int): Boolean {
        return try {
            ftpClient.connect(host, port)
            FTPReply.isPositiveCompletion(ftpClient.replyCode) && ftpClient.login(username, password).also {
                if (it) ftpClient.enterLocalPassiveMode()
            }
        } catch (e: Exception) {
            Log.d("Failed connected to host")
            false
        }
    }

    fun ftpDisconnect(): Boolean {
        return try {
            ftpClient.logout()
            ftpClient.disconnect()
            true
        } catch (e: Exception) {
            Log.d("Failed disconnected to host")
            false
        }
    }

    fun ftpGetDirectory(): String? {
        return try {
            ftpClient.printWorkingDirectory()
        } catch (e: Exception) {
            Log.d("Failed get Directory")
            null
        }
    }

    fun ftpChangeDirectory(directory: String): Boolean {
        return try {
            ftpClient.changeWorkingDirectory(directory)
        } catch (e: Exception) {
            Log.d("failed ChangeDirectory")
            false
        }
    }

    fun ftpGetFileList(directory: String): Array<String> {
        return try {
            ftpClient.listFiles(directory)
                .filter { it.isFile } // 파일만 필터링
                .map { it.name }
                .toTypedArray()
        } catch (e: Exception) {
            Log.d("Failed GetFileList")
            emptyArray()
        }
    }

    fun ftpCreateDirectory(directory: String): Boolean {
        return try {
            ftpClient.makeDirectory(directory)
        } catch (e: Exception) {
            Log.e("Failed make the directory ($directory)")
            false
        }
    }

    fun ftpDeleteDirectory(directory: String): Boolean {
        return try {
            ftpClient.removeDirectory(directory)
        } catch (e: Exception) {
            Log.e("Failed ftpDeleteDirectory ($directory)")
            false
        }
    }

    fun ftpDeleteFile(file: String): Boolean {
        return try {
            ftpClient.deleteFile(file)
        } catch (e: Exception) {
            Log.d("Failed delete file")
            false
        }
    }

    fun ftpRenameFile(from: String, to: String): Boolean {
        return try {
            ftpClient.rename(from, to)
        } catch (e: Exception) {
            Log.e("Failed RenameFile from:$from to:$to")
            false
        }
    }

    fun ftpDownloadFile(srcFilePath: String, desFilePath: String, list: Array<String>, category: String): Boolean {
        var fileName: String? = null
        try {
            list.forEach {
                it.let { nonNullFileName ->
                    fileName = nonNullFileName
                    if (category != "Patch" || compareVersion(nonNullFileName)) {
                        val ftpFilePath = srcFilePath + nonNullFileName
                        val downloadPath = desFilePath + nonNullFileName

                        ftpClient.apply {
                            setFileType(FTP.BINARY_FILE_TYPE)
                            setFileTransferMode(FTP.BINARY_FILE_TYPE)
                        }

                        FileOutputStream(downloadPath).use { fos ->
                            ftpClient.retrieveFile(ftpFilePath, fos)
                        }
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("Failed DownloadFile : $fileName")
            return false
        }
    }


    fun ftpUploadFile(srcFilePath: String, desFileName: String, desDirectory: String): Boolean {
        return try {
            FileInputStream(srcFilePath).use { fis ->
                if (ftpChangeDirectory(desDirectory)) {
                    ftpClient.storeFile(desFileName, fis)
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("Failed UploadFile")
            false
        }
    }

    // 패치 파일 버전 비교
    private fun compareVersion(fileName: String): Boolean {
        var result = false
        if (fileName.contains("apk", ignoreCase = true) && fileName.contains("IQSDisplay", ignoreCase = true)) {
            val fileVersionStr = fileName.replace("IQSDisplay", "", ignoreCase = true).replace(".apk", "", ignoreCase = true)
            val fileVersion = fileVersionStr.split("\\.".toRegex()).toTypedArray()

            if (fileVersion.size == 3) {
                try {
                    val nFileVer = (fileVersion[0].toInt() * 100) + (fileVersion[1].toInt() * 10) + fileVersion[2].toInt()
                    if (nFileVer > appVer) {
                        result = true
                    }
                } catch (e: Exception) {
                    result = false
                }
            }
        }
        return result
    }
}
