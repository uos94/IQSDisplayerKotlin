package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.network.Packet
import com.kct.iqsdisplayer.network.ProtocolDefine

/**
 * updateType이 1일때는 fileSize와 fileName이 온다.
 * updateType이 2일때는 fileSize와 fileName이 없다.
 */
data class UpdateInfoResponse(
    var updateType: Int = 0,
    var updateSize: Int = 0,
    var updateFileName: String = "",
    var dataArray: ByteArray? = null
    , override var protocolDefine: ProtocolDefine? = ProtocolDefine.UPDATE_INFO_RESPONSE
) : BaseReceivePacket() {
    override fun toString(): String {
        return "UpdateInfoResponse(updateType=$updateType, updateSize=$updateSize, updateFileName='$updateFileName')"
    }

    /** 사용할일은 없음 */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateInfoResponse

        if (updateType != other.updateType) return false
        if (updateSize != other.updateSize) return false
        if (updateFileName != other.updateFileName) return false
        if (dataArray != null) {
            if (other.dataArray == null) return false
            if (!dataArray.contentEquals(other.dataArray)) return false
        } else if (other.dataArray != null) return false
        if (protocolDefine != other.protocolDefine) return false

        return true
    }
    /** 사용할일은 없음 */
    override fun hashCode(): Int {
        var result = updateType
        result = 31 * result + updateSize
        result = 31 * result + updateFileName.hashCode()
        result = 31 * result + (dataArray?.contentHashCode() ?: 0)
        result = 31 * result + (protocolDefine?.hashCode() ?: 0)
        return result
    }
}

fun Packet.toUpdateInfoResponse(): UpdateInfoResponse {
    val updateType = integer
    val result = UpdateInfoResponse()
    if(updateType == 1) {
        result.updateType = updateType
        result.updateSize = integer
        result.updateFileName = string
    }
    else {
        result.updateType = updateType
        result.dataArray = ByteArray(getData().remaining()) { getData().get() }
    }
    return result
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