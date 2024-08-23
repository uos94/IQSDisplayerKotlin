import com.kct.iqsdisplayer.BuildConfig
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.network.ProtocolDefine
import com.kct.iqsdisplayer.util.Log
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * TCP 통신 시 순번 발행기로 보낼 ByteBuffer를 만드는 클래스
 */
class SendBufferClass() { // ScreenInfoManager 주입

    private val nullValue: Byte = 0x00 // 1byte NULL

    // 접속 승인 요청
    fun acceptAuthRequest(): ByteBuffer? {
        val code = ProtocolDefine.ACCEPT_AUTH_REQUEST.value
        val mode = if (Const.CommunicationInfo.CALLVIEW_MODE == "3") 0x14 else 0x02
        val ip = Const.CommunicationInfo.MY_IP
        val mac = Const.CommunicationInfo.MY_MAC
        val version = BuildConfig.VERSION_NAME

        Log.d("ip: $ip, mac: $mac, version: $version")

        // null 체크를 먼저 수행하여 조기 반환
        if (ip == null || mac == null) {
            return null
        }

        val dataSize = 4 + ip.toByteArray().size + mac.toByteArray().size + version.toByteArray().size + 4

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
                .putInt(mode)
                .put(ip.toByteArray())
                .put(nullValue)
                .put(mac.toByteArray())
                .put(nullValue)
                .put(version.toByteArray())
                .put(nullValue)
        }
    }

    // 대기 정보 요청
    fun waitRequest(): ByteBuffer {
        Log.d("WaitRequest")
        val code = ProtocolDefine.WAIT_REQUEST.value
        val winID = ScreenInfo.instance.winID // ScreenInfoManager에서 winID 가져오기
        val dataSize = 4

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
                .putInt(winID)
        }
    }

    // Keep-Alive 요청
    fun keepAlive(): ByteBuffer {
        val code = ProtocolDefine.KEEP_ALIVE_REQUEST.value
        val dataSize = 0

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
        }
    }

    // 예약 리스트 요청
    fun reserveList(): ByteBuffer {
        val code = ProtocolDefine.RESERVE_LIST_REQUEST.value
        val dataSize = 0

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
        }
    }

    // 설치 정보 패킷
    fun installInfo(): ByteBuffer {
        val code = ProtocolDefine.INSTALL_INFO.value
        val iqsIP: String? = null
        val bkServerIP: String? = null
        val subDisplayID: String? = null
        val subDisplayFlag: String? = null
        val subDisplayIP: String? = null

        val sendData = "$iqsIP&$bkServerIP&$subDisplayID&$subDisplayFlag&$subDisplayIP"

        val dataSize = sendData.toByteArray().size

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
                .put(sendData.toByteArray())
        }
    }

    // 화면 표시 정보 패킷
    fun screenInfo(): ByteBuffer {
        val code = ProtocolDefine.DISPLAY_INFO.value
        val dataSize = 0

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
        }
    }

    // 보조 표시 정보 요청
    fun subScreenRequest(): ByteBuffer {
        val code = ProtocolDefine.SUB_SCREEN_REQUEST.value
        val dataSize = 0

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
        }
    }

    // 광고 파일 요청
    fun videoDownLoadRequest(): ByteBuffer {
        val code = ProtocolDefine.VIDEO_DOWNLOAD_REQUEST.value
        val dataSize = 0

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
        }
    }

    // 비디오 리스트 요청
    fun videoListRequest(): ByteBuffer {
        val code = ProtocolDefine.VIDEO_LIST_REQUEST.value

        val videoPath = Const.Path.DIR_VIDEO
        val dirVideo = File(videoPath)

        val sendData = try {
            dirVideo.listFiles()
                ?.filter { it.isFile } // 파일만 필터링
                ?.joinToString("#") {
                    "${it.name};${it.length()}"
                } ?: ""
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }

        val dataSize = 4 + sendData.toByteArray().size

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
                .put(sendData.toByteArray())
                .put(nullValue)
        }
    }

    // 앱 버전 정보 요청
    fun appVersionRequest(): ByteBuffer {
        val code = ProtocolDefine.RESERVE_UPDATE_INFO_REQUEST.value
        val versionName = BuildConfig.VERSION_NAME
        val dataSize = versionName.length + 1

        return ByteBuffer.allocate(dataSize + 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
                .putShort(dataSize.toShort())
                .putShort(code)
                .put(versionName.toByteArray())
                .put(nullValue)
        }
    }
}
