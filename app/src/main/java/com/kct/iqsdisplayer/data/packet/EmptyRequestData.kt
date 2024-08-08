package com.kct.iqsdisplayer.data.packet

import com.kct.iqsdisplayer.network.Packet

// ProtocolDefine.EMPTY_REQUEST 데이터 클래스
data class EmptyRequestData(
    val winNum: Int,// 부재 설정 창구 직원 창구 번호
    val emptyFlag: Int, // 부재 설정 여부
    val emptyMsg: String// 부재 설정 메시지
) {
    override fun toString(): String {
        return "EmptyRequestData(winNum=$winNum, emptyFlag=$emptyFlag, emptyMsg='$emptyMsg')"
    }
}
fun Packet.toEmptyRequestData(): EmptyRequestData {
    return EmptyRequestData(
        winNum = integer,
        emptyFlag = integer,
        emptyMsg = string
    )
}