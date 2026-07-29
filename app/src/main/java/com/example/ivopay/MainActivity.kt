package com.example.ivopay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.blankj.utilcode.util.Utils
import com.example.ivopay.app.ui.navigation.AppNavigation
import com.example.ivopay.app.ui.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.init(application)

        // Menangkap data rute yang dikirim oleh SplashActivity (Default: Screen.SelectRole)
        val targetRoute = intent.getStringExtra("TARGET_ROUTE") ?: Screen.SelectRole

        setContent {
            val navController = rememberNavController()

            AppNavigation(
                navController = navController,
                startDestination = targetRoute
            )
        }
    }
}