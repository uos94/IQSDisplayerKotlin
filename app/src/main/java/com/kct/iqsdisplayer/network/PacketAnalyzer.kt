package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.data.packet.BaseReceivePacket
import com.kct.iqsdisplayer.data.packet.receive.EmptyData
import com.kct.iqsdisplayer.data.packet.receive.toAcceptAuthData
import com.kct.iqsdisplayer.data.packet.receive.toCrowdedData
import com.kct.iqsdisplayer.data.packet.receive.toInfoMessage
import com.kct.iqsdisplayer.data.packet.receive.toMediaListData
import com.kct.iqsdisplayer.data.packet.receive.toPausedWorkData
import com.kct.iqsdisplayer.data.packet.receive.toRestartRequest
import com.kct.iqsdisplayer.data.packet.receive.toTellerRenewData
import com.kct.iqsdisplayer.data.packet.receive.toUpdateInfoData
import com.kct.iqsdisplayer.data.packet.receive.toWaitData
import com.kct.iqsdisplayer.data.packet.receive.toWinInfos
import com.kct.iqsdisplayer.data.packet.receive.toCallRequest
import com.kct.iqsdisplayer.data.packet.receive.toReserveAdd
import com.kct.iqsdisplayer.data.packet.receive.toReserveArrive
import com.kct.iqsdisplayer.data.packet.receive.toReserveCallData
import com.kct.iqsdisplayer.data.packet.receive.toReserveCancel
import com.kct.iqsdisplayer.data.packet.receive.toReserveList
import com.kct.iqsdisplayer.data.packet.receive.toReserveUpdate
import com.kct.iqsdisplayer.data.packet.receive.toTellerList
import com.kct.iqsdisplayer.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PacketAnalyzer(inputStream: InputStream) {

    companion object {
        const val HEADER_SIZE       = 4
        const val MAX_PACKET_SIZE   = 8192
    }

    /** 패킷에 따라 수행해야 할 funtion을 정의해 둔다. **/
    private val parserMap = mapOf<Short, Packet.() -> BaseReceivePacket?>(
        ProtocolDefine.CONNECT_SUCCESS.value        to { EmptyData(ProtocolDefine.CONNECT_SUCCESS) },
        ProtocolDefine.CONNECT_REJECT.value         to { EmptyData(ProtocolDefine.CONNECT_REJECT) },
        ProtocolDefine.ACCEPT_AUTH_RESPONSE.value   to Packet::toAcceptAuthData,
        ProtocolDefine.WAIT_RESPONSE.value          to Packet::toWaitData,
        ProtocolDefine.CALL_REQUEST.value           to Packet::toCallRequest,
        ProtocolDefine.RE_CALL_REQUEST.value        to Packet::toCallRequest,
        ProtocolDefine.PAUSED_WORK_REQUEST.value    to Packet::toPausedWorkData,
        ProtocolDefine.INFO_MESSAGE_REQUEST.value   to Packet::toInfoMessage,
        ProtocolDefine.TELLER_LIST.value            to Packet::toTellerList,
        ProtocolDefine.SYSTEM_OFF.value             to { EmptyData(ProtocolDefine.SYSTEM_OFF) },
        ProtocolDefine.SERVICE_RETRY.value          to { EmptyData(ProtocolDefine.SERVICE_RETRY) },
        ProtocolDefine.RESTART_REQUEST.value        to Packet::toRestartRequest,
        ProtocolDefine.CROWDED_REQUEST.value        to Packet::toCrowdedData,
        ProtocolDefine.WIN_RESPONSE.value           to Packet::toWinInfos,
        ProtocolDefine.KEEP_ALIVE_RESPONSE.value    to { EmptyData(ProtocolDefine.KEEP_ALIVE_RESPONSE) },
        ProtocolDefine.MEDIA_LIST_RESPONSE.value    to Packet::toMediaListData,
        ProtocolDefine.RESERVE_LIST_RESPONSE.value  to Packet::toReserveList,
        // 예약 추가,수정,취소의 경우 안쓰는것 같다는데 확실치 않음.
        ProtocolDefine.RESERVE_ADD_REQUEST.value    to  Packet::toReserveAdd,
        ProtocolDefine.RESERVE_UPDATE_REQUEST.value to  Packet::toReserveUpdate,
        ProtocolDefine.RESERVE_CANCEL_REQUEST.value to  Packet::toReserveCancel,
        ProtocolDefine.RESERVE_ARRIVE_REQUEST.value to  Packet::toReserveArrive,
        // 예약 호출 요청 패킷은 사용함.
        ProtocolDefine.RESERVE_CALL_REQUEST.value   to  Packet::toReserveCallData,
        ProtocolDefine.RESERVE_RE_CALL_REQUEST.value to Packet::toReserveCallData,
        ProtocolDefine.TELLER_RENEW_REQUEST.value   to  Packet::toTellerRenewData,
        ProtocolDefine.UPDATE_INFO_RESPONSE.value   to  Packet::toUpdateInfoData
    )

    private var protocolId: ProtocolDefine? = null
    private var parsedData: BaseReceivePacket = EmptyData()

    init {
        try {
            val headerBytes = ByteArray(HEADER_SIZE)
            var totalBytesRead: Int
            val headerRead = inputStream.read(headerBytes)
            if (headerRead != HEADER_SIZE) {
                throw IOException("잘못된 헤더길이 : $headerRead")
            }

            val headerBuffer = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
            val dataLength = headerBuffer.short.toInt() and 0xFFFF
            val protocolIdValue = headerBuffer.short

            protocolId = ProtocolDefine.entries.find { it.value == protocolIdValue }

            if (protocolId == null) {
                Log.w("정의되지 않은 프로토콜[0x${String.format("%04X", protocolIdValue.toInt() and 0xFFFF)}]")
            } else {
                if (dataLength > MAX_PACKET_SIZE - HEADER_SIZE) {
                    Log.w("패킷 사이즈가 초과되었습니다. dataLength[$dataLength]")
                }

                val dataBytes = ByteArray(dataLength)
                totalBytesRead = 0
                while (totalBytesRead < dataLength) {
                    val bytesRead = inputStream.read(dataBytes, totalBytesRead, dataLength - totalBytesRead)
                    if (bytesRead == -1) {
                        Log.d("데이터 읽기완료")
                        break
                    }
                    totalBytesRead += bytesRead
                }

                if (totalBytesRead != dataLength) { //검증용..데이터 길이가 맞지 않아도 진행한다.
                    Log.w("데이터 길이가 실제 사이즈와 맞지 않습니다.")
                }

                val packet = Packet(headerBytes, dataBytes)
                if (parserMap.containsKey(protocolId?.value)) {
                    parsedData = parserMap[protocolId?.value]?.invoke(packet) ?: EmptyData(protocolId)
                } else {
                    Log.w("프로토콜[${protocolId?.value}]에 대한 처리가 parseMap에 등록되지 않았습니다.")
                    parsedData = EmptyData(protocolId)
                }
            }
        } catch (e: Exception) { throw e }
    }

    fun getProtocolId(): ProtocolDefine? {
        return protocolId
    }

    fun getData(): BaseReceivePacket {
        return parsedData
    }

}
