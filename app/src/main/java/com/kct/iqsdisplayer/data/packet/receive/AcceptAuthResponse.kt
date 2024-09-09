package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.network.Packet

data class AcceptAuthResponseData(
    val winNum : Int,
    val winIdList : String,
    val winNameList : String,
    val tellerInfo : String,
    val mediaInfo : String,
    val volumeInfo : String,
    val waitList : String,
    val time : Int,
    val settingInfo : String,
    val movieInfo : String,
    val bellInfo : String,
    val callInfo : String,
    val reserve1 : String,// 안내 멘트
    val reserve2 : String,// 전산 장애 표시
    val reserve3 : String // 공석 표시
) {
    override fun toString(): String {
        return "AcceptAuthResponseData(winNum=$winNum, winIdList='$winIdList', winNameList='$winNameList', tellerInfo='$tellerInfo', mediaInfo='$mediaInfo', volumeInfo='$volumeInfo', waitList='$waitList', time=$time, settingInfo='$settingInfo', movieInfo='$movieInfo', bellInfo='$bellInfo', callInfo='$callInfo', info ment='$reserve1', Error='$reserve2', Empty='$reserve3')"
    }
}

fun Packet.toAcceptAuthResponseData(): AcceptAuthResponseData {
    return AcceptAuthResponseData(
        winNum = integer,
        winIdList = string,
        winNameList = string,
        tellerInfo = string,
        mediaInfo = string,
        volumeInfo = string,
        waitList = string,
        time = integer,
        settingInfo = string,
        movieInfo = string,
        bellInfo = string,
        callInfo = string,
        reserve1 = string,
        reserve2 = string,
        reserve3 = string
    )
}