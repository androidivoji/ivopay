package com.example.ivopay.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.ivopay.R

@Composable
fun SelectRoleScreen(
    isLoggedIn: Boolean = false,
    onUploadTrackingEvent: (String) -> Unit = {},
    onNavigateToBorrowerMain: () -> Unit = {},
    onNavigateToLenderLogin: (isLender: Boolean) -> Unit = {},
    onNavigateToLenderBasicInfo: () -> Unit = {},
    onNavigateToLenderMain: () -> Unit = {},
    onFetchLenderUserInfo: (onSuccess: (hasInm: Boolean) -> Unit) -> Unit = {}
) {
    // Fungsi handler untuk klik Lender (onJumpLender)
    val handleJumpLender = {
        if (isLoggedIn) {
            onFetchLenderUserInfo { hasInm ->
                // Jika user info tidak memiliki inm (!su.pi.inm), pindah ke LenderBasicInfo
                if (!hasInm) {
                    // Panggil navigasi ke 'LenderBasicInfo'
                    onNavigateToLenderBasicInfo()
                } else {
                    onNavigateToLenderMain()
                }
            }
        } else {
            // Pindah ke PhoneLogin dengan role = '1'
            onNavigateToLenderLogin(true)
        }
    }

    // Fungsi handler untuk klik Borrower (onJumpBorrower)
    val handleJumpBorrower = {
        onUploadTrackingEvent("N1")
        if (isLoggedIn) {
            onNavigateToBorrowerMain()
        } else {
            // Jika Borrower belum login dan ingin login dulu
            onNavigateToLenderLogin(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Banner Role Lender (iv_splash_screen1)
        Image(
            painter = painterResource(id = R.drawable.iv_splash_screen1),
            contentDescription = "Pilih Role Lender",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { handleJumpLender() }
        )

        // Banner Role Borrower (iv_splash_screen2)
        Image(
            painter = painterResource(id = R.drawable.iv_splash_screen2),
            contentDescription = "Pilih Role Borrower",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { handleJumpBorrower() }
        )
    }
}