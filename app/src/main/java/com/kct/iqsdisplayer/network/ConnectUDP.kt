package com.kct.iqsdisplayer.network

import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException

/**
 * 필요 없는 것으로 보임.
 */
class ConnectUDP : Thread() {
    private var udpSocket: DatagramSocket? = null

    override fun run() {
        super.run()
        try {
            udpSocket = DatagramSocket(Const.ConnectionInfo.UDP_PORT)

            val receiveBuf = ByteArray(8192)
            val packet = DatagramPacket(receiveBuf, receiveBuf.size)
            udpSocket?.receive(packet) // null-safe 호출

            Log.d("UDP Receive")
        } catch (e: SocketException) {
            Log.e("ConnectUDP : SocketException (${e.message})", e)
        } catch (e: IOException) {
            Log.e("ConnectUDP : IOException (${e.message})", e)
        } finally {
            udpSocket?.close() // 소켓 닫기
        }
    }
}