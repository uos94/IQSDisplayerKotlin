package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.util.splitData

data class MediaListResponse(
    val mediaList: ArrayList<String>
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.MEDIA_LIST_RESPONSE
) : BaseReceivePacket() {
    override fun toString(): String {
        return "MediaListResponse(mediaList=$mediaList)"
    }
}
fun Packet.toMediaListResponse(): MediaListResponse {
    val mediaList = ArrayList<String>()
    val splitData = string.splitData(";")
    for (data in splitData) {
        if(data.isNotEmpty() && data.isNotBlank()) mediaList.add(data)
    }
    return MediaListResponse(
        mediaList = mediaList
    )
}

