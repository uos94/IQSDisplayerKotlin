package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

data class WaitResponse(
    val ticketNum: Int,     // 발권 번호
    val winNum: Int,        // 창구 번호
    val waitNum: Int        // 창구 대기 인수
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.WAIT_RESPONSE
) : BaseReceivePacket() {
    override fun toString(): String {
        return "WaitResponse(ticketNum=$ticketNum, winNum=$winNum, waitNum=$waitNum)"
    }
}

fun Packet.toWaitResponse(): WaitResponse {
    return WaitResponse(
        ticketNum = integer,
        winNum = integer,
        waitNum = integer
    )
}