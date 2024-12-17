package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

data class WaitData(
    val ticketNum: Int,     // 발권 번호..2024.12.17이제 이 데이터는 안쓸꺼라고 김희정수석님께 전달받음.
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