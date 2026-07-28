package com.example.ivopay.app.util

object JniFuncs {
    init {
        System.loadLibrary("k5V5Ll8r01joxQG")
    }

    val versionString: String?
        external get

    external fun getSaltySign(src: String?, channel: String?): String?

    external fun getSalt(appname: String?, channel: String?): String?

    val afSdkKey: String?
        external get
}