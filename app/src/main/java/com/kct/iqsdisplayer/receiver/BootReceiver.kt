package com.kct.iqsdisplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.kct.iqsdisplayer.ui.MainActivity
import com.kct.iqsdisplayer.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BOOT_COMPLETED")
        val intentMainActivity = Intent(context, MainActivity::class.java)
        intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intentMainActivity)
        Toast.makeText(context, "표시기 시작", Toast.LENGTH_SHORT).show()
    }
}
