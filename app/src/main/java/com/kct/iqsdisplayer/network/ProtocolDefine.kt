package com.kct.iqsdisplayer.network


/**
 * IQS 간 TCP 통신 프로토콜 ID 정의 클래스
 * 모두 Short로 2Byte를 가짐
 */
enum class ProtocolDefine(val value: Short) {
    // 접속 관련 프로토콜 (Connection)
    /** 0xF0F0, 지능형 순번발행기 접속 시 표시기로 전송되는 패킷. 데이터 없음 (IQS -> 표시기)*/
    CONNECT_SUCCESS(0xF0F0.toShort()),
    /** 0xF0F1, 접속거부 응답	등록되지 않은 IP로 접속 또는 승인 요청 시 응답 패킷. 데이터 없음 (IQS -> 표시기) */
    CONNECT_REJECT(0xF0F1.toShort()),
    /** 0x1001, 표시기 접속 승인 요청 패킷. (표시기 -> IQS) */
    ACCEPT_AUTH_REQUEST(0x1001.toShort()),
    /** 0x1002, 접속승인 요청 패킷에 대한 응답 패킷. (IQS -> 표시기) */
    ACCEPT_AUTH_RESPONSE(0x1002.toShort()),

    // 시작 정보 관련 프로토콜 (Start Information) (미사용)
    /** 0x1001, 시작 정보 요청 패킷  */
    //START_REQUEST(0x0001.toShort()), (미사용)
    /** 0x1002, 시작 정보 응답 패킷  */
    //START_RESPONSE(0x0002.toShort()), (미사용)

    // 대기 정보 관련 프로토콜 (Waiting Information)
    /** 0x0003, 대기인수 정보 요청 패킷. (표시기 -> IQS) */
    WAIT_REQUEST(0x0003.toShort()),
    /** 0x0004, 대기인수 정보 응답 패킷. (IQS -> 표시기) */
    WAIT_RESPONSE(0x0004.toShort()),

    // 호출 관련 프로토콜 (Call)
    /** 0x0005, 호출 시 전송되는 패킷. (IQS -> 표시기) */
    CALL_REQUEST(0x0005.toShort()),
    /** 0x0006, 호출 시 전송되는 패킷. (표시기 -> IQS) (미사용) */
    //CALL_RESPONSE(0x0006.toShort()),
    /** 0x0007, 재 호출 시 전송되는 패킷. (IQS -> 표시기) */
    RE_CALL_REQUEST(0x0007.toShort()),
    /** 0x0008, 재 호출 시 전송되는 패킷. (표시기 -> IQS) (미사용) */
    //RE_CALL_RESPONSE(0x0008.toShort()),

    /** 0x0009, 부재정보 요청, 직원 부재 설정 시 전송되는 패킷 (IQS -> 표시기) */
    PAUSED_WORK_REQUEST(0x0009.toShort()),
    //PAUSED_WORK_RESPONSE(0x000A.toShort()),

    /** 0x000B, 안내메시지 설정 요청, 직원이 안내 메시지 설정시 순번발행기로부터 전송되는 패킷. (IQS -> 표시기) */
    INFO_MESSAGE_REQUEST(0x000B.toShort()),
    /** 0x000C, 직원이 안내 메시지 응답 패킷. (IQS -> 표시기) (미사용)*/
    //INFO_MESSAGE_RESPONSE(0x000C.toShort()),

    /** 0x000D, 직원정보 설정, 관제서버에서 직원 설정시 순번발행기로부터 전송되는 패킷 (IQS -> 표시기) */
    TELLER_LIST(0x000D.toShort()),
    /** 0x000E, 상세직원정보 (미사용) */
    //INFO_TELLER(0x000E.toShort()),

    // 시스템 관련 프로토콜 (System)
    /** 0x000F, 순번발행기 종료시 전송되는 패킷 (IQS -> 표시기) */
    SYSTEM_OFF(0x000F.toShort()),
    /** 0x0010, 동영상 설정, (관제서버)동영상 설정 변경 되었을 때 순번발행기로부터 수신되는 패킷 (미사용)*/
    //VIDEO_SET(0x0010.toShort()),

    // 기타 설정 관련 프로토콜 (Other Settings)
    /** 0x0011, 볼륨테스트, 관리자에서 창구표시기 호출볼륨 테스트시 순번발행기로부터 전송되는 패킷 (미사용)*/
    //VOLUME_TEST(0x0011.toShort()),
    /** 0x0012, 음성설정, 관리자에서 음성설정시 순번발행기로부터 전송되는 패킷, (미사용) */
    //SOUND_SET(0x0012.toShort()),
    /** 0x0013, 재시작 요청, 표시기 재시작이 필요한 경우 순번발행기에서 전송하는 패킷 */
    RESTART_REQUEST(0x0013.toShort()),
    /** 0x0014, 재시작 응답 (미사용)*/
    RESTART_RESPONSE(0x0014.toShort()),
    /** 0x0015, 혼잡메세지, 창구 대기인수가 혼잡상태 및 해제 상태 발생시 순번발행기에서 전송하는 패킷*/
    CROWDED_REQUEST(0x0015.toShort()),
    /** 0x0016, 혼잡 응답 (미사용)*/
    //CROWDED_RESPONSE(0x0016.toShort()),
    /** 0x0017, 창구정보 요청 (미사용)*/
    WIN_REQUEST(0x0017.toShort()),
    /** 0x0018, 창구정보 응답, 순번발행기에서 창구정보 변경 시 전송하는 패킷 */
    WIN_RESPONSE(0x0018.toShort()),
    /** 0x0019, KEEPALIVE 요청, 표시기에서 순번발행기로 실행중임을 주기적으로 알리는 패킷(표시기 -> IQS) */
    KEEP_ALIVE_REQUEST(0x0019.toShort()),
    /** 0x001A, KEEPALIVE 응답, 순번발행기에서 보내는 응답 패킷 (IQS -> 표시기) */
    KEEP_ALIVE_RESPONSE(0x001A.toShort()),
    /** 0x001B, 설치정보, 표시기에서 순번발행기로 실행중임을 주기적으로 알리는 패킷 (미사용) */
    //INSTALL_INFO(0x001B.toShort()),
    /** 0x001C, 표시기 정보, 순번발행기로부터 표시기 색상 및 대기인수표시 정보를 수신하는 패킷 (미사용)*/
    //DISPLAY_INFO(0x001C.toShort()),
    /** 0x001D, 보조표시정보 요청, 보조표시기에서 과거호출정보가 필요할 경우 순번발행기로 요청하는 패킷 (미사용)*/
    //SUB_SCREEN_REQUEST(0x001D.toShort()),
    /** 0x001E, 보조표시정보 응답, 표시기에서 순번발행기로 실행중임을 주기적으로 알리는 패킷 (미사용)*/
    //SUB_SCREEN_RESPONSE(0x001E.toShort()),
    /** 0x001F, 배경음악정보, 관리자에서 직원별 배경음악 설정시 순번발행기로부터 전송되는 패킷 (미사용)*/
    //BGM_INFO(0x001F.toShort()),
    /** 0x0020, 동영상 리스트 요청 (표시기 -> IQS) */
    MEDIA_LIST_REQUEST(0x0020.toShort()),
    /** 0x0021, 동영상 리스트 응답, 이미지도같이 내려옴 (IQS -> 표시기) */
    MEDIA_LIST_RESPONSE(0x0021.toShort()),
    /** 0x0022, 호출취소요청, 호출음성 재생중 중단이 필요한 경우 순번발행기에서 전송되는 패킷 (미사용)*/
    //CALL_CANCEL(0x0022.toShort()),
    /** 0x0023, 호출횟수설정, 1회 호출시 음성 출력 횟수를 설정하는 패킷 (미사용)*/
    //CALL_COLLECT_SET(0x0023.toShort()),
    /** 0x0024, 전산장애설정, 전산장애 화면의 표시 여부를 설정하는 패킷 (미사용)*/
    //ERROR_SET(0x0024.toShort()),
    /** 0x0026, 공석설정, 공석화면 표시 여부를 설정하는 패킷 (미사용) */
    //PJT_SET(0x0026.toShort()),

    // 상담 예약 관련 프로토콜 (Reservation)
    /** 0x0027, 상담예약리스트 요청, 순번발행기에 상담 예약 리스트 요청 시 전송되는 패킷 (표시기 -> IQS) */
    RESERVE_LIST_REQUEST(0x0027.toShort()),
    /** 0x0028, 상담예약리스트 응답, 순번발행기에 상담 예약 리스트 수신 패킷*/
    RESERVE_LIST_RESPONSE(0x0028.toShort()),
    /** 0x0029, 통신패킷정의서에 없음 */
    RESERVE_ADD_REQUEST(0x0029.toShort()),
    /** 0x002A, 통신패킷정의서에 없음 */
    RESERVE_ADD_RESPONSE(0x002A.toShort()),
    /** 0x002B, 통신패킷정의서에 없음 */
    RESERVE_UPDATE_REQUEST(0x002B.toShort()),
    /** 0x002C, 통신패킷정의서에 없음 */
    RESERVE_UPDATE_RESPONSE(0x002C.toShort()),
    /** 0x002D, 통신패킷정의서에 없음 */
    RESERVE_CANCEL_REQUEST(0x002D.toShort()),
    /** 0x002E, 통신패킷정의서에 없음 */
    RESERVE_CANCEL_RESPONSE(0x002E.toShort()),
    /** 0x002F, 상담예약 도착등록, 상담예약 손님이 도착등록한 경우 순번발행기로부터 전송되는 패킷*/
    RESERVE_ARRIVE_REQUEST(0x002F.toShort()),
    /** 0x0030, 상담예약 도착등록 응답  (표시기 -> IQS)*/
    RESERVE_ARRIVE_RESPONSE(0x0030.toShort()),
    /** 0x0031, 상담예약호출, 직원이 고객 호출 시 순번발행기로부터 전송되는 패킷 (IQS -> 표시기)*/
    RESERVE_CALL_REQUEST(0x0031.toShort()),
    /** 0x0032, 상담예약호출 응답, (미사용) */
    //RESERVE_CALL_RESPONSE(0x0032.toShort()),
    /** 0x0033, 상담예약재호출, 직원이 고객 재호출 시 순번발행기로부터 전송되는 패킷 */
    RESERVE_RE_CALL_REQUEST(0x0033.toShort()),
    /** 0x0034, 상담예약재호출 응답 (미사용)*/
    //RESERVE_RE_CALL_RESPONSE(0x0034.toShort()),
    /** 0x0035, 표시기 업데이트정보 요청, 순번발행기로 표시기 최종 버전 정보 요청할 때 전송하는 패킷 (표시기 -> IQS)*/
    UPDATE_INFO_REQUEST(0x0035.toShort()),
    /** 0x0036, 표시기 업데이트정보 응답, 업데이트 여부 체크해서 업데이트 대상 파일 정보 전송하는 패킷 (IQS -> 표시기)*/
    UPDATE_INFO_RESPONSE(0x0036.toShort()),

    // 2022-08-24 written by kshong
    // FTP로 로그 파일을 업로드 하던것을 TCP로 업로드를 수행하기 위한 프로토콜 ID
    /** 0x0037, 통신패킷정의서에 없음,  (표시기 -> IQS)*/
    UPLOAD_LOG_FILE_TO_SERVER(0x0037.toShort()),
    /** 0x0038, 통신패킷정의서에 없음, 안쓰는 것으로 보임 (표시기 -> IQS) */
    VIDEO_DOWNLOAD_REQUEST(0x0038.toShort()),
    /** 0x0039, 통신패킷정의서에 없음, 안쓰는 것으로 보임 */
    //VIDEO_DOWNLOAD_RESPONSE(0x0039.toShort()),

    // 접속시 FTP 다운로드 상태 리턴 받는 프로토콜
    /** 0x00F1, 패치파일 다운로드 시작, 통신패킷정의서에 없음 */
    START_PATCH(0x00F1.toShort()),
    /** 0x00F2, 패치파일 다운로드 종료, 통신패킷정의서에 없음 */
    END_PATCH(0x00F2.toShort()),
    /** 0x00F3, 이미지파일 다운로드 시작, 통신패킷정의서에 없음 */
    START_IMAGE(0x00F3.toShort()),
    /** 0x00F4, 이미지파일 다운로드 종료, 통신패킷정의서에 없음 */
    END_IMAGE(0x00F4.toShort()),
    /** 0x00F5, 비디오파일 다운로드 시작, 통신패킷정의서에 없음 */
    START_VIDEO(0x00F5.toShort()),
    /** 0x00F6, 비디오파일 다운로드 종료, 통신패킷정의서에 없음 */
    END_VIDEO(0x00F6.toShort()),
    /** 0x00F7, 사운드파일 다운로드 시작, 통신패킷정의서에 없음 */
    START_SOUND(0x00F7.toShort()),
    /** 0x00F8, 사운드파일 다운로드 종료, 통신패킷정의서에 없음 */
    END_SOUND(0x00F8.toShort()),

    // TCP Socket Excpetion 시 액티비티로 전달 하는 프로토콜
    /** 0x00F9, TCP 연결 소실 시 전달 */
    SERVICE_RETRY(0x00F9.toShort()),
    /** 0x00FA, 접속 승인 요청 중 단말기 IP 또는 Mac 주소를 받아오지 못하였을 경우, 이것도 안쓰는것으로 보인다. */
    //NO_IP_RETRY(0x00FA.toShort()),

    // 추가 패킷
    /** 0x402C, 직원 정보 갱신 요청 */
    TELLER_RENEW_REQUEST(0x402C.toShort()),
    /** 0x402D, 직원 정보 갱신 응답 */
    TELLER_RENEW_RESPONSE(0x402D.toShort());

    /**
     * 사용법
     *     val protocolId: Short = 0x0003 // 대기인수 정보 요청 패킷
     *     val protocol = getProtocolById(protocolId) */
    fun getProtocolById(protocolId: Short): ProtocolDefine? {
        return entries.find { it.value == protocolId }
    }

    override fun toString(): String {
        return "ProtocolName:${name}[0x${String.format("%04X", value.toInt() and 0xFFFF)}]"
    }
}

