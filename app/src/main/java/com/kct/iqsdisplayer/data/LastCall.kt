package com.kct.iqsdisplayer.data

data class LastCall(
    val ticketWinID: Int,
    val callWinID: Int,
    val callWinNum: Int,
    val callNum: Int,
    val waitNum: Int
) {
    override fun toString(): String {
        return "LastCall(ticketWinID=$ticketWinID, callWinID=$callWinID, callWinNum=$callWinNum, callNum=$callNum, waitNum=$waitNum)"
    }
}
