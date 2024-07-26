package com.kct.iqsdisplayer.data

// 231005, by HAHU  하나은행은 16개
data class Teller(
    var tellerID: Int,          // 직원 IDX
    var winID: Int,             // 소속 창구ID
    var job: String,            // 직무명
    var tellerImg: String,      // 직원 사진명
    var pcIP: String,           // 직원 PC IP
    var bkDisplay: Int,         // 백업 표시기 번호
    var bkWay: String,          // 백업 표시기 방향
    var proFile1: String,       // 프로필1 ( 자격증 | 자격등급)
    var proFile2: String,       // 프로필2 ( 자격증 | 자격등급)
    var tellerNum: Int,         // 직원 행번
    var displayIP: String,      // 표시기 IP
    var tellerName: String,     // 직원명
    var pjt: String,            // 공석 여부
    var winName: String,        // 창구명
    var winNum: Int,            // 창구 번호
    var emptyMsg: String,       // 부재 메세지
    var vip: String,            // VIP 구분
    var dispatch: String        // 파견 구분
)