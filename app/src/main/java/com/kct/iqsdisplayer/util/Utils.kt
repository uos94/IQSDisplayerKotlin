package com.kct.iqsdisplayer.util

import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kct.iqsdisplayer.common.Const
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

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

fun copyFile(context: Context, sourcePath: String, destPath: String) {
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
    FileOutputStream(file).use { fos ->
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


fun String.removeChar(replacement: String) = this.replace(replacement, "")

fun String.splitData(delimiter: String): Array<String> {
    return if (this.endsWith(delimiter)) {
        this.split(delimiter.toRegex()).dropLast(1).toTypedArray()
    } else {
        this.split(delimiter.toRegex()).toTypedArray()
    }
}