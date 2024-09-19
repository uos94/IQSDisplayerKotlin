package com.kct.iqsdisplayer.data

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.data.packet.receive.ReserveListResponse
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.splitData

data class Reserve(
    /** 예약 일자 YYYY-MM-DD */
    var reserveDate: String = "",   // 1.예약일자
    var branchNum: Int = 0,         // 2.예약 점번
    var reserveNum: Int = 0,        // 3.예약 번호
    /** 예약 시간 HH:MM:SS */
    var reserveTime: String = "",   // 4.예약 시간
    var customerNum: Int = 0,       // 5.고객 번호
    var customerName: String = "",  // 6.고객 명
    var customerTel: String = "",   // 7.고객 연락처
    var customerGrade: Int = 10,    // 8.고객 등급[1:premier, 2:ace, 3:best, 4:classic, 9:일반], 임의 고객등급이 없을경우 10 리턴
    var tellerNum: Int = 0,         // 9.상담 직원 번호
    var tellerName: String = "",    // 10.상담 직원 이름
    var tellerJob: String = "",     // 11.상담 업무
    var reserveWinID: Int = 0,      // 12.창구 ID
    var reserveWinName: String = "",// 13.창구명
    /** 도착 등록 시간 HH:MM:SS */
    var arriveTime: String = "",    // 14.도착 등록 시간
    var isArrive: Boolean = false,  // 15.도착 여부   // N Y
    /** 호출 시간 HH:MM:SS */
    var callTime: String = "",      // 16.호출 시간
    var isCancel: Boolean = false,  // 17.취소 여부   // N Y
    var channelType: Int = 0,       // 18.채널 타입(하나원큐,콜센터,네이버)
    override var protocolDefine: ProtocolDefine? = null
) : BaseReceivePacket() {

    override fun toString(): String {
        return "예약 일자 : $reserveDate, 지점 번호 : $branchNum, 예약 번호 : $reserveNum, 예약 시간 : $reserveTime, " +
                "직원 번호 : $tellerNum, 직원 명 : $tellerName, 업무 명 : $tellerJob, 고객 번호 : $customerNum, " +
                "고객 이름 : $customerName, 고객 연락처 : $customerTel, 고객 등급 : $customerGrade, " +
                "창구 ID : $reserveWinID, 창구명 : $reserveWinName, 도착 시간 : $arriveTime, 도착 등록 여부 : $isArrive, " +
                "호출 시간 : $callTime, 취소 여부 : $isCancel, 채널타입 : $channelType"
    }
}

private fun Array<String>.newReserve(protocol: ProtocolDefine) : Reserve {
    // 데이터 추출 및 변환
    val size = this.size

    val result = Reserve().apply { protocolDefine = protocol }
    if(size > 0) result.reserveDate     = this[0]
    if(size > 1) result.branchNum       = this[1].toIntOrNull() ?: 0
    if(size > 2) result.reserveNum      = this[2].toIntOrNull() ?: 0
    if(size > 3) result.reserveTime     = this[3]
    if(size > 4) result.tellerNum       = this[4].toIntOrNull() ?: 0
    if(size > 5) result.tellerName      = this[5]
    if(size > 6) result.tellerJob       = this[6]
    if(size > 7) result.customerNum     = this[7].toIntOrNull() ?: 0
    if(size > 8) result.customerName    = this[8]
    if(size > 9) result.customerTel     = this[9]
    if(size > 10) result.customerGrade  = this[10].toIntOrNull() ?: 10
    if(size > 11) result.reserveWinID   = this[11].toIntOrNull() ?: 0
    if(size > 12) result.reserveWinName = this[12]
    if(size > 13) result.arriveTime     = this[13]
    if(size > 14) result.isArrive       = this[14] == "Y"
    if(size > 15) result.callTime       = this[15]
    if(size > 16) result.isCancel       = this[16] == "Y"
    if(size > 17) result.channelType    = this[17].toIntOrNull() ?: 0
    return result
}

fun Packet.toReserveAddRequest(): Reserve {
    val splitData = string.splitData("#")

    return splitData.newReserve(ProtocolDefine.RESERVE_ADD_REQUEST)
}

fun Packet.toReserveUpdateRequest(): Reserve {
    //TODO : 패킷정의서에는 없는 데이터 인데. AS-IS보면 실제로 뭔가 넘어온다. 변수명도 mul 그대로 가져옴.
    val mul = integer
    val splitData = string.splitData("#")

    return splitData.newReserve(ProtocolDefine.RESERVE_UPDATE_REQUEST)
}

fun Packet.toReserveCancelRequest(): Reserve {
    val splitData = string.splitData("#")

    return splitData.newReserve(ProtocolDefine.RESERVE_CANCEL_REQUEST)
}


fun Packet.toReserveArriveRequest(): Reserve {
// 2019-12-12#0000#2019121200009008#14:30:00#CUST0123456789#김고객님#01012345566#3###개인대출상담#1#종합상담창구#14:18:32#Y#00:00:00#N
    val splitData = string.splitData("#")

    return splitData.newReserve(ProtocolDefine.RESERVE_ARRIVE_REQUEST)
}

fun Packet.toReserveListResponse(): ReserveListResponse {
    val mul = integer //항상 0인데 뭐하는 용도인지는 모르겠음.
    val resultList = ArrayList<Reserve>()
    val reserveSplitData = string.splitData("&")
    for (reserveData in reserveSplitData) {
        val splitData = reserveData.splitData("#")
        val reserve = splitData.newReserve(ProtocolDefine.RESERVE_LIST_RESPONSE) //ProtocolDefine 안해도 상관 없음.
        reserve.let { resultList.add(it) }
    }

    return ReserveListResponse(
        mul = mul,
        reserveList = resultList,
        protocolDefine = ProtocolDefine.RESERVE_LIST_RESPONSE
    )
}