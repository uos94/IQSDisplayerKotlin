package com.kct.iqsdisplayer.data

data class ErrorInfo(
    var isAbsence: Boolean = false,
    var absenceMessage: String = "",
    var isNetWorkError: Boolean = false
)

