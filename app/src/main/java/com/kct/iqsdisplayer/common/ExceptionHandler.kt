package com.kct.iqsdisplayer.common

import android.content.Context
import android.content.Intent
import com.kct.iqsdisplayer.ui.MainActivity
import com.kct.iqsdisplayer.util.Log

class ExceptionHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, exception: Throwable) {
        // 예외 처리 로직 (예: 로그 기록, 오류 보고 등)
        //Log.e("처리되지 않은 Exception 발생 : ${exception.message}")
        Log.s(exception)
        // 앱 재시작
        val intentMainActivity = Intent(context, MainActivity::class.java)
        intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intentMainActivity)

        // 현재 프로세스 종료
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}