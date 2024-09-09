package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.network.Packet

// ProtocolDefine.RESTART_REQUEST 데이터 클래스
data class RestartRequestData(
    val mode: Int,// 표시기 모드 (2:창구표시기, 6:보조표시기)
    val restartWinNum: Int// 창구번호
) {
    override fun toString(): String {
        return "RestartRequestData(mode=$mode, restartWinNum=$restartWinNum)"
    }
}
fun Packet.toRestartRequestData(): RestartRequestData {
    return RestartRequestData(
        mode = integer,
        restartWinNum = integer
    )
}