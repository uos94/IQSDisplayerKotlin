package com.kct.iqsdisplayer.data.packet

import com.kct.iqsdisplayer.network.Packet

// ProtocolDefine.INFO_MESSAGE_REQUEST 데이터 클래스
data class InfoMessageRequestData(
    val infoMessageWinNum: Int,// 창구 번호
    val infoMessage: String // 안내 메시지
) {
    override fun toString(): String {
        return "InfoMessageRequestData(infoMessageWinNum=$infoMessageWinNum, infoMessage='$infoMessage')"
    }
}

fun Packet.toInfoMessageRequestData(): InfoMessageRequestData {
    return InfoMessageRequestData(
        infoMessageWinNum = integer,
        infoMessage = string
    )
}