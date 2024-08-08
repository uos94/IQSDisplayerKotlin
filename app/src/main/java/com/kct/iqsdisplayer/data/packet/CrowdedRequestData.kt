package com.kct.iqsdisplayer.data.packet

import com.kct.iqsdisplayer.network.Packet

// ProtocolDefine.CROWDED_REQUEST 데이터 클래스
data class CrowdedRequestData(
    val isCrowded: Int,     // 혼잡 여부 (BOOL)
    val crowdedWinID: Int,  // 창구 ID
    val crowdedMsg: String  // 혼잡 메시지
) {
    override fun toString(): String {
        return "CrowdedRequestData(isCrowded=$isCrowded, crowdedWinID=$crowdedWinID, crowdedMsg='$crowdedMsg')"
    }
}

fun Packet.toCrowdedRequestData(): CrowdedRequestData {
    return CrowdedRequestData(
        isCrowded = integer,
        crowdedWinID = integer,
        crowdedMsg = string
    )
}