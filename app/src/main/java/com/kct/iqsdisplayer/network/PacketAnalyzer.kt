package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.data.packet.receive.EmptyData
import com.kct.iqsdisplayer.data.packet.receive.toAcceptAuthResponse
import com.kct.iqsdisplayer.data.packet.receive.toCrowdedRequest
import com.kct.iqsdisplayer.data.packet.receive.toPausedWorkRequest
import com.kct.iqsdisplayer.data.packet.receive.toInfoMessageRequest
import com.kct.iqsdisplayer.data.packet.receive.toMediaListResponse
import com.kct.iqsdisplayer.data.packet.receive.toRestartRequest
import com.kct.iqsdisplayer.data.packet.receive.toTellerRenewRequest
import com.kct.iqsdisplayer.data.packet.receive.toUpdateInfoResponse
import com.kct.iqsdisplayer.data.packet.receive.toWaitResponse
import com.kct.iqsdisplayer.data.packet.receive.toWinResponse
import com.kct.iqsdisplayer.data.toCallRequest
import com.kct.iqsdisplayer.data.toReserveAddRequest
import com.kct.iqsdisplayer.data.toReserveArriveRequest
import com.kct.iqsdisplayer.data.toReserveCancelRequest
import com.kct.iqsdisplayer.data.toReserveListResponse
import com.kct.iqsdisplayer.data.toReserveUpdateRequest
import com.kct.iqsdisplayer.data.toTellerList
import com.kct.iqsdisplayer.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

class PacketAnalyzer(inputStream: InputStream) {

    companion object {
        const val HEADER_SIZE = 4
        const val MAX_PACKET_SIZE = 8192
    }

    private val parserMap = mapOf<Short, Packet.() -> BaseReceivePacket?>(
        ProtocolDefine.CONNECT_SUCCESS.value        to { null },
        ProtocolDefine.CONNECT_REJECT.value         to { null },
        ProtocolDefine.ACCEPT_AUTH_RESPONSE.value   to Packet::toAcceptAuthResponse,
        ProtocolDefine.WAIT_RESPONSE.value          to Packet::toWaitResponse,
        ProtocolDefine.CALL_REQUEST.value           to Packet::toCallRequest,
        ProtocolDefine.RE_CALL_REQUEST.value        to Packet::toCallRequest,
        ProtocolDefine.PAUSED_WORK_REQUEST.value    to Packet::toPausedWorkRequest,
        ProtocolDefine.INFO_MESSAGE_REQUEST.value   to Packet::toInfoMessageRequest,
        ProtocolDefine.TELLER_LIST.value            to Packet::toTellerList,
        ProtocolDefine.SYSTEM_OFF.value             to { null },
        ProtocolDefine.SERVICE_RETRY.value          to { null },
        ProtocolDefine.RESTART_REQUEST.value        to Packet::toRestartRequest,
        ProtocolDefine.CROWDED_REQUEST.value        to Packet::toCrowdedRequest,
        ProtocolDefine.WIN_RESPONSE.value           to Packet::toWinResponse,
        ProtocolDefine.KEEP_ALIVE_RESPONSE.value    to { null },
        ProtocolDefine.MEDIA_LIST_RESPONSE.value    to Packet::toMediaListResponse,
        ProtocolDefine.RESERVE_LIST_RESPONSE.value  to Packet::toReserveListResponse,
        // 예약 추가,수정,취소의 경우 안쓰는것 같다는데 확실치 않음.
        ProtocolDefine.RESERVE_ADD_REQUEST.value    to  Packet::toReserveAddRequest,
        ProtocolDefine.RESERVE_UPDATE_REQUEST.value to  Packet::toReserveUpdateRequest,
        ProtocolDefine.RESERVE_CANCEL_REQUEST.value to  Packet::toReserveCancelRequest,
        ProtocolDefine.RESERVE_ARRIVE_REQUEST.value to  Packet::toReserveArriveRequest,
        // 예약 호출 요청 패킷은 사용함.
        ProtocolDefine.RESERVE_CALL_REQUEST.value   to  Packet::toCallRequest,
        ProtocolDefine.RESERVE_RE_CALL_REQUEST.value to Packet::toCallRequest,
        ProtocolDefine.TELLER_RENEW_REQUEST.value   to  Packet::toTellerRenewRequest,
        ProtocolDefine.UPDATE_INFO_RESPONSE.value   to  Packet::toUpdateInfoResponse
    )

    private var protocolId: ProtocolDefine? = null
    private var parsedData: BaseReceivePacket = EmptyData()

    init {
        val headerBytes = ByteArray(HEADER_SIZE)
        val bytesRead = inputStream.read(headerBytes)
        if (bytesRead != HEADER_SIZE) {
            throw IOException("잘못된 헤더길이 : $bytesRead")
        }

        val headerBuffer = ByteBuffer.wrap(headerBytes)
        val dataLength = headerBuffer.short.toInt() and 0xFFFF
        val protocolIdValue = headerBuffer.short

        protocolId = ProtocolDefine.entries.find { it.value == protocolIdValue }

        if(protocolId == null) {
            Log.w("정의되지 않은 프로토콜[${protocolIdValue.toInt()}")
        }
        else {
            if (dataLength > MAX_PACKET_SIZE - HEADER_SIZE) {
                Log.w("패킷 사이즈가 초과되었습니다. dataLength[$dataLength]")
                //초과되도 진행은 가능
            }

            val dataBytes = ByteArray(dataLength)
            val dataBytesRead = inputStream.read(dataBytes)
            if (dataBytesRead != dataLength) { //검증용..데이터 길이가 맞지 않아도 진행한다.
                Log.w("데이터 길이가 실제 사이즈와 맞지 않습니다.")
            }

            val packet = Packet(dataBytes, headerBytes)
            if(parserMap.containsKey(protocolId?.value)) {
                val parserFunction = parserMap[protocolId?.value]
                parsedData = if (parserFunction != null) {
                    packet.parserFunction() ?: EmptyData(protocolId)
                } else {
                    EmptyData(protocolId)
                }
            }
            else {
                Log.w("프로토콜[${protocolId?.value}에 대한 처리가 parseMap에 등록되지 않았습니다.")
            }
        }
    }

    fun getProtocolId(): ProtocolDefine? {
        return protocolId
    }

    fun getData(): BaseReceivePacket {
        return parsedData
    }

}
