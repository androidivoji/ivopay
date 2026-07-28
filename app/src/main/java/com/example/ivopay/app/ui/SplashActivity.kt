package com.example.ivopay.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent // Sesuaikan nama activity utama kamu
import com.example.ivopay.MainActivity
import com.example.ivopay.app.ui.splash.SplashNavigationState
import com.example.ivopay.app.ui.splash.SplashScreen
import com.example.ivopay.app.ui.splash.SplashViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("XBZ", "SplashActivity onCreate dipanggil!")

        val viewModel = SplashViewModel(applicationContext)

        setContent {
            SplashScreen(viewModel = viewModel) { targetState ->
                // Membuat intent untuk berpindah ke MainActivity
                val intent = Intent(this@SplashActivity, MainActivity::class.java)

                when (targetState) {
                    SplashNavigationState.GoToLMain -> {
                        intent.putExtra("TARGET_ROUTE", "l_main")
                    }
                    SplashNavigationState.GoToMain -> {
                        intent.putExtra("TARGET_ROUTE", "main")
                    }
                    SplashNavigationState.GoToSelectRole -> {
                        intent.putExtra("TARGET_ROUTE", "select_role")
                    }
                    else -> {}
                }

                startActivity(intent)
                finish() // Menutup SplashActivity agar tidak bisa di-back
            }
        }
    }
}