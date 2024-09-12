package com.kct.iqsdisplayer.data.packet

import com.kct.iqsdisplayer.network.ProtocolDefine

abstract class BaseReceivePacket {
    abstract var protocolDefine: ProtocolDefine?
}