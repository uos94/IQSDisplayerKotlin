package com.kct.iqsdisplayer.data.packet.receive

data class LastCall(
    /** 발권창구ID */
    var ticketWinID: Int = 0,
    /** 호출창구ID */
    var callWinID: Int = 0,
    /** 호출창구번호 */
    var callWinNum: Int = 0,
    /** 호출번호 */
    var callNum: Int = 0,
    /** 대기인수 */
    var waitNum: Int = 0
) {
    override fun toString(): String {
        return "TicketWinID[$ticketWinID], CallWinID[$callWinID], CallWinNum[$callWinNum], CallNum[$callNum], WaitNum[$waitNum]"
    }
}

/** packet에서 전달되는 형식은 다음과 같다.
 * 발권창구ID, 호출창구ID, 호출창구번호, 호출번호, 대기인수
 * 예) 9;9;3;2;0;#9;9;1;1;0;#
 */

fun String?.toLastCallList() : ArrayList<LastCall> {
    val lastCallList = ArrayList<LastCall>()
    if(this.isNullOrEmpty()) return lastCallList

    val splitData = this.split("#")

    for(data in splitData) {
        val split = data.split(";")
        val size = split.size

        if(size < 5) continue

        val item = LastCall()

        item.ticketWinID   = split[0].toIntOrNull() ?: 0
        item.callWinID     = split[1].toIntOrNull() ?: 0
        item.callWinNum    = split[2].toIntOrNull() ?: 0
        item.callNum       = split[3].toIntOrNull() ?: 0
        item.waitNum       = split[4].toIntOrNull() ?: 0

        lastCallList.add(item)
    }

    return lastCallList
}