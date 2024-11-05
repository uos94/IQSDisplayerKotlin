package com.kct.iqsdisplayer.data.packet.receive

data class ErrorInfo(
    var isAbsence: Boolean = false,
    var absenceMessage: String = "",
    var isNetWorkError: Boolean = false
)

