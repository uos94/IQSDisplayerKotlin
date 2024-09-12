package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.Teller
import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.RESERVE_LIST_RESPONSE 데이터 클래스
data class TellerListResponse(
    //val mul: Int, 기존 코드에서는 packet.getInteger로 먼저 받고 있는데 문서에는 그런게 없고 reserveListStr하나만 있다...일단 삭제함.
    val tellerList: ArrayList<Teller>
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.TELLER_LIST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "TellerListResponse(reserveList='$tellerList')"
    }
}
