package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.data.packet.receive.toAcceptAuthResponseData
import com.kct.iqsdisplayer.data.packet.receive.toCrowdedRequestData
import com.kct.iqsdisplayer.data.packet.receive.toEmptyRequestData
import com.kct.iqsdisplayer.data.packet.receive.toInfoMessageRequestData
import com.kct.iqsdisplayer.data.packet.receive.toReserveListResponseData
import com.kct.iqsdisplayer.data.packet.receive.toRestartRequestData
import com.kct.iqsdisplayer.data.packet.receive.toTellerRenewRequestData
import com.kct.iqsdisplayer.data.packet.receive.toUpdateInfoResponseData
import com.kct.iqsdisplayer.data.packet.receive.toWaitResponse
import com.kct.iqsdisplayer.data.packet.receive.toWinResponseData
import com.kct.iqsdisplayer.data.toCallRequestData
import com.kct.iqsdisplayer.data.toReserveAddRequestData
import com.kct.iqsdisplayer.data.toReserveArriveRequestData
import com.kct.iqsdisplayer.data.toReserveCancelRequestData
import com.kct.iqsdisplayer.data.toReserveUpdateRequestData
import com.kct.iqsdisplayer.data.toTellerData
import com.kct.iqsdisplayer.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

class PacketAnalyzer(inputStream: InputStream) {

    companion object {
        const val HEADER_SIZE = 4
        const val MAX_PACKET_SIZE = 8192
    }

    private val parserMap = mapOf<Short, Packet.() -> Any?>(
        ProtocolDefine.ACCEPT_REQUEST.value         to { null },
        ProtocolDefine.ACCEPT_REJECT.value          to { null },
        ProtocolDefine.ACCEPT_AUTH_RESPONSE.value   to Packet::toAcceptAuthResponseData,
        ProtocolDefine.WAIT_RESPONSE.value          to Packet::toWaitResponse,
        ProtocolDefine.CALL_REQUEST.value           to Packet::toCallRequestData,
        ProtocolDefine.RE_CALL_REQUEST.value        to Packet::toCallRequestData,
        ProtocolDefine.EMPTY_REQUEST.value          to Packet::toEmptyRequestData,
        ProtocolDefine.INFO_MESSAGE_REQUEST.value   to Packet::toInfoMessageRequestData,
        ProtocolDefine.TELLER.value                 to Packet::toTellerData,
        ProtocolDefine.SYSTEM_OFF.value             to { null },
        ProtocolDefine.VIDEO_SET.value              to { Packet::string },//TODO : 문서가 와야 확인가능할 것으로 보임
        ProtocolDefine.RESTART_REQUEST.value        to Packet::toRestartRequestData,
        ProtocolDefine.CROWDED_REQUEST.value        to Packet::toCrowdedRequestData,
        ProtocolDefine.WIN_RESPONSE.value           to Packet::toWinResponseData,
        ProtocolDefine.KEEP_ALIVE_RESPONSE.value    to { null },
        ProtocolDefine.VIDEO_LIST_RESPONSE.value    to { Packet::string },//TODO : 문서가 와야 확인가능할 것으로 보임
        ProtocolDefine.RESERVE_LIST_RESPONSE.value  to Packet::toReserveListResponseData,
        // 예약 추가,수정,취소의 경우 안쓰는것 같다는데 확실치 않음.
        ProtocolDefine.RESERVE_ADD_REQUEST.value    to  Packet::toReserveAddRequestData,
        ProtocolDefine.RESERVE_UPDATE_REQUEST.value to  Packet::toReserveUpdateRequestData,
        ProtocolDefine.RESERVE_CANCEL_REQUEST.value to  Packet::toReserveCancelRequestData,
        ProtocolDefine.RESERVE_ARRIVE_REQUEST.value to  Packet::toReserveArriveRequestData,
        // 예약 호출 요청 패킷은 사용함.
        ProtocolDefine.RESERVE_CALL_REQUEST.value   to  Packet::toCallRequestData,
        ProtocolDefine.RESERVE_RE_CALL_REQUEST.value to  Packet::toCallRequestData,
        ProtocolDefine.TELLER_RENEW_REQUEST.value   to  Packet::toTellerRenewRequestData,
        ProtocolDefine.UPDATE_INFO_RESPONSE.value   to Packet::toUpdateInfoResponseData
    )

    private var protocolId: ProtocolDefine? = null
    private var parsedData: Any? = null


    init {
        val headerBytes = ByteArray(HEADER_SIZE)
        val bytesRead = inputStream.read(headerBytes)
        if (bytesRead != HEADER_SIZE) {
            throw IOException("잘못된 헤더길이 : $bytesRead")
        }

        val headerBuffer = ByteBuffer.wrap(headerBytes)
        val dataLength = headerBuffer.short.toInt() and 0xFFFF
        val protocolIdValue = headerBuffer.short

        //TODO : 이것 좀 어떻게 해라.
        protocolId = ProtocolDefine.entries.find { it.value == protocolIdValue }
            ?: throw IllegalArgumentException("정의되지 않은 프로토콜[${protocolIdValue.toInt()}]")

        if (dataLength > MAX_PACKET_SIZE - HEADER_SIZE) {
            throw IOException("패킷 사이즈가 초과되었습니다. dataLength[$dataLength]")
        }

        val dataBytes = ByteArray(dataLength)
        val dataBytesRead = inputStream.read(dataBytes)
        if (dataBytesRead != dataLength) { //검증용..데이터 길이가 맞지 않아도 진행한다.
            Log.w("데이터 길이가 실제 사이즈와 맞지 않습니다.")
        }

        val packet = Packet(dataBytes, headerBytes)
        if(parserMap.containsKey(protocolId?.value)) {
            parserMap[protocolId?.value]?.let { parsedData = packet.it() }
        }
        else {
            Log.e("프로토콜[${protocolId?.value}에 대한 처리가 parseMap에 등록되지 않았습니다.")
            throw IllegalArgumentException("정의되지 않은 프로토콜[${protocolId?.value?.toInt()}]")
        }
    }

    fun getProtocolId(): ProtocolDefine? {
        return protocolId
    }

    fun getData(): Any? {
        return parsedData
    }

    fun reset() {
        protocolId = null
        parsedData = null
    }
}
