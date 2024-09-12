package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

/* 미사용 코드로 삭제함.
data class PJTSetData(
    val pjtWinNum: Int,  // 창구 번호
    val pjt: Int         // 공석 여부 BOOL
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.PJT_SET
) : BaseReceivePacket() {
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
*/