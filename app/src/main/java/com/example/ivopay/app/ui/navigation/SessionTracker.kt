package com.example.ivopay.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ivopay.app.data.network.PageEndRequest
import com.example.ivopay.app.data.network.PageStartRequest
import com.example.ivopay.app.data.network.SessionApiService
import kotlinx.coroutines.launch

@Composable
fun TrackPageLifecycle(
    pageName: String,
    token: String?,
    sessionId: String?,
    apiService: SessionApiService
) {
    val coroutineScope = rememberCoroutineScope()

    // Daftar halaman aktif yang ingin di-track sesuai array pagesName di Vue kamu
    val trackedPages = listOf("SplashPage", "home", "mine", "PhoneLogin")

    // Pengondisian: Hanya track jika halaman masuk daftar dan Token/Session tersedia
    if (!trackedPages.contains(pageName) || token.isNullOrBlank() || sessionId.isNullOrBlank()) return

    DisposableEffect(pageName) {
        val startPageTime = System.currentTimeMillis()

        // Memicu aksi mirip router.beforeEach -> PAGE_START
        coroutineScope.launch {
            try {
                apiService.sendPageStart(
                    PageStartRequest(tkn = token, session_id = sessionId, page_name = pageName)
                )
            } catch (e: Exception) {
                // Di-catch secara silent seperti .catch() di Vue
                e.printStackTrace()
            }
        }

        // Memicu aksi mirip router.beforeEach -> PAGE_END saat komponen hancur/pindah route
        onDispose {
            val currentTime = System.currentTimeMillis()
            val stayTimeSeconds = ((currentTime - startPageTime) / 1000).toInt()
            val finalStayTime = if (stayTimeSeconds > 0) stayTimeSeconds else 1

            coroutineScope.launch {
                try {
                    apiService.sendPageEnd(
                        PageEndRequest(
                            tkn = token,
                            session_id = sessionId,
                            page_name = pageName,
                            page_stay_seconds = finalStayTime,
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}