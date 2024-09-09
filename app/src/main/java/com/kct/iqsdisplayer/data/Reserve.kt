package com.kct.iqsdisplayer.data

import com.kct.iqsdisplayer.network.Packet
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
    var channelType: Int = 0        // 18.채널 타입(하나원큐,콜센터,네이버)
) {
    override fun toString(): String {
        return "예약 일자 : $reserveDate, 지점 번호 : $branchNum, 예약 번호 : $reserveNum, 예약 시간 : $reserveTime, " +
                "직원 번호 : $tellerNum, 직원 명 : $tellerName, 업무 명 : $tellerJob, 고객 번호 : $customerNum, " +
                "고객 이름 : $customerName, 고객 연락처 : $customerTel, 고객 등급 : $customerGrade, " +
                "창구 ID : $reserveWinID, 창구명 : $reserveWinName, 도착 시간 : $arriveTime, 도착 등록 여부 : $isArrive, " +
                "호출 시간 : $callTime, 취소 여부 : $isCancel, 채널타입 : $channelType"
    }
}

private fun Array<String>.newReserve() : Reserve? {
    // 데이터 추출 및 변환
    if(this.isEmpty()) return null

    val size = this.size

    val result = Reserve()
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

fun Packet.toReserveAddRequestData(): Reserve? {
    val splitData = string.splitData("#")

    // 데이터 유효성 검사
    if (splitData.size <= 11) {
        Log.w("데이터오류 - ProtocolDefine.RESERVE_ADD_REQUEST[0x0029] : $string")
        return null
    }

    return splitData.newReserve()
}

fun Packet.toReserveUpdateRequestData(): Reserve? {
    val splitData = string.splitData("#")

    // 데이터 유효성 검사
    if (splitData.size < 11) {
        Log.w("데이터오류 - ProtocolDefine.RESERVE_UPDATE_REQUEST[0x002B] : $string")
        return null
    }

    return splitData.newReserve()
}

fun Packet.toReserveCancelRequestData(): Reserve? {
    val splitData = string.splitData("#")

    // 데이터 유효성 검사
    if (splitData.size < 11) {
        Log.w("데이터오류 - ProtocolDefine.RESERVE_CANCEL_REQUEST[0x002D] : $string")
        return null
    }

    return splitData.newReserve()
}


fun Packet.toReserveArriveRequestData(): Reserve? {
// 2019-12-12#0000#2019121200009008#14:30:00#CUST0123456789#김고객님#01012345566#3###개인대출상담#1#종합상담창구#14:18:32#Y#00:00:00#N
    val splitData = string.splitData("#")
    // 데이터 유효성 검사
    if (splitData.size < 17) {
        Log.w("데이터오류 - ProtocolDefine.RESERVE_ARRIVE_REQUEST[0x002F] : $string")
        return null
    }

    return splitData.newReserve()
}