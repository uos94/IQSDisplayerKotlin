package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.CALL_COLLECT_SET 데이터 클래스
/* 미사용으로 삭제함
data class CallCollectSetData(
    val collectWinNum: Int,     // 창구 번호
    val collectNum: Int         // 호출 횟수
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.CALL_COLLECT_SET
) : BaseReceivePacket() {
    override fun toString(): String {
        return "CallCollectSetData(collectWinNum=$collectWinNum, collectNum=$collectNum)"
    }
}
fun Packet.toCallCollectSetData(): CallCollectSetData {
    return CallCollectSetData(
        collectWinNum = integer,
        collectNum = integer
    )
}*/