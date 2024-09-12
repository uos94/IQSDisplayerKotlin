package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

data class AcceptAuthResponse(
    /** 표시기의 창구 번호 */
    val winNum : Int = 0,//1
    /** 지점에서 사용중인 창구ID 리스트(구분자 “;”) */
    val winNumList : String = "",//1;2;3;
    /** 지점에서 사용중인 창구명 리스트 (구분자 “;”) */
    val winNameList : String = "",//입출금/제신고;일반업무;상담업무;
    /** 직원정보(구분자 “;”) */
    val tellerInfo : String = "",//5;1;;;10.131.54.65;0;1;;;30702819;1.1.1.3;구민가;1;입출금/제신고;1;;0;0;
    /** 미디어 표시 정보(구분자 “#”)  */
    val mediaInfo : String = "",//15000#1#5000#1#10000#woori_travel_15sec.mp4;woori2024.jpg;iqs_backup.jpg;#
    /** 1~10 까지의 볼륨값 */
    val volumeLevel : String = "",//10
    /** 창구별 대기인수 리스트 (구분자 “;”) */
    val waitingNumList : String = "",//0;0;0;
    /** 현재 시간, time_t 형식의 현재 시간값(고정값 0) */
    val serverTime : Int = 0,//0
    /** 화면설정정보, 대기인수 화면 표시 여부, 표시기 안내문구(구분자 “;”)
     *  화면설정정보  T : 대기인수 표시, F : 대기인수 미표시   0 : 검정 테마 1: 파랑 테마*/
    val displaySettingInfo : String = "",//1;우리은행에 방문해주셔서 감사합니다.;
    /** 동영상 삭제리스트, NULL문자(신한 미사용) */
    val deleteMovieInfo : String = "",//아무데이터도 안왔음 ""
    /** 호출 벨소리 파일명 */
    val bellFileName : String = "",//recv_mail.wav
    /** 호출 시 반복 출력 횟수 */
    val callRepeatCount : String = "", //1
    /** 호출안내멘트, 호출음 재생 시 사용 멘트 종류
     * 안내 멘트, 0: 창구로 오십시오, 1: 창구로 모시겠습니다, 2: 창구에서 도와드리겠습니다.*/
    val callMentNum : String = "",//0
    /** 부재정보(창구표시기용),전산장애표시(음성호출표시기용)
     * 부재여부(1:부재,0:사용중), 부재메시지(구분자 “;”), 전산장애여부(0: 정상 운영, 1: 전산장애)*/
    val pausedWork : String = "",//0;;
    /** 공석표시, 0: 정상화면 표시, 1: 공석화면 표시 */
    val notWork : String = ""//0
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.ACCEPT_AUTH_RESPONSE
) : BaseReceivePacket() {
    override fun toString(): String {
        return "AcceptAuthResponse(winNum=$winNum, winNumList='$winNumList', winNameList='$winNameList', tellerInfo='$tellerInfo', mediaInfo='$mediaInfo', volumeLevel='$volumeLevel', waitingNumList='$waitingNumList', serverTime=$serverTime, displaySettingInfo='$displaySettingInfo', deleteMovieInfo='$deleteMovieInfo', bellFileName='$bellFileName', callRepeatCount='$callRepeatCount', callMentNum='$callMentNum', pausedWork='$pausedWork', notWork='$notWork', protocolDefine=$protocolDefine)"
    }
}

/** 데이터가 많아 이 패킷만 특별히 다른데서 한번 더 가공하겠음 */
fun Packet.toAcceptAuthResponse(): AcceptAuthResponse {
    return AcceptAuthResponse(
        winNum = integer,
        winNumList = string,
        winNameList = string,
        tellerInfo = string,
        mediaInfo = string,
        volumeLevel = string,
        waitingNumList = string,
        serverTime = integer, //AS-IS그대로 가져옴..문서에 따르면 Long임.
        displaySettingInfo = string,
        deleteMovieInfo = string,
        bellFileName = string,
        callRepeatCount = string,
        callMentNum = string,
        pausedWork = string,
        notWork = string
    )
}