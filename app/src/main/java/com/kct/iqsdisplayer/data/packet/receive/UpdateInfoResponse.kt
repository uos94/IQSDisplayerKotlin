package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.network.Packet

data class UpdateInfoResponse(
    var updateType: Int = 0,
    var updateSize: Int = 0,
    var updateFileName: String = ""
){
    override fun toString(): String {
        return "UpdateInfoResponse(updateType=$updateType, updateSize=$updateSize, updateFileName='$updateFileName')"
    }
}

fun Packet.toUpdateInfoResponseData(): UpdateInfoResponse {
    val updateType = integer
    val updateSize = integer
    val updateFileName = string
    return UpdateInfoResponse(updateType, updateSize, updateFileName)
}

/**
 * 아래 주석은 참고용 (기존 코드 주석 )
 */
// =====================================================================================
//2022.07.21 dyyoon 업데이트 해야 되는지 확인
// - 수신된 Packet 구조
//   length(2byte) + id(2byte) + data(n byte)
// -------------------------------------------------------------------------------------
// - *.APK 또는 *.WAV 를 업데이트 하는 경우 처리 순서
//   (1) Header 부(length(2byte) + id(2byte))는 AnalysisID()에서 처리되었음.
//       따라서 아래의 부분에서 따로 처리할 것 없음.
//   (2) 첫번째 Packet data 부분 처리
//       1) Packet 구조 : command(4byte) + download될 file size(4byte) + file name(nbyte)
//       2) command(update)가 1인 경우로 각 파일 사이스와 이름을 클래스 변수에 저장한다.
//   (3) 두번째 Packet data 부분 부터 마지막 Packet 처리(command(update)가 2인 경우)
//       1) Packet 구조 : command(4byte) + file 내용(nbyte)
//       2) 첫번째 Packet에서 만든 파일에 파일 내용을 저장한다.
//       3) 마지막 까지 저장하고 첫번째 수신한 파일 사이즈와 비교하여 같으면 정상 처리한다.
// -------------------------------------------------------------------------------------
// - update 명령어 의미
//   (1) update 0 인 경우 : 다운로드할 파일이 없는 경우로 MainActivity 로 전환한다.
//   (2) update 1 인 경우 : 다운로드할 파일의 첫번째 처리 부분이다.
//   (3) update 2 인 경우 : 다운로드할 파일을 반복해서 처리하는 부분이다.
//   (4) update 3 인 경우 : 실제 다운로드 파일의 크기를 가져올 수 없는 경우이다.
//                         따라서, 사운드디스플레이 정상 작동을 위해 MainActivity 로 전환한다.
// =====================================================================================