package com.example.ivopay.app.util

import com.blankj.utilcode.util.MetaDataUtils

object ChannelUtils {
    val isTestEnv: Boolean
        get() = appChannel.equals("TestEnv", ignoreCase = true)
    val appChannel: String
        get() = MetaDataUtils.getMetaDataInApp("APP_CHANNEL")
}