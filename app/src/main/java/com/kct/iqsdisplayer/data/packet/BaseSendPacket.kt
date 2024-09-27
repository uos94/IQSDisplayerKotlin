package com.kct.iqsdisplayer.data.packet

import com.kct.iqsdisplayer.network.PacketAnalyzer
import com.kct.iqsdisplayer.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class BaseSendPacket(private val protocolId: Short) {

    // 하위 클래스에서 구현해야 하는 추상 메서드: 데이터 배열 반환
    abstract fun getDataArray(): Array<Any>

    // ByteBuffer 생성 함수 (공통 로직)
    open fun toByteBuffer(): ByteBuffer {
        val dataSize = calculateDataSize(*getDataArray())

        return ByteBuffer.allocate(dataSize + PacketAnalyzer.HEADER_SIZE).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())   // 헤더 length   2byte
                .putShort(protocolId)           // 헤더 protocol 2byte
            putDataArray(*getDataArray())
        }
    }

    // 데이터 크기 계산 함수
    private fun calculateDataSize(vararg data: Any): Int {
        var size = PacketAnalyzer.HEADER_SIZE
        for (value in data) {
            size += when (value) {
                is Int -> 4
                is Short -> 2
                is String -> value.toByteArray().size + 1 // String에는 NULL 종료 문자 포함
                is ByteArray -> value.size
                else -> throw IllegalArgumentException("Unsupported data type: ${value::class.simpleName}")
            }
        }
        return size
    }

    // ByteBuffer 에 데이터 배열 추가 함수
    private fun ByteBuffer.putDataArray(vararg data: Any) {
        for (value in data) {
            putData(value)
        }
    }

    // ByteBuffer에 데이터 추가 함수 (NULL 종료 문자 자동 처리)
    private fun ByteBuffer.putData(data: Any) {
        when (data) {
            is Int -> putInt(data)
            is Short -> putShort(data)
            is String -> {
                put(data.toByteArray())
                put(0x00)
            }
            is ByteArray -> {
                put(data)
                put(0x00)
            }
            else -> throw IllegalArgumentException("Unsupported data type: ${data::class.simpleName}")
        }
    }
}
