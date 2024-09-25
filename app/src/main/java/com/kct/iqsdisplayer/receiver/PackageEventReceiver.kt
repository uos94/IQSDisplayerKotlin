package com.kct.iqsdisplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kct.iqsdisplayer.BuildConfig
import com.kct.iqsdisplayer.ui.MainActivity
import com.kct.iqsdisplayer.util.Log

class PackageEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == Intent.ACTION_PACKAGE_REPLACED) {
            // Broadcast Action: A new version of an application package has been installed, replacing an existing version that was previously installed.
            // 새로운 버전의 앱 패키지가 설치 되거나 업데이트 되었을 때
            Log.d("업데이트 버전 설치 완료 : VERSION_NAME[${BuildConfig.VERSION_NAME}]")
            val intentMainActivity = Intent(context, MainActivity::class.java)
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentMainActivity)
        }
    }
}