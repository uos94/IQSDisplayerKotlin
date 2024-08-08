package com.kct.iqsdisplayer.data.packet

import com.kct.iqsdisplayer.network.Packet

data class WaitResponse(
    val ticketNum: Int,     // 발권 번호
    val winID: Int,         // 창구 번호
    val waitNum: Int        // 창구 대기 인수
) {
    override fun toString(): String {
        return "WaitResponse(ticketNum=$ticketNum, winID=$winID, waitNum=$waitNum)"
    }
}

fun Packet.toWaitResponse(): WaitResponse {
    return WaitResponse(
        ticketNum = integer,
        winID = integer,
        waitNum = integer
    )
}