package com.kct.iqsdisplayer.data

data class Reserve(
    var day: String,            // 예약일자
    var branchName: String,     // 예약 점번
    var reserveNum: String,     // 예약 번호
    var reserveTime: Int,       // 예약 시간
    var customerNum: String,    // 고객 번호
    var customerName: String,   // 고객 명
    var customerTel: String,    // 고객 연락처
    var customerGrade: String = "10",  // 고객 등급[1:premier, 2:ace, 3:best, 4:classic, 9:일반], 임의 고객등급이 없을경우 10 리턴
    var tellerNum: String,      // 상담 직원 번호
    var tellerName: String,     // 상담 직원 이름
    var tellerJob: String,      // 상담 업무
    var winID: Int,             // 창구 ID
    var winName: String,        // 창구명
    var arriveTime: String,     // 도착 시간
    var isArrive: String,       // 도착 등록 여부               // N Y
    var callTime: String,       // 호출 시간
    var isCancel: String        // 취소 여부                   // N Y
) {
    fun updateReserve(
        day: String, branchNum: String, reserveNum: String, reserveTime: Int, tellerNum: String, tellerName: String,
        tellerJob: String, customerNum: String, customerName: String, customerTel: String, customerGrade: String, winID: Int
    ) {
        this.day = day
        this.branchName = branchNum
        this.reserveNum = reserveNum
        this.reserveTime = reserveTime
        this.tellerNum = tellerNum
        this.tellerName = tellerName
        this.tellerJob = tellerJob
        this.customerNum = customerNum
        this.customerName = customerName
        this.customerTel = customerTel
        this.customerGrade = customerGrade
        this.winID = winID
    }

    fun setArriveTime(strArriveTime: String, strIsArrive: String) {
        arriveTime = strArriveTime
        isArrive = strIsArrive
    }

    fun cancelReserve() {
        this.isCancel = "Y"
    }
}
