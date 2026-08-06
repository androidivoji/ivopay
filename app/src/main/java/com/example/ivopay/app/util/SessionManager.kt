package com.example.ivopay.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        // Membuat master key untuk enkripsi otomatis
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Menggantikan fungsi localStorage dengan tingkat keamanan EncryptedSharedPreferences
        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ==========================================
    // GETTER METHODS
    // ==========================================

    // Menggantikan check status login native
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Menggantikan localStorage.getItem('role')
    fun getUserRole(): Int {
        return sharedPreferences.getInt(KEY_USER_ROLE, 0)
    }

    // Mengambil Auth Token
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    // Menggantikan localStorage.getItem('mob')
    fun getMobileNumber(): String? {
        return sharedPreferences.getString(KEY_MOBILE_NUMBER, "")
    }

    // Mengambil Nama Lengkap User (su.pi.fun)
    fun getUserFullName(): String? {
        return sharedPreferences.getString(KEY_FULL_NAME, "")
    }

    // Mengambil status pgsh (su.cme.pgsh)
    fun getHasPgsh(): Boolean {
        return sharedPreferences.getBoolean(KEY_PGSH, false)
    }

    fun getApiService(): String? {
        return sharedPreferences.getString(KEY_API_SERVICE, "")
    }

    // Update status pgsh secara terpisah
    fun savePgshStatus(hasPgsh: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PGSH, hasPgsh).apply()
    }

    // Menggantikan localStorage.getItem('act')
    fun getActStatus(): String {
        return sharedPreferences.getString(KEY_ACT, "") ?: ""
    }

    // Menggantikan localStorage.setItem('act', data.act ? "1" : "")
    fun saveActStatus(isActive: Boolean) {
        sharedPreferences.edit().putString(KEY_ACT, if (isActive) "1" else "").apply()
    }

    // Mendapatkan Old Token (token pemulihan)
    fun getOldToken(): String? {
        return sharedPreferences.getString(KEY_OLD_TOKEN, null)
    }

    // Menyimpan Old Token
    fun saveOldToken(token: String) {
        sharedPreferences.edit().putString(KEY_OLD_TOKEN, token).apply()
    }

    // Menghapus Old Token
    fun removeOldToken() {
        sharedPreferences.edit().remove(KEY_OLD_TOKEN).apply()
    }

    // Mendapatkan nomor telepon yang tersimpan (untuk gesture login check)
    fun getSavedPhoneNumber(): String? {
        return sharedPreferences.getString(KEY_SAVED_PHONE_NUMBER, null)
    }

    // Menyimpan nomor telepon untuk pengecekan selanjutnya
    fun saveSavedPhoneNumber(phone: String) {
        sharedPreferences.edit().putString(KEY_SAVED_PHONE_NUMBER, phone).apply()
    }

    // ==========================================
    // SETTER METHODS
    // ==========================================

    // Simpan session saat login sukses
    fun saveLoginSession(
        token: String,
        role: Int,
        hasPgsh: Boolean,
        isActive: Boolean = false,
        mobile: String = "",
        fullName: String = ""
    ) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_AUTH_TOKEN, token)
            putInt(KEY_USER_ROLE, role)
            putBoolean(KEY_PGSH, hasPgsh)
            putString(KEY_ACT, if (isActive) "1" else "")
            putString(KEY_MOBILE_NUMBER, mobile)
            putString(KEY_FULL_NAME, fullName)
            apply()
        }
    }

    // Update nama lengkap pengguna (misal setelah fetch/submit info)
    fun saveUserFullName(fullName: String) {
        sharedPreferences.edit().putString(KEY_FULL_NAME, fullName).apply()
    }

    // Update nomor telepon
    fun saveMobileNumber(mobile: String) {
        sharedPreferences.edit().putString(KEY_MOBILE_NUMBER, mobile).apply()
    }

    // Menggantikan fungsi hapus session / clear history saat Logout
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_PGSH = "pgsh"
        private const val KEY_MOBILE_NUMBER = "mobile_number"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_API_SERVICE = "api_service"
        private const val KEY_ACT = "act"
        private const val KEY_OLD_TOKEN = "oldTkn"
        private const val KEY_SAVED_PHONE_NUMBER = "savePhoneNumber"
    }
}
