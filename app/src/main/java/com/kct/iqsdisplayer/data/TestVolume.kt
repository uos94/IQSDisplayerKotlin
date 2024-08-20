package com.kct.iqsdisplayer.data

data class TestVolume(
    val volumeWin: Int = 0,
    val volumeSize: Int = 0,
    val volumeName :String = "",
    val playNum: Int = 0,
    val infoSound: Int = 0
) {
    override fun toString(): String {
        return "TestVolume(volumeWin=$volumeWin, volumeSize=$volumeSize, volumeName='$volumeName', playNum=$playNum, infoSound=$infoSound)"
    }
}

