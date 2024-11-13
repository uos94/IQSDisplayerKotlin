package com.kct.iqsdisplayer.data.packet.receive

data class WinInfo(
    var winId: Int = 0,
    var winName: String = "",
    var waitNum: Int = 0
) {
    override fun toString(): String {
        return """
            |  (winId=$winId, winName='$winName', waitNum=$waitNum)
        """.trimMargin()
    }
}

