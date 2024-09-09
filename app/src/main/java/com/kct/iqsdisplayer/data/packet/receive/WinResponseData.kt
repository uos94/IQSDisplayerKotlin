package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.network.Packet

// ProtocolDefine.WIN_RESPONSE 데이터 클래스
data class WinResponseData(
    val winIDList: String,  // 창구 ID 리스트
    val winNmList: String,  // 창구명 리스트
    val waitList: String    // 대기 인수 리스트
) {
    override fun toString(): String {
        return "WinResponseData(winIDList='$winIDList', winNmList='$winNmList', waitList='$waitList')"
    }
}
fun Packet.toWinResponseData(): WinResponseData {
    return WinResponseData(
        winIDList = string,
        winNmList = string,
        waitList = string
    )
}