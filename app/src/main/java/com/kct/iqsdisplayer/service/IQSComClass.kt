package com.kct.iqsdisplayer.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ResultReceiver
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.LogFile

/**
 * 순번발행기와 TCP UDP FTP 통신을 하기 위한 서비스 클래스
 * 통신은 Thread를 통해 개별로 수행
 * HISON : Coroutine으로 다 빼려고 했으나 덩어리가 커서 일단 그대로 감. 추후 변경 요망
 */
class IQSComClass : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("IQSComClass onCreate")
        LogFile.write("IQSComClass onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("IQSComClass onStartCommand")

        var commResultReceiver = intent.getParcelableExtra<ResultReceiver>("receiver")
        return START_NOT_STICKY
    }
}