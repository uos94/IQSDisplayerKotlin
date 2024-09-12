package com.kct.iqsdisplayer.data.packet.send

import com.kct.iqsdisplayer.data.packet.BaseSendPacket
import com.kct.iqsdisplayer.network.ProtocolDefine

data class MediaListRequest(
    val code: Short = ProtocolDefine.MEDIA_LIST_REQUEST.value
) : BaseSendPacket(code) {
    override fun getDataArray(): Array<Any> {
        return emptyArray() // 빈 배열 반환
    }

    override fun toString(): String {
        return "MediaListRequest()"
    }
}


