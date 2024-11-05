package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.RESERVE_LIST_RESPONSE 데이터 클래스
data class TellerListData(
    val tellerList: ArrayList<TellerData>
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.TELLER_LIST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "TellerListResponse(reserveList='$tellerList')"
    }
}
