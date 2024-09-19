package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.CROWDED_REQUEST 데이터 클래스
data class CrowdedRequest(
    val isCrowded: Boolean,     // 혼잡 여부 (BOOL)
    val crowdedWinID: Int,  // 창구 ID
    val crowdedMsg: String  // 혼잡 메시지
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.CROWDED_REQUEST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "CrowdedRequest(isCrowded=$isCrowded, crowdedWinID=$crowdedWinID, crowdedMsg='$crowdedMsg')"
    }
}

fun Packet.toCrowdedRequest(): CrowdedRequest {
    return CrowdedRequest(
        isCrowded = integer == 1,
        crowdedWinID = integer,
        crowdedMsg = string
    )
}