package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.network.Packet

// ProtocolDefine.PJT_SET 데이터 클래스
data class PJTSetData(
    val pjtWinNum: Int,  // 창구 번호
    val pjt: Int         // 공석 여부 BOOL
) {
    override fun toString(): String {
        return "PJTSetData(pjtWinNum=$pjtWinNum, pjt=$pjt)"
    }
}
fun Packet.toPJTSetData(): PJTSetData {
    return PJTSetData(
        pjtWinNum = integer,
        pjt = integer
    )
}
