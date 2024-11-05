package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

data class WaitData(
    val ticketNum: Int,     // 발권 번호
    val winId: Int,        // 창구 ID
    val waitNum: Int        // 창구 대기 인수
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.WAIT_RESPONSE
) : BaseReceivePacket() {
    override fun toString(): String {
        return """
                |WaitResponse(
                |   ticketNum=$ticketNum, 
                |   winId=$winId, 
                |   waitNum=$waitNum)""".trimMargin()
    }
}

fun Packet.toWaitData(): WaitData {
    return WaitData(
        ticketNum = integer,
        winId = integer,
        waitNum = integer
    )
}