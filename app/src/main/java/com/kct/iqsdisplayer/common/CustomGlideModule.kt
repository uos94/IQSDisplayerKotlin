package com.kct.iqsdisplayer.common

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class CustomGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.VERBOSE) //이미지 없는 경우 404로그가 너무 많이 나와서 LogLevel 조정함.
        //super.applyOptions(context, builder)
    }
}