package com.kct.iqsdisplayer.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.kct.iqsdisplayer.common.Const
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun AppCompatActivity.setFullScreen() {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        supportActionBar?.hide()    //상단 Action Bar Hide

        // 이값이 True면 내부적으로 SYSTEM UI LAYOUT FLAG값들을 살펴보고 해당 값들을 토대로 화면을 구성하게 된다.
        // 따라서 False로 해야 이제 사라질(Deprecated) Flag값들을 무시하고 Window Insets를 통해 화면을 구성하게 된다.
        window.setDecorFitsSystemWindows(false)
        //상태바와 네비게이션바를 Hide
        window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        //스와이프를 통해 시스템바가 나타나도록 설정
        window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // setOnApplyWindowInsetsListener를 사용하여 컨텐츠 영역 조정, 화면에 어떻게 나올지 몰라 남겨둠
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            val systemBars = insets.getInsets(WindowInsets.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    else {
        supportActionBar?.hide()    //Action Nar를 숨기지 않으면 뜨는 경우가 있다고 함.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
        //window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}

fun makeDir(path: String): File? {
    val dir = File(path)
    if (!dir.exists()) {
        try {
            dir.mkdirs()
        } catch (e: SecurityException) {
            Log.e("SecurityException: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    return dir
}

fun deleteFile(filePath: String): Boolean {
    val fileToDelete = File(filePath)

    return if (fileToDelete.exists() && fileToDelete.isFile && fileToDelete.canWrite()) {
        fileToDelete.delete()
    } else {
        // 삭제 불가능한 경우 false 반환
        false
    }
}

fun String.isExistFile(): Boolean {
    val file = File(this)
    return file.exists() && file.isFile
}

fun copyFile(sourcePath: String, destPath: String): Boolean {
    try {
        val sourceFile = File(sourcePath)
        val destFile = File(destPath)

        val destDir = destFile.parentFile
        if (destDir != null && !destDir.exists()) {
            destDir.mkdirs()
        }

        var copiedSize: Long = 0

        FileInputStream(sourceFile).channel.use { inputChannel ->
            FileOutputStream(destFile).channel.use { outputChannel ->
                val bufferSize = 8192 // 8KB 버퍼 사용
                val buffer = ByteBuffer.allocateDirect(bufferSize)

                while (inputChannel.read(buffer) != -1) {
                    buffer.flip()
                    outputChannel.write(buffer)
                    buffer.clear()

                    copiedSize += bufferSize
                }
            }
        }
        return true // 파일 복사 성공
    } catch (e: IOException) {
        e.printStackTrace()
        return false // 파일 복사 실패
    }
}

suspend fun copyFileCoroutine(sourcePath: String, destPath: String): Boolean {
    return CoroutineScope(Dispatchers.IO).async {
        try {
            val sourceFile = File(sourcePath)
            val destFile = File(destPath)

            val destDir = destFile.parentFile
            if (destDir != null && !destDir.exists()) {
                destDir.mkdirs()
            }

            var copiedSize: Long = 0

            FileInputStream(sourceFile).channel.use { inputChannel ->
                FileOutputStream(destFile).channel.use { outputChannel ->
                    val bufferSize = 8192
                    val buffer = ByteBuffer.allocateDirect(bufferSize)

                    while (inputChannel.read(buffer) != -1) {
                        buffer.flip()
                        outputChannel.write(buffer)
                        buffer.clear()

                        copiedSize += bufferSize
                    }
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }.await()
}

fun copyFilePopup(context: Context, sourcePath: String, destPath: String) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("파일 복사 중")
    val progressBar = ProgressBar(context)
    builder.setView(progressBar)
    val dialog = builder.create()
    dialog.setCancelable(false)
    dialog.show()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val sourceFile = File(sourcePath)
            val destFile = File(destPath)

            val totalSize = sourceFile.length()
            var copiedSize: Long = 0

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (input.read(buffer).also { length = it } > 0) {
                        output.write(buffer, 0, length)
                        copiedSize += length
                        val progress = (copiedSize * 100 / totalSize).toInt()
                        withContext(Dispatchers.Main) {
                            progressBar.progress = progress
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                dialog.dismiss()
                // 복사 성공
            }
        } catch (e: IOException) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                dialog.dismiss()
                // 복사 실패
            }
        }
    }
}

fun saveFile(fileName: String, directory: String, data: ByteArray) {
    val dir = File(directory)
    if (!dir.exists()) dir.mkdirs()

    val file = File(directory, fileName)
    FileOutputStream(file, true).use { fos ->
        fos.write(data)
    }
}

fun String.getFileExtension(): String {
    if (isNullOrEmpty()) {
        return ""
    }

    val lastIndexOfDot = lastIndexOf(".")
    return if (lastIndexOfDot > 0 && lastIndexOfDot < length - 1) {
        substring(lastIndexOfDot + 1)
    } else {
        ""
    }
}

inline fun <reified T> Context.setPreference(prefName: String, key: String, value: T) {
    val sp: SharedPreferences = getSharedPreferences(prefName, Activity.MODE_PRIVATE)
    val editor = sp.edit()

    when (value) {
        is String   -> editor.putString(key, value)
        is Int      -> editor.putInt(key, value)
        // 필요한 경우 다른 타입에 대한 처리 추가 (예: Boolean, Long, Float 등)
        else -> throw IllegalArgumentException("Unsupported type")
    }

    editor.apply()
}

inline fun <reified T> Context.getPreference(prefName: String, key: String, defaultValue: T): T {
    val sp: SharedPreferences = getSharedPreferences(prefName, Activity.MODE_PRIVATE)

    return when (T::class) {
        String::class   -> sp.getString(key, defaultValue as? String) as T
        Int::class      -> sp.getInt(key, defaultValue as? Int ?: 0) as T
        // 필요한 경우 다른 타입에 대한 처리 추가 (예: Boolean, Long, Float 등)
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

/** IPv4 얻어오기 */
fun getLocalIpAddress(): String? {
    try {
        return NetworkInterface.getNetworkInterfaces()
            .toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
            ?.hostAddress
    } catch (e: SocketException) {
        e.printStackTrace()
        return null
    }
}

/** 기기 맥주소 반환, 일반적으로 유선 네트워크 인터페이스(Ethernet)의 MAC 주소*/
fun getMacAddress(): String? {
    try {
        val filePath = Const.Path.FILE_MAC_ADDRESS
        File(filePath).useLines { lines ->
            return lines.firstOrNull()?.uppercase()?.substring(0, 17)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e("기기 맥주소 반환 실패[IOException]" )
        return null
    }
}

/** wlan0 네트워크 인터페이스의 MAC 주소 반환, wlan0은 Wi-Fi 네트워크 인터페이스 */
fun getMacAddress2(): String {
    try {
        return NetworkInterface.getNetworkInterfaces()
            .toList()
            .firstOrNull { it.name.equals("wlan0", ignoreCase = true) }
            ?.hardwareAddress
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(":") { String.format("%02x", it) } ?: ""
    } catch (ex: Exception) {
        ex.printStackTrace()
        return ""
    }
}

fun rebootIQSDisplayer() {
    val pb = ProcessBuilder("su", "-c", "/system/bin/reboot")
    try {
        val process = pb.start()
        process.waitFor()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun String.removeChar(replacement: String) = this.replace(replacement, "")

fun String.splitData(delimiter: String): Array<String> {
    return if (this.endsWith(delimiter)) {
        this.split(delimiter.toRegex()).dropLast(1).toTypedArray()
    } else {
        this.split(delimiter.toRegex()).toTypedArray()
    }
}

fun <T> String?.splitToArrayList(delimiter: String, transform: (String) -> T): ArrayList<T> {
    if (this.isNullOrEmpty()) return arrayListOf()

    val splitList = this.split(delimiter)
    val resultList = ArrayList<T>()

    for (item in splitList) {
        resultList.add(transform(item)) // 변환 함수 적용 후 결과 리스트에 추가
    }

    return resultList
}

fun getCurrentTimeFormatted(): String {
    val currentTime = Date()
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(currentTime)
}

fun Context.installApk(fileName: String) {
    val filePath = Const.Path.DIR_PATCH + fileName
    val file = File(filePath)
    if (filePath.isEmpty() || file.length() <= 0 || !file.exists() || !file.isFile) {
        Log.d("Not exist File")
        return
    }

    // FileProvider를 사용하여 content Uri 생성
    val contentUri = FileProvider.getUriForFile(this, "com.kct.iqsdisplayer.provider", file)

    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // FileProvider에 권한 부여
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

// TODO : 기존에 설치하던 방식인데. 이러면 설치완료 시점을 알 수 없어 앱 재시작이 힘들다. 폐기 예정
fun installSilent(packageName: String, fileName: String): Int {
    Log.d("신규앱 설치 시작..File Name : $fileName")

    val filePath = Const.Path.DIR_PATCH + fileName
    val file = File(filePath)
    if (fileName.isEmpty() || file.length() <= 0 || !file.exists() || !file.isFile) {
        Log.d("Not exist File")
        return 1
    }

    val args = arrayOf("pm", "install", "-i", packageName, filePath)

    val processBuilder = ProcessBuilder(*args) // *args를 사용하여 배열을 펼쳐서 전달
    var process: Process? = null
    var successResult: BufferedReader? = null
    var errorResult: BufferedReader? = null
    val successMsg = StringBuilder()
    val errorMsg = StringBuilder()

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
    } catch (e: Exception) {
        e.printStackTrace()
        Log.d("Fail Process Exception")
    } finally {
        successResult?.close()
        errorResult?.close()
        process?.destroy()
    }

    val result: Int = if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
        0
    } else {
        2
    }
    Log.d("successMsg: $successMsg, ErrorMsg: $errorMsg")

    return result
}