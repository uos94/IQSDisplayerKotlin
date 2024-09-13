package com.kct.iqsdisplayer.data

import com.kct.iqsdisplayer.common.Const.Arrow
import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

data class Call(
    /** 해당 표시기 장애 여부 BOOL (4) */
    val isError: Boolean = false,
    /** 호출번호(고객이 발권한 번호) */
    val callNum: Int = 0,
    /** 최초 발권 시 창구ID */
    val ticketWinId: Int = 0,
    /** 호출한 직원의 창구ID */
    val callWinId: Int = 0,
    /** 발권창구 대기인수 */
    val winWaitNum: Int = 0,
    /** 호출한 직원의 창구번호 */
    val callWinNum: Int = 0,
    /** 과거 호출번호 리스트(최근 응대 고객 안내용)
     * 반복 데이터 구분자 : ‘#’, 반복 데이터 내 구분자 : ‘;’
     * 순서대로
     * 발권창구ID, 호출창구ID, 호출창구번호, 호출번호, 대기인수
     * 예) 9;9;3;2;0;#9;9;1;1;0;#*/
    val lastCallList: ArrayList<LastCall> = ArrayList(),
    /** 백업 표시기 번호 */
    val bkDisplayNum: Int = 0,
    /** 백업 표시기 화살표 방향 */
    val bkWay: Arrow = Arrow.LEFT,
    /** 발권타입 (0:일반, 100:모바일 발권, 101:모바일 발권 취소, 200:상담예약 발권) */
    val ticketType: Int = 0,
    /** VIP실 여부 */
    val flagVip: Boolean = false
    , override var protocolDefine: ProtocolDefine? = null
) : BaseReceivePacket() {
    override fun toString(): String {
        return "Call(isError=$isError, callNum=$callNum, ticketWinID=$ticketWinId, callWinID=$callWinId, winWaitNum=$winWaitNum, callWinNum=$callWinNum, lastCallList=$lastCallList, bkDisplayNum=$bkDisplayNum, bkWay=$bkWay, ticketType=$ticketType, flagVip=$flagVip)"
    }
}

fun Packet.toCallRequest(): Call {
    return Call(
        isError = integer == 1,
        callNum = integer,
        ticketWinId = integer,
        callWinId = integer,
        winWaitNum = integer,
        callWinNum = integer,
        lastCallList = string.toLastCallList(),
        bkDisplayNum = integer,
        bkWay = integer.let { Arrow.entries.find { arrow -> arrow.value == it } } ?: Arrow.LEFT,
        ticketType = integer,
        flagVip = integer == 1,
        protocolDefine = ProtocolDefine.CALL_REQUEST
    )
}
/*
fun Packet.toCallCancelData(): Call {
    return Call(
        isError = integer == 1,
        callNum = integer,
        ticketWinID = integer,
        callWinID = integer,
        winWaitNum = integer,
        callWinNum = integer,
        lastCallList = string.toLastCallList(),
        bkDisplayNum = integer,
        bkWay = integer.let { Arrow.entries.find { arrow -> arrow.value == it } } ?: Arrow.LEFT,
        protocolDefine = ProtocolDefine.CALL_CANCEL
    )
}*/
