package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.TELLER_RENEW_REQUEST 데이터 클래스
data class TellerRenewRequest(
    val renewWinNum: Int,
    val tellerNum: Int,
    val tellerName: String
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.TELLER_RENEW_REQUEST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "TellerRenewRequest(renewWinNum=$renewWinNum, tellerNum=$tellerNum, tellerName='$tellerName')"
    }
}
fun Packet.toTellerRenewRequest(): TellerRenewRequest {
    return TellerRenewRequest(
        renewWinNum = integer,
        tellerNum = integer,
        tellerName = string
    )
}