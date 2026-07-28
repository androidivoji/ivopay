package com.example.ivopay.app.util

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Sha256 {
    fun getSHA256(str: String?): String? {
        val messageDigest: MessageDigest
        var encodestr: String? = ""
        try {
            messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(str?.toByteArray(charset("UTF-8")))
            encodestr = byteToHex(messageDigest.digest())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return encodestr
    }

    private fun byteToHex(byteArray: ByteArray):String{
        return with(StringBuilder()){
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexString = Integer.toHexString(hex)
                if (hexString.length==1){
                    append("0").append(hexString)
                }else{
                    append(hexString)
                }
            }
            toString()
        }
    }
}