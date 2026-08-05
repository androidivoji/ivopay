package com.example.ivopay.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.blankj.utilcode.util.Utils
import com.example.ivopay.MainActivity
import com.example.ivopay.app.ui.navigation.Screen
import com.example.ivopay.app.ui.splash.SplashNavigationState
import com.example.ivopay.app.ui.splash.SplashScreen
import com.example.ivopay.app.ui.splash.SplashViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.init(application)

        val viewModel = SplashViewModel(applicationContext)

        setContent {
            SplashScreen(viewModel = viewModel) { targetState ->
                // Membuat intent untuk berpindah ke MainActivity
                val intent = Intent(this@SplashActivity, MainActivity::class.java)

                val route = when (targetState) {
                    SplashNavigationState.GoToLMain -> Screen.LenderMain
                    SplashNavigationState.GoToMain -> Screen.Main
                    SplashNavigationState.GoToSelectRole -> Screen.SelectRole
                    else -> {
                        "select_role" // Gunakan fallback aman, bukan dikosongkan
                    }
                }
                intent.putExtra("TARGET_ROUTE", route)
                Log.d("XBZ", "Intent extra TARGET_ROUTE set to: $route")

                startActivity(intent)
                finish() // Menutup SplashActivity agar tidak bisa di-back
            }
        }
    }
}