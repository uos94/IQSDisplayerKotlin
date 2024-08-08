package com.kct.iqsdisplayer.data.packet

import com.kct.iqsdisplayer.network.Packet

data class CallRequestData(
    val errorStatus: Int,
    val callNum: Int,
    val ticketWinID: Int,
    val callWinID: Int,
    val winWaitNum: Int,
    val callWinNum: Int,
    val lastCallNum: String,
    val bkDisplay: Int,
    val bkWay: Int,
    val reserve: Int,
    val vipFlag: Int
) {
    override fun toString(): String {
        return "CallRequestData(errorStatus=$errorStatus, callNum=$callNum, ticketWinID=$ticketWinID, callWinID=$callWinID, winWaitNum=$winWaitNum, callWinNum=$callWinNum, lastCallNum='$lastCallNum', bkDisplay=$bkDisplay, bkWay=$bkWay, reserve=$reserve, vipFlag=$vipFlag)"
    }
}

// Packet 클래스 확장 함수
fun Packet.toCallRequestData(): CallRequestData {
    return CallRequestData(
        errorStatus = integer,
        callNum = integer,
        ticketWinID = integer,
        callWinID = integer,
        winWaitNum = integer,
        callWinNum = integer,
        lastCallNum = string,
        bkDisplay = integer,
        bkWay = integer,
        reserve = integer,
        vipFlag = integer
    )
}