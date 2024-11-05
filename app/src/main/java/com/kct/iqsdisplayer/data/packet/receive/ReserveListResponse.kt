package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.RESERVE_LIST_RESPONSE 데이터 클래스
data class ReserveListResponse(
    val mul: Int,
    val reserveList: ArrayList<Reserve>
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.RESERVE_LIST_RESPONSE
) : BaseReceivePacket() {
    override fun toString(): String {
        return "ReserveListResponse(reserveList='$reserveList')"
    }
}
