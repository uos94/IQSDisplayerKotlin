package com.kct.iqsdisplayer.data.packet.send

import com.kct.iqsdisplayer.BuildConfig
import com.kct.iqsdisplayer.data.packet.BaseSendPacket
import com.kct.iqsdisplayer.network.ProtocolDefine

data class UpdateInfoRequest(
    val version: String = BuildConfig.VERSION_NAME,
    val code: Short = ProtocolDefine.UPDATE_INFO_REQUEST.value
) : BaseSendPacket(code) {
    override fun toString(): String {
        return "ReserveUpdateInfoRequestData()"
    }

    override fun getDataArray(): Array<Any> {
        return arrayOf(version)
    }
}
