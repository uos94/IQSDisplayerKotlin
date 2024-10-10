package com.kct.iqsdisplayer.common

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

@Deprecated("서비스 삭제로 더 이상 사용 안함")
class CommResultReceiver(handler: Handler) : ResultReceiver(handler) {
    private var receiver: Receiver? = null

    fun interface Receiver {
        fun onReceiverResult(resultCode: Int, resultData: Bundle)
    }

    fun setReceiver(receiver: Receiver) {
        this.receiver = receiver
    }

    /**
     * super로 넘긴 Handler에 의해 UI 스레드에서 실행되므로,
     * UI 업데이트와 같은 작업을 안전하게 수행할 수 있음.
     */
    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        receiver?.onReceiverResult(resultCode, resultData)
    }
}
