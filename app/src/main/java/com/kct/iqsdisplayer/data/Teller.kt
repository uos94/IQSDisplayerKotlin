package com.kct.iqsdisplayer.data

// 231005, by HAHU  하나은행은 16개
data class Teller(
    /** 직원 IDX */
    var tellerID: Int = 0,          // 직원 IDX
    /** 소속 창구ID */
    var winID: Int = 0,             // 소속 창구ID
    /** 직무명 */
    var job: String = "",           // 직무명
    /** 직원 사진파일명 */
    var tellerImg: String = "",     // 직원 사진명
    /** 직원 PC IP */
    var pcIP: String = "",          // 직원 PC IP
    /** 백업 표시기 번호, 기본값 0 */
    var bkDisplay: Int = 0,         // 백업 표시기 번호
    /** 백업 표시기 방향 , 기본값 1*/
    var bkWay: Int = 1,             // 백업 표시기 방향
    /** 프로필1 ( 자격증 | 자격등급)  */
    var proFile1: String = "",      // 프로필1 ( 자격증 | 자격등급)
    /** 프로필2 ( 자격증 | 자격등급) */
    var proFile2: String = "",      // 프로필2 ( 자격증 | 자격등급)
    /** 직원 행번 */
    var tellerNum: Int = 0,         // 직원 행번
    /** 표시기 IP  */
    var displayIP: String = "",     // 표시기 IP
    /** 직원명 */
    var tellerName: String = "",    // 직원명
    /** 공석 여부 , 기본값 1, 0이면 공석X, 1이면 공석 */
    var pjt: Int = 1,               // 공석 여부
    /** 창구명 */
    var winName: String = "",       // 창구명
    /** 창구 번호 , 기본값 0 */
    var winNum: Int = 0,            // 창구 번호
    /** 부재 메세지 */
    var emptyMsg: String = "",      // 부재 메세지
    /** VIP 구분  */
    var vip: String = "",           // VIP 구분
    /** 파견 구분 */
    var dispatch: String = ""       // 파견 구분
)