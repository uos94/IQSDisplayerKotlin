package com.kct.iqsdisplayer.data.packet.send

import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.data.packet.BaseSendPacket
import com.kct.iqsdisplayer.network.ProtocolDefine

data class WaitRequest(
    val winNum: Int = ScreenInfo.instance.winID,
    val code: Short = ProtocolDefine.WAIT_REQUEST.value
): BaseSendPacket(code) {
    override fun toString(): String {
        return "WaitRequestData()"
    }

    override fun getDataArray(): Array<Any> {
        return arrayOf(winNum)
    }
}


