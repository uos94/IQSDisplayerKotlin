package com.kct.iqsdisplayer.common


/**
 * IQS 간 TCP 통신 프로토콜 ID 정의 클래스
 * 모두 Short로 2Byte를 가짐
 */
object ProtocolDefine {
    const val AcceptRequest: Short          = 0xF0F0.toShort() // 접속성공 응답
    const val AcceptReject: Short           = 0xF0F1.toShort() // 접속거부 응답
    const val AcceptAuthRequest: Short      = 0x1001 // 접속승인 요청
    const val AcceptAuthResponse: Short     = 0x1002 // 접속승인 응답
    const val StartRequest: Short           = 0x0001 // 시작정보 요청(미사용)
    const val StartResponse: Short          = 0x0002 // 시작정보 응답(미사용)
    const val WaitRequest: Short            = 0x0003 // 대기정보 요청
    const val WaitResponse: Short           = 0x0004 // 대기정보 응답
    const val CallRequest: Short            = 0x0005 // 호출 요청
    const val CallResponse: Short           = 0x0006 // 호출 응답(미사용)
    const val ReCallRequest: Short          = 0x0007 // 재호출 요청
    const val ReCallResponse: Short         = 0x0008 // 재호출 응답(미사용)
    const val EmptyRequest: Short           = 0x0009 // 부재정보 요청
    const val EmptyResponse: Short          = 0x000A // 부재정보 응답(미사용)
    const val InfoMessageRequest: Short     = 0x000B // 안내메시지설정 요청
    const val InfoMessageResponse: Short    = 0x000C // 안내메시지설정 응답(미사용)
    const val Teller: Short                 = 0x000D // 직원정보 패킷
    const val InfoTeller: Short             = 0x000E // 직원 상세정보 패킷(미사용)
    const val SystemOFF: Short              = 0x000F // 시스템 종료 패킷
    const val VideoSet: Short               = 0x0010 // 동영상 설정 패킷

    const val VolumTest: Short              = 0x0011 // 볼륨 테스트 패킷
    const val SoundSet: Short               = 0x0012 // 음성 설정 패킷
    const val RestartRequest: Short         = 0x0013 // 재시작 요청
    const val RestartResponse: Short        = 0x0014 // 재시작 응답(미사용)
    const val CrowedRequest: Short          = 0x0015 // 혼잡 요청
    const val CrowedResponse: Short         = 0x0016 // 혼잡 응답(미사용)
    const val WinReqeust: Short             = 0x0017 // 창구 정보 요청(미사용)
    const val WinResponse: Short            = 0x0018 // 창구 정보 응답
    const val KeepaliveRequest: Short       = 0x0019 // KEEPALIVE 요청
    const val KeepalliveResponse: Short     = 0x001A // KEEPALIVE 응답
    const val InstallInfo: Short            = 0x001B // 설치정보 패킷
    const val DispayInfo: Short             = 0x001C // 화면표시정보 패킷
    const val SubScreenRequest: Short       = 0x001D // 보조표시정보 요청
    const val SubScreenResponse: Short      = 0x001E // 보조표시정보 응답
    const val BGMInfo: Short                = 0x001F // 배경음악정보 패킷
    const val VideoListRequest: Short       = 0x0020 // 비디오 리스트 요청
    const val VideoListResponse: Short      = 0x0021 // 비디오 리스트 응답
    const val CallCancel: Short             = 0x0022 // 호출 취소 요청
    const val CallCollectSet: Short         = 0x0023 // 호출회수 설정 패킷
    const val ErrorSet: Short               = 0x0024 // 전산장애 설정
    const val PJTSet: Short                 = 0x0026 // 공석 설정

    // 231130, by HAHU  아래 표시기->iqs, iqs->표시기  방향이 반대 아닌가?;;;;
    // 상담 예약 관련
    const val ReservListRequest: Short      = 0x0027 // 상담 예약 리스트 요청 패킷 ( 표시기 -> IQS )
    const val ReservListResponse: Short     = 0x0028 // 상담 예약 리스트 응답 패킷 ( IQS -> 표시기 )
    const val ReservAddRequest: Short       = 0x0029 // 상담 예약 리스트 추가 요청 패킷 ( 표시기 -> IQS )
    const val ReservAddResponse: Short      = 0x002A // 상담 예약 리스트 추가 응답 패킷 ( IQS -> 표시기 )
    const val ReservUpdateRequest: Short    = 0x002B // 상담 예약 리스트 수정 요청 패킷 ( 표시기 -> IQS )
    const val ReservUpdateResponse: Short   = 0x002C // 상담 예약 리스트 수정 응답 패킷 ( IQS -> 표시기)
    const val ReservCancleRequest: Short    = 0x002D // 상담 예약 리스트 취소 요청 패킷 ( 표시기 -> IQS )
    const val ReservCancleResponse: Short   = 0x002E // 상담 예약 리스트 취소 응답 패킷 ( IQS -> 표시기 )
    const val ReservArriveReqeust: Short    = 0x002F // 예약 고객 도착 등록 요청 패킷 ( IQS -> 표시기 )
    const val ReservArriveResponse: Short   = 0x0030 // 예약 고객 도착 등록 응답 패킷 ( 표시기 -> IQS )
    const val ReservCallRequest: Short      = 0x0031 // 예약 고객 호출 요청 패킷 ( IQS -> 표시기 )
    const val ReservCallResponse: Short     = 0x0032 // 예약 고객 호출 응답 패킷 ( 표시기 -> IQS )
    const val ReservReCallRequest: Short    = 0x0033 // 예약 고객 재호출 요청 패킷 ( IQS -> 표시기 )
    const val ReservReCallResponse: Short   = 0x0034 // 예약 고객 재호출 응답 패킷 ( 표시기 -> IQS )
    const val ReservUpdateInfoRequest: Short = 0x0035 // 업데이트 정보 요청 ( IQS -> 표시기 )
    const val ReservUpdateInfoResponse: Short = 0x0036 // 업데이트 정보 응답 ( 표시기 -> IQS )

    // 2022-08-24 written by kshong
    // FTP로 로그 파일을 업로드 하던것을 TCP로 업로드를 수행하기 위한 프로토콜 ID
    const val UpLoadLogFileToServerID: Short = 0x0037 // 업로드를 수행하기 위한 프로토콜 ID

    // 231211, by HAHU  광고파일 요청
    const val VideoDownLoadRequest: Short   = 0x0038 //
    const val VideoDownLoadResponse: Short  = 0x0039 // 비디오 리스트 응답

    // 접속시 FTP 다운로드 상태 리턴 받는 프로토콜
    const val StartPatch: Short     = 0x00F1 // 패치파일 다운로드 시작
    const val EndPatch: Short       = 0x00F2 // 패치파일 다운로드 종료
    const val StartImage: Short     = 0x00F3 // 이미지 파일 다운로드 시작
    const val EndImage: Short       = 0x00F4 // 이미지 파일 다운로드 종료
    const val StartVideo: Short     = 0x00F5 // 비디오 파일 다운로드 시작
    const val EndVideo: Short       = 0x00F6 // 비디오 파일 다운로드 종료
    const val StartSound: Short     = 0x00F7 // 사운드 파일 다운로드 시작
    const val EndSound: Short       = 0x00F8 // 사운드 파일 다운로드 종료

    // TCP Socket Excpetion 시 액티비티로 전달 하는 프로토콜
    const val ServiceRetry: Short   = 0x00F9 // TCP 연결 소실 시 전달

    // ADD 20-01-21 LSB
    const val NoIPRetry: Short      = 0x00FA // 접속 승인 요청 중 단말기 IP 또는 Mac 주소를 받아오지 못하였을 경우

    // 추가 패킷
    const val TellerRenewRequest: Short     = 0x402C // 직원 정보 갱신 요청
    const val TellerRenewResponse: Short    = 0x402D // 직원 정보 갱신 응답
}
