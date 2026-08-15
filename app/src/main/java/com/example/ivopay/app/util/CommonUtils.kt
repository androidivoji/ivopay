package com.example.ivopay.app.util

import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

object CommonUtils {

    // Ganti dari: _formatMoney(rpNum) & _getRp(num)
    fun formatRupiah(amount: Double?): String {
        if (amount == null) return "Rp.0"
        return try {
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
                maximumFractionDigits = 0
            }
            numberFormat.format(amount).replace("Rp", "Rp.")
        } catch (e: Exception) {
            "Rp.0"
        }
    }

    // Ganti dari: _digitFormat(value) / _restrictToNumbers
    fun restrictToNumbers(input: String): String {
        return input.replace("\\D".toRegex(), "")
    }

    // Ganti dari: _restrictToLetter
    fun restrictToLetters(input: String): String {
        return input.replace("[^A-Za-z\\s]".toRegex(), "")
    }

    // Ganti dari: _generateRandomString()
    fun generateRandomString(length: Int = 19): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars[java.util.Random().nextInt(chars.length)] }
            .joinToString("")
    }

    fun generateSessionId(): String {
        val randomStr = generateRandomString(19)
        val timestamp = System.currentTimeMillis()
        return "$randomStr$timestamp"
    }

    // Ganti dari: _isValidPhoneNo()
    fun isValidPhoneNo(number: String?): Boolean {
        if (number == null) return false
        val cleanNumber = number.replace("[^0-9+]".toRegex(), "")
        return cleanNumber.startsWith("+62") ||
                cleanNumber.startsWith("08") ||
                cleanNumber.startsWith("62")
    }

    // Ganti dari: _maskIdNumber() (KTP Masking)
    fun maskKtpNumber(idNumber: String?): String {
        if (idNumber.isNullOrEmpty()) return ""
        if (idNumber.length != 16) return idNumber
        return idNumber.take(6) + "******" + idNumber.takeLast(4)
    }
}