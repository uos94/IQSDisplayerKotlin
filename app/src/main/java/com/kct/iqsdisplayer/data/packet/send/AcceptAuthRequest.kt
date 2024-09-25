package com.kct.iqsdisplayer.data.packet.send

import com.kct.iqsdisplayer.BuildConfig
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.data.packet.BaseSendPacket
import com.kct.iqsdisplayer.network.ProtocolDefine

/**
 * Mode값
 * 표시기(0x02) : 기본 출화면,CALLVIEW_MODE=0에 해당됨.
 * 보조표시기(0x0A) : 보조표시기화면,CALLVIEW_MODE=2에 해당됨.
 * 음성호출기(0x14) : 음성호출표시기,CALLVIEW_MODE=3에 해당됨.
 */
data class AcceptAuthRequest(
    val ip: String,
    val mac: String,
    val version: String = BuildConfig.VERSION_NAME,
    val mode: Int = if (Const.ConnectionInfo.CALLVIEW_MODE == Const.CallViewMode.SOUND) 0x14 else 0x02,
    val code: Short = ProtocolDefine.ACCEPT_AUTH_REQUEST.value
) : BaseSendPacket(code) {

    override fun toString(): String {
        return "AcceptAuthRequestData(mode=$mode, ip='$ip', mac='$mac', version='$version')"
    }

    override fun getDataArray(): Array<Any> {
        return arrayOf(
            mode,
            ip,
            mac,
            version
        )
    }
}