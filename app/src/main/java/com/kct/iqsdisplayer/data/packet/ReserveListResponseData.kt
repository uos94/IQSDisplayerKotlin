package com.kct.iqsdisplayer.data.packet

import com.kct.iqsdisplayer.network.Packet

// ProtocolDefine.RESERVE_LIST_RESPONSE 데이터 클래스
data class ReserveListResponseData(
    val mul: Int,
    val reserveListStr: String
) {
    override fun toString(): String {
        return "ReserveListResponseData(mul=$mul, reserveListStr='$reserveListStr')"
    }
}

fun Packet.toReserveListResponseData(): ReserveListResponseData {
    return ReserveListResponseData(
        mul = integer,
        reserveListStr = string
    )
}
