package com.example.ivopay.app.ui.splash

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mengadopsi warna dari CSS Vue kamu (#BD0100)
val BrandRed = Color(0xFFBD0100)

@Composable
fun SplashScreen(viewModel: SplashViewModel, onNavigate: (SplashNavigationState) -> Unit) {
    val navState by viewModel.navigationState.collectAsState()

    // Triger pencarian rute saat pertama kali layar dimuat (mounted)
    LaunchedEffect(Unit) {
        Log.d("XBZ", "SplashScreen Composable: LaunchedEffect Unit dipanggil")
        viewModel.judgeAndJump()
    }

    // Mengamati perubahan state untuk pindah halaman
    LaunchedEffect(navState) {
        Log.d("XBZ", "SplashScreen Composable: navState berubah menjadi -> $navState")
        when (navState) {
            is SplashNavigationState.GoToLMain,
            is SplashNavigationState.GoToMain,
            is SplashNavigationState.GoToSelectRole -> {
                onNavigate(navState)
            }
            else -> { /* Loading atau Error: Jangan lakukan navigasi */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when (navState) {
            is SplashNavigationState.Loading,
            is SplashNavigationState.GoToLMain,
            is SplashNavigationState.GoToMain,
            is SplashNavigationState.GoToSelectRole -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Komponen animasi loader kotak berputar khas Vue kamu
                    InfiniteRotatingLoader()

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Loading...",
                        color = BrandRed,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            is SplashNavigationState.Error -> {
                // Tampilan Error ketika jaringan bermasalah (error-con)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Network Error!",
                        color = BrandRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button reload pengganti van-button plain
                    Button(
                        onClick = { viewModel.judgeAndJump() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(50) // Membuat bundar (round)
                    ) {
                        Text(text = "Reloading", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InfiniteRotatingLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")

    // Animasi rotasi terbalik dari 0 ke -360 derajat (rotationBack 1s infinite reverse)
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .rotate(angle),
        contentAlignment = Alignment.Center
    ) {
        // Kotak Merah Utama (.loader)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandRed)
        )
        // Efek Belah Ketupat Merah (.loader::before)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(45f)
                .background(BrandRed)
        )
        // Lingkaran Putih di Tengah (.loader::after)
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.White, shape = RoundedCornerShape(50))
        )
    }
}