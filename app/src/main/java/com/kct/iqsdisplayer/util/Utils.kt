package com.kct.iqsdisplayer.util

import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import java.io.File

/**
 * 보안키패드 비밀번호 검증 시 연속된 문자인지 확인
 * 3글자 이상 연속 되는 문자면 연속된 것으로 본다.
 */
fun isSequential(bytes: ByteArray): Boolean {
    for (i in 2..bytes.lastIndex) {
        if (bytes[i] - bytes[i - 1] == 1 && bytes[i - 1] - bytes[i - 2] == 1 ||
            bytes[i] - bytes[i - 1] == -1 && bytes[i - 1] - bytes[i - 2] == -1
        ) {
            return true
        }
    }
    return false
}

/**
 * 보안키패드 비밀번호 검증 시 반복되는 문자인지 확인
 * 3글자 이상 반복 되는 문자면 반복된 것으로 본다.
 */fun isRepeat(bytes: ByteArray): Boolean {
    for (i in 2..bytes.lastIndex) {
        if (bytes[i] == bytes[i - 1] && bytes[i - 1] == bytes[i - 2]) {
            return true
        }
    }
    return false
}

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
