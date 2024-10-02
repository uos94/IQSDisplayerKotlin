package com.kct.iqsdisplayer.common

import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.copyFile
import com.kct.iqsdisplayer.util.deleteFile
import java.io.File
import java.io.FileOutputStream

object UpdateManager {
    private var fileSize: Long = 0
    private var fileName: String = ""
    private var fileExtension = ""

    private var downloadTempDir = ""
    private var sourceFilePath = ""
    private var targetFilePath = ""

    fun setUpdateFileInfo(downloadFileSize: Int, downloadFileName: String) {
        Log.d("파일 이름: $downloadFileName, 파일 사이즈: $downloadFileSize")
        fileSize = downloadFileSize.toLong()
        fileName = downloadFileName
        fileExtension = downloadFileName.substringAfterLast(".", "").lowercase()

        downloadTempDir = when (fileExtension) {
            "apk"                       -> Const.Path.DIR_PATCH
            "wav"                       -> Const.Path.DIR_DOWNLOAD_SOUND
            "jpg", "jpeg", "png", "mp4" -> Const.Path.DIR_DOWNLOAD_VIDEO
            else -> {
                Log.e("정의되지 않은 파일 extension: $fileExtension")
                return // 지원하지 않는 파일 형식인 경우 함수 종료
            }
        }

        val (sourceDir, targetDir) = when (fileExtension) {
            "apk"                       -> Const.Path.DIR_PATCH to Const.Path.DIR_PATCH // apk는 같은 디렉토리에 저장되므로 sourceDir와 targetDir가 같습니다.
            "wav"                       -> Const.Path.DIR_DOWNLOAD_SOUND to Const.Path.DIR_SOUND
            "jpg", "jpeg", "png", "mp4" -> Const.Path.DIR_DOWNLOAD_VIDEO to Const.Path.DIR_VIDEO
            else -> {
                Log.e("Unsupported file extension: $fileExtension")
                return // 지원하지 않는 파일 형식인 경우 함수 종료
            }
        }

        this.sourceFilePath = sourceDir + fileName
        this.targetFilePath = targetDir + fileName

        val deleteFilePath = downloadTempDir + downloadFileName
        File(deleteFilePath).delete() // 파일 삭제
    }

    fun getFileName() = fileName
    fun getFileExtension() = fileExtension
    fun isCompleteDownload() = File(sourceFilePath).length() >= fileSize

    fun writeData(dataArray: ByteArray?) {
        try {
            dataArray ?: return

            // sourceFilePath에 해당하는 파일을 열고, append 모드로 FileOutputStream 생성
            FileOutputStream(sourceFilePath, true).also {
                it.write(dataArray)
                it.close()
            }
            // 현재까지 다운로드된 파일 크기
            val currentFileSize = File(sourceFilePath).length()

            // 완료 퍼센트 계산
            val percent = (currentFileSize * 100 / fileSize).toInt()
            // 로그 메시지에 완료 퍼센트 추가
            Log.d("파일 이름: $fileName, 진행중[$percent%] - $currentFileSize/$fileSize")

            if(isCompleteDownload()) {
                val copySuccess = copyFile(sourceFilePath, targetFilePath)
                if(copySuccess) deleteFile(sourceFilePath)
            }
        } catch (e: Exception) {
            // 예외 처리
            Log.e("파일 쓰기 실패: ${e.message}")
            e.printStackTrace()
        }
    }
}