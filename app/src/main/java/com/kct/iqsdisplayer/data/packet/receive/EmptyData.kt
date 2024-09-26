package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.ProtocolDefine

data class EmptyData(
    override var protocolDefine: ProtocolDefine? = null
) : BaseReceivePacket() {
    override fun toString(): String {
        return "...No Data"
    }
}