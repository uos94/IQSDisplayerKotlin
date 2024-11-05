package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.INFO_MESSAGE_REQUEST 데이터 클래스
data class InfoMessage(
    val infoMessageWinNum: Int,// 창구 번호
    val infoMessage: String // 안내 메시지
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.INFO_MESSAGE_REQUEST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "InfoMessageRequest(infoMessageWinNum=$infoMessageWinNum, infoMessage='$infoMessage')"
    }
}

fun Packet.toInfoMessage(): InfoMessage {
    return InfoMessage(
        infoMessageWinNum = integer,
        infoMessage = string
    )
}