package com.kct.iqsdisplayer.data

import com.kct.iqsdisplayer.common.Const.Arrow
import com.kct.iqsdisplayer.network.Packet

data class Call(
    /** 해당 표시기 장애 여부 BOOL (4) */
    val isError: Boolean,
    /** 호출번호(고객이 발권한 번호) */
    val callNum: Int,
    /** 최초 발권 시 창구ID */
    val ticketWinID: Int,
    /** 호출한 직원의 창구ID */
    val callWinID: Int,
    /** 발권창구 대기인수 */
    val winWaitNum: Int,
    /** 호출한 직원의 창구번호 */
    val callWinNum: Int,
    /** 과거 호출번호 리스트(최근 응대 고객 안내용)
     * 반복 데이터 구분자 : ‘#’, 반복 데이터 내 구분자 : ‘;’
     * 순서대로
     * 발권창구ID, 호출창구ID, 호출창구번호, 호출번호, 대기인수
     * 예) 9;9;3;2;0;#9;9;1;1;0;#*/
    val lastCallList: ArrayList<LastCall>,
    /** 백업 표시기 번호 */
    val bkDisplayNum: Int = 0,
    /** 백업 표시기 화살표 방향 */
    val bkWay: Arrow = Arrow.LEFT,
    /** 발권타입 (0:일반, 100:모바일 발권, 101:모바일 발권 취소, 200:상담예약 발권) */
    val ticketType: Int = 0,
    /** VIP실 여부 */
    val flagVip: Int = 0  //아직 value가 어떻게 오는지 몰라 대기중
) {
    override fun toString(): String {
        return "Call(isError=$isError, callNum=$callNum, ticketWinID=$ticketWinID, callWinID=$callWinID, winWaitNum=$winWaitNum, callWinNum=$callWinNum, lastCallList=$lastCallList, bkDisplayNum=$bkDisplayNum, bkWay=$bkWay, ticketType=$ticketType, flagVip=$flagVip)"
    }
}

// Packet 클래스 확장 함수
fun Packet.toCallRequestData(): Call {
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
        ticketType = integer,
        flagVip = integer
    )
}

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
        bkWay = integer.let { Arrow.entries.find { arrow -> arrow.value == it } } ?: Arrow.LEFT
    )
}