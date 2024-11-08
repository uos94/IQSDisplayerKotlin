package com.kct.iqsdisplayer.data.packet.receive

data class LastCallData(
    /** 발권창구ID */
    var ticketWinId: Int = 0,
    /** 호출창구ID */
    var callWinId: Int = 0,
    /** 호출창구번호 */
    var callWinNum: Int = 0,
    /** 호출번호 */
    var callNum: Int = 0,
    /** 대기인수 */
    var waitNum: Int = 0
) {
    override fun toString(): String {
        return "ticketWinId[$ticketWinId], callWinId[$callWinId], callWinNum[$callWinNum], callNum[$callNum], waitNum[$waitNum]"
    }
}

/** packet에서 전달되는 형식은 다음과 같다.
 * 발권창구ID, 호출창구ID, 호출창구번호, 호출번호, 대기인수
 * 예) 9;9;3;2;0;#9;9;1;1;0;#
 */

fun String?.toLastCallList() : ArrayList<LastCallData> {
    val lastCallList = ArrayList<LastCallData>()
    if(this.isNullOrEmpty()) return lastCallList

    val splitData = this.split("#")

    for(data in splitData) {
        val split = data.split(";")
        val size = split.size

        if(size < 5) continue

        val item = LastCallData()

        item.ticketWinId   = split[0].toIntOrNull() ?: 0
        item.callWinId     = split[1].toIntOrNull() ?: 0
        item.callWinNum    = split[2].toIntOrNull() ?: 0
        item.callNum       = split[3].toIntOrNull() ?: 0
        item.waitNum       = split[4].toIntOrNull() ?: 0

        lastCallList.add(item)
    }

    return lastCallList
}