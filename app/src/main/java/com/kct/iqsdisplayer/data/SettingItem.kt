package com.kct.iqsdisplayer.data

data class SettingItem(
    val mainText: String = "",
    val subText: String = "",
    val prefKey: String = "") {

    fun isTitleItem() = mainText.isNotEmpty() && subText.isEmpty()
}
