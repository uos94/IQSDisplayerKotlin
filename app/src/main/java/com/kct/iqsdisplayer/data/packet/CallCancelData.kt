package com.kct.iqsdisplayer.data.packet

import com.kct.iqsdisplayer.network.Packet

// ProtocolDefine.CALL_CANCEL 데이터 클래스
data class CallCancelData(
    val cancelError: Int,       // 장애 여부
    val cancelCallNum: Int,     // 호출 번호
    val ticketWinID: Int,      // 발권 창구 ID
    val callWinID: Int,       // 호출 창구 ID
    val wait: Int,       // 대기 인수
    val callWinNum: Int,        // 호출 창구 번호
    val lastCallNumList: String, // 과거 호출 번호 리스트
    val bkNum: Int             // 백업 표시기 번호
) {
    override fun toString(): String {
        return "CallCancelData(cancelError=$cancelError, cancelCallNum=$cancelCallNum, ticketWinID=$ticketWinID, callWinID=$callWinID, wait=$wait, callWinNum=$callWinNum, lastCallNumList='$lastCallNumList', bkNum=$bkNum)"
    }
}

fun Packet.toCallCancelData(): CallCancelData {
    return CallCancelData(
        cancelError = integer,
        cancelCallNum = integer,
        ticketWinID = integer,
        callWinID = integer,
        wait = integer,
        callWinNum = integer,
        lastCallNumList = string,
        bkNum = integer
    )
}