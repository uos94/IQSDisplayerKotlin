package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.EMPTY_REQUEST 데이터 클래스
data class PausedWorkData(
    val pausedWinNum: Int,      // 부재 설정 창구 직원 창구 번호
    val isPausedWork: Boolean,  // 부재 설정 여부
    val pausedMessage: String   // 부재 설정 메시지
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.PAUSED_WORK_REQUEST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "PausedWorkRequest(pausedWinNum=$pausedWinNum, isPaused=$isPausedWork, pausedMessage='$pausedMessage', protocolDefine=$protocolDefine)"
    }
}
fun Packet.toPausedWorkData(): PausedWorkData {
    return PausedWorkData(
        pausedWinNum = integer,
        isPausedWork = integer == 1,
        pausedMessage = string
    )
}