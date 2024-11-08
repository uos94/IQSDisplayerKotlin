package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.common.Const.Arrow
import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.util.splitData

data class TellerData(
    /** 직원 IDX */
    var tellerId: Int = 0,          // 직원 IDX
    /** 소속 창구ID */
    var winId: Int = 0,             // 소속 창구ID
    /** 직무명 */
    var job: String = "",           // 직무명
    /** 직원 사진파일명 */
    var tellerImg: String = "",     // 직원 사진명
    /** 직원 PC IP */
    var pcIP: String = "",          // 직원 PC IP
    /** 백업 표시기 번호, 기본값 0 */
    var bkDisplay: Int = 0,         // 백업 표시기 번호
    /** 백업 표시기 방향 , 기본값 1, 좌(1),우(2),중앙(3)*/
    var bkWay: Arrow = Arrow.LEFT,  // 백업 표시기 방향
    /** 프로필1 ( 자격증 | 자격등급)  */
    var profile1: String = "",      // 프로필1 ( 자격증 | 자격등급)
    /** 프로필2 ( 자격증 | 자격등급) */
    var profile2: String = "",      // 프로필2 ( 자격증 | 자격등급)
    /** 직원 행번 */
    var tellerNum: Int = 0,         // 직원 행번
    /** 표시기 IP  */
    var displayIP: String = "",     // 표시기 IP
    /** 직원명 */
    var tellerName: String = "",    // 직원명
    /** 공석 여부 , 기본값 0, 0이면 사용중, 1이면 공석 */
    var isNotWork: Boolean = false, // 공석 여부
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
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.TELLER_LIST
) : BaseReceivePacket() {
    override fun toString(): String {
        return "\n직원 IDX:$tellerId,소속 창구 ID:$winId,직무명:$job,직원사진:$tellerImg,직원IP:$pcIP,백업표시기번호:$bkDisplay,백업화살표방향:${bkWay.value},프로필1:$profile1,프로필2:$profile2,행번:$tellerNum,표시기IP:$displayIP,직원명:$tellerName,공석여부:$isNotWork,창구명:$winName,창구 번호:$winNum,부재 메세지:$emptyMsg,VIP 구분:$vip,파견 구분:$dispatch"
        /*return """
        직원 IDX      : $tellerId
        소속 창구 ID   : $winId
        직무명         : $job
        직원 사진      : $tellerImg
        직원 PC IP    : $pcIP
        백업 표시기 번호: $bkDisplay
        백업 화살표 방향: ${bkWay.value}
        프로필1        : $profile1
        프로필2        : $profile2
        직원 행번      : $tellerNum
        표시기 IP      : $displayIP
        직원명         : $tellerName
        공석 여부      : $isNotWork
        창구명         : $winName
        창구 번호      : $winNum
        부재 메세지     : $emptyMsg
        VIP 구분       : $vip
        파견 구분       : $dispatch
    """.trimIndent()*/
    }
}
fun String.toTeller(): TellerData {
    val splitTeller = this.splitData(";")
    val size = splitTeller.size
    val tellerData = TellerData()
    if(size > 0) tellerData.tellerId    = splitTeller[0].toIntOrNull() ?: 0
    if(size > 1) tellerData.winId       = splitTeller[1].toIntOrNull() ?: 0
    if(size > 2) tellerData.job         = splitTeller[2]
    if(size > 3) tellerData.tellerImg   = splitTeller[3]
    if(size > 4) tellerData.pcIP        = splitTeller[4]
    if(size > 5) tellerData.bkDisplay   = splitTeller[5].toIntOrNull() ?: 0
    if(size > 6) tellerData.bkWay       = splitTeller[6].toIntOrNull()?.let { Arrow.entries.find { arrow -> arrow.value == it } } ?: Arrow.LEFT
    if(size > 7) tellerData.profile1    = splitTeller[7]
    if(size > 8) tellerData.profile2    = splitTeller[8]
    if(size > 9) tellerData.tellerNum   = splitTeller[9].toIntOrNull() ?: 0
    if(size > 10) tellerData.displayIP  = splitTeller[10]
    if(size > 11) tellerData.tellerName = splitTeller[11]
    if(size > 12) tellerData.isNotWork  = when(splitTeller[12]) {"공석", "공석(PJT)", "1" -> true  else -> false }
    if(size > 13) tellerData.winName    = splitTeller[13]
    if(size > 14) tellerData.winNum     = splitTeller[14].toIntOrNull() ?: 0
    if(size > 15) tellerData.emptyMsg   = splitTeller[15]
    if(size > 16) tellerData.vip        = splitTeller[16]
    if(size > 17) tellerData.dispatch   = splitTeller[17]
    return tellerData
}

fun Packet.toTellerList(): TellerListData {
    val data = string.replace("DATA#직원#", "")
    val splitTellerInfo = data.splitData("&")
    val tellerList = ArrayList<TellerData>()
    for (tellerInfo in splitTellerInfo) {
        val teller = tellerInfo.toTeller()
        tellerList.add(teller)
    }

    return TellerListData(
        tellerList = tellerList,
        protocolDefine = ProtocolDefine.TELLER_LIST
    )
}