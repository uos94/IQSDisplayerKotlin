package com.kct.iqsdisplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kct.iqsdisplayer.ui.MainActivity
import com.kct.iqsdisplayer.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BOOT_COMPLETED")
            val intentMainActivity = Intent(context, MainActivity::class.java)
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //context.startActivity(intentMainActivity)
        }
    }
}
