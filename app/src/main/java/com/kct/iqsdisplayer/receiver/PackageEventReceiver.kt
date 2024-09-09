package com.kct.iqsdisplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class PackageEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == Intent.ACTION_PACKAGE_REPLACED) {
            // Broadcast Action: A new version of an application package has been installed, replacing an existing version that was previously installed.
            // 새로운 버전의 앱 패키지가 설치 되거나 업데이트 되었을 때

            // do something...
        }
    }
}