package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.WIN_RESPONSE 데이터 클래스
data class WinInfos(
    val winIds: String,  // 창구 ID 리스트
    val winNames: String,  // 창구명 리스트
    val waitNums: String    // 대기 인수 리스트
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.WIN_RESPONSE
) : BaseReceivePacket() {
    override fun toString(): String {
        return "WinResponse(winIDList='$winIds', winNmList='$winNames', waitList='$waitNums')"
    }
}
fun Packet.toWinInfos(): WinInfos {
    return WinInfos(
        winIds = string,
        winNames = string,
        waitNums = string
    )
}