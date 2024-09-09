package com.kct.iqsdisplayer.data

import com.kct.iqsdisplayer.common.Const.PJT
import com.kct.iqsdisplayer.common.Const.Arrow
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.splitData

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
    var bkDisplay: Int = 0,         // 백업 표시기 번호  //TODO : 나중에 변수명 수정해야함. backupDisplayNum
    /** 백업 표시기 방향 , 기본값 1, 좌(1),우(2),중앙(3)*/
    var bkWay: Arrow = Arrow.LEFT,  // 백업 표시기 방향  //TODO : 나중에 변수명 수정해야함. backupArrow, Arrow define할것
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
    /** 공석 여부 , 기본값 0, 0이면 사용중, 1이면 공석 */
    var pjt: PJT = PJT.IN_DESK, // 공석 여부
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
) {
    override fun toString(): String {
        return """
        직원 IDX      : $tellerID
        소속 창구 ID   : $winID
        직무명         : $job
        직원 사진      : $tellerImg
        직원 PC IP    : $pcIP
        백업 표시기 번호: $bkDisplay
        백업 화살표 방향: ${bkWay.value}
        프로필1        : $proFile1
        프로필2        : $proFile2
        직원 행번      : $tellerNum
        표시기 IP      : $displayIP
        직원명         : $tellerName
        공석 여부      : ${pjt.value}
        창구명         : $winName
        창구 번호      : $winNum
        부재 메세지     : $emptyMsg
        VIP 구분       : $vip
        파견 구분       : $dispatch
    """.trimIndent()
    }
}

fun Packet.toTellerData(): ArrayList<Teller> {
    val data = string.replace("DATA#직원#", "")
    val splitTellerInfo = data.splitData("&")
    val tellerList = ArrayList<Teller>()
    for (tellerInfo in splitTellerInfo) {
        val splitTeller = tellerInfo.splitData(";")
        if(splitTeller.size < 18) {
            Log.d("Data length < 18")
            continue
        }
        val teller = Teller(
            tellerID =  splitTeller[0].toIntOrNull() ?: 0,
            winID =     splitTeller[1].toIntOrNull() ?: 0,
            job =       splitTeller[2],
            tellerImg = splitTeller[3],
            pcIP =      splitTeller[4],
            bkDisplay = splitTeller[5].toIntOrNull() ?: 0,
            bkWay =     splitTeller[6].toIntOrNull()?.let { Arrow.entries.find { arrow -> arrow.value == it } } ?: Arrow.LEFT,
            proFile1 =  splitTeller[7],
            proFile2 =  splitTeller[8],
            tellerNum = splitTeller[9].toIntOrNull() ?: 0,
            displayIP = splitTeller[10],
            tellerName = splitTeller[11],
            pjt =       when(splitTeller[12]) {"공석", "공석(PJT)" -> PJT.EMPTY  else -> PJT.IN_DESK },
            winName =   splitTeller[13],
            winNum =    splitTeller[14].toIntOrNull() ?: 0,
            emptyMsg =  splitTeller[15],
            vip =       splitTeller[16],
            dispatch =  splitTeller[17]
        )
        tellerList.add(teller)
    }

    return tellerList
}