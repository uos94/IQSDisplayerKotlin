package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.network.Packet

// ProtocolDefine.TELLER_RENEW_REQUEST 데이터 클래스
data class TellerRenewRequestData(
    val renewWinNum: Int,
    val tellerNum: Int,
    val tellerName: String
) {
    override fun toString(): String {
        return "TellerRenewRequestData(renewWinNum=$renewWinNum, tellerNum=$tellerNum, tellerName='$tellerName')"
    }
}
fun Packet.toTellerRenewRequestData(): TellerRenewRequestData {
    return TellerRenewRequestData(
        renewWinNum = integer,
        tellerNum = integer,
        tellerName = string
    )
}