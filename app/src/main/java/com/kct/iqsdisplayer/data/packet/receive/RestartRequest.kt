package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.RESTART_REQUEST 데이터 클래스
data class RestartRequest(
    val mode: Int,// 표시기 모드 (2:창구표시기, 6:보조표시기)
    val restartWinNum: Int// 창구번호
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.RESTART_REQUEST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "RestartRequest(mode=$mode, restartWinNum=$restartWinNum)"
    }
}
fun Packet.toRestartRequest(): RestartRequest {
    return RestartRequest(
        mode = integer,
        restartWinNum = integer
    )
}