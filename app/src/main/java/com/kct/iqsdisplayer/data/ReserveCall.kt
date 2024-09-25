package com.kct.iqsdisplayer.data

import com.kct.iqsdisplayer.common.Const.Arrow
import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.util.removeChar
import com.kct.iqsdisplayer.util.splitData

data class ReserveCall(
    var reserveNum: Int = 0,            // 1.예약 번호
    var reserveTime: Int = 0,           // 2.예약 시간
    var customerNum: String = "",       // 3.고객 ID, AI-IS코드 확인 결과 고객번호와 같음(0으로 시작하는 고객도 있어 Int로 하면 안됨)
    var customerName: String = "",      // 4.고객명
    val isError: Boolean = false,       // 5.장애여부
    var reserveCallNum: Int = 0,        // 6.호출번호
    var reserveCallWinID: Int = 0,      // 7.호출창구ID
    var reserveCallWinNum: Int = 0,     // 8.호출창구번호
    var reserveBkDisplayNum: Int = 0,   // 9.백업창구번호
    var reserveBkWay: Arrow = Arrow.LEFT,// 10.백업방향
    val flagVip: Boolean = false
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.RESERVE_CALL_REQUEST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "ReserveCall(reserveNum=$reserveNum, reserveTime='$reserveTime', customerNum='$customerNum', customerName='$customerName', isError=$isError, reserveCallNum=$reserveCallNum, reserveCallWinID=$reserveCallWinID, reserveCallWinNum=$reserveCallWinNum, reserveBkDisplayNum=$reserveBkDisplayNum, reserveBkWay=$reserveBkWay, flagVip=$flagVip, protocolDefine=$protocolDefine)"
    }
}

fun Packet.toReserveCallRequest(): ReserveCall {
    val splitData = string.splitData("#")
    return ReserveCall(
        reserveNum          = splitData[0].toIntOrNull() ?: 0,
        reserveTime         = splitData[1].removeChar(":").toIntOrNull() ?: 0,
        customerNum         = splitData[2],
        customerName        = splitData[3],
        isError             = splitData[4] == "1",
        reserveCallNum      = splitData[5].toIntOrNull() ?: 0,
        reserveCallWinID    = splitData[6].toIntOrNull() ?: 0,
        reserveCallWinNum   = splitData[7].toIntOrNull() ?: 0,
        reserveBkDisplayNum = splitData[8].toIntOrNull() ?: 0,
        reserveBkWay        = splitData[9].toIntOrNull()?.let { Arrow.entries.find { arrow -> arrow.value == it } } ?: Arrow.LEFT,
        flagVip             = splitData[10] == "1",
        protocolDefine      = ProtocolDefine.RESERVE_CALL_REQUEST
    )
}

