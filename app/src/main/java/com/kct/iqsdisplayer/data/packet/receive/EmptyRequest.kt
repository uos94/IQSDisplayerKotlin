package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

// ProtocolDefine.EMPTY_REQUEST 데이터 클래스
data class EmptyRequest(
    val winNum: Int,// 부재 설정 창구 직원 창구 번호
    val emptyFlag: Int, // 부재 설정 여부
    val emptyMsg: String// 부재 설정 메시지
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.EMPTY_REQUEST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "EmptyRequest(winNum=$winNum, emptyFlag=$emptyFlag, emptyMsg='$emptyMsg')"
    }
}
fun Packet.toEmptyRequest(): EmptyRequest {
    return EmptyRequest(
        winNum = integer,
        emptyFlag = integer,
        emptyMsg = string
    )
}