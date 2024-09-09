package com.kct.iqsdisplayer.data.packet.send

import com.kct.iqsdisplayer.data.packet.BaseSendPacket
import com.kct.iqsdisplayer.network.ProtocolDefine

data class ReserveListRequest(
    val code: Short = ProtocolDefine.RESERVE_LIST_REQUEST.value
) : BaseSendPacket(code) {
    override fun toString(): String {
        return "ReserveListRequestData()"
    }

    override fun getDataArray(): Array<Any> {
        return emptyArray()
    }
}


