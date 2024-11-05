package com.kct.iqsdisplayer.data.packet.receive

import com.kct.iqsdisplayer.common.Const

data class BackupCallData(
    var callNum: Int = 0,                     //호출번호
    var backupWinName: String = "",           //백업창구명
    var backupWinNum: Int = 0,                //백업표시기번호
    var bkWay: Const.Arrow = Const.Arrow.LEFT //백업방향
)

