package com.example.ivopay.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.ivopay.app.data.network.SessionApiService
import com.example.ivopay.app.ui.auth.SelectRoleScreen
import com.example.ivopay.app.ui.main.LenderMainDashboardScreen
import com.example.ivopay.app.util.SessionManager

object Screen {
    const val Splash = "SplashPage"
    const val SelectRole = "select_role"
    const val PhoneLogin = "login_screen"

    // Borrower Routes
    const val Main = "main"
    const val Home = "home"
    const val MyBill = "MyBill"
    const val Mine = "mine"

    // Lender Routes
    const val LenderMain = "l_main"
    const val LenderBasicInfo = "lender_basic_info"

    // Additional Settings & Legal Routes
    const val MyContracts = "my_contracts"
    const val AboutUs = "about_us"
    const val PrivacyPolicy = "privacy_policy"
    const val UseAgreement = "use_agreement"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    token: String?,
    sessionId: String?,
    apiService: SessionApiService
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {

        // 1. Splash Page Route
        composable(route = Screen.Splash) {
            TrackPageLifecycle(Screen.Splash, token, sessionId, apiService)
            // Dipanggil melalui SplashActivity terpisah, atau jika ditempatkan di sini
        }

        // 2. Select Role Page Route
        composable(route = Screen.SelectRole) {
            TrackPageLifecycle(Screen.SelectRole, token, sessionId, apiService)

            SelectRoleScreen(
                isLoggedIn = sessionManager.isUserLoggedIn(),
                onUploadTrackingEvent = { event ->
                    // Tracking event
                },
                onNavigateToBorrowerMain = {
                    navController.navigate(Screen.Main) {
                        popUpTo(Screen.SelectRole) { inclusive = true }
                    }
                },
                onNavigateToLenderLogin = {
                    navController.navigate("${Screen.PhoneLogin}?role=1")
                },
                onNavigateToLenderBasicInfo = {
                    navController.navigate(Screen.LenderBasicInfo)
                },
                onNavigateToLenderMain = {
                    navController.navigate(Screen.LenderMain) {
                        popUpTo(Screen.SelectRole) { inclusive = true }
                    }
                },
                onFetchLenderUserInfo = { onFinished ->
                    // Contoh pengecekan status inm dari data session/local
                    val hasInm = sessionManager.getUserFullName()?.isNotEmpty() == true
                    onFinished(hasInm)
                }
            )
        }

        // 3. Phone Login Route
        composable(route = Screen.PhoneLogin) {
            TrackPageLifecycle(Screen.PhoneLogin, token, sessionId, apiService)
            // PhoneLoginScreen(navController = navController)
        }

        // 4. Main Tab Navigation (Borrower Dashboard)
        navigation(
            startDestination = Screen.Home,
            route = Screen.Main
        ) {
            composable(route = Screen.Home) {
                TrackPageLifecycle(Screen.Home, token, sessionId, apiService)
                // BorrowerHomeScreen()
            }
            composable(route = Screen.MyBill) {
                TrackPageLifecycle(Screen.MyBill, token, sessionId, apiService)
                // BorrowerMyBillScreen()
            }
            composable(route = Screen.Mine) {
                TrackPageLifecycle(Screen.Mine, token, sessionId, apiService)
                // BorrowerMineScreen()
            }
        }

        // 5. Lender Dashboard Main Route (Memuat BottomBar Home, Portfolio, & Settings)
        composable(route = Screen.LenderMain) {
            TrackPageLifecycle(Screen.LenderMain, token, sessionId, apiService)

            LenderMainDashboardScreen(
                rootNavController = navController,
                lenderStatus = 1
            )
        }

        // 6. Lender Basic Info Route
        composable(route = Screen.LenderBasicInfo) {
            TrackPageLifecycle(Screen.LenderBasicInfo, token, sessionId, apiService)
            // LenderBasicInfoScreen(navController = navController)
        }

        // 7. Sub-Routes dari Menu Settings Lender
        composable(route = Screen.MyContracts) {
            TrackPageLifecycle(Screen.MyContracts, token, sessionId, apiService)
            // MyContractsScreen()
        }
        composable(route = Screen.AboutUs) {
            TrackPageLifecycle(Screen.AboutUs, token, sessionId, apiService)
            // AboutUsScreen()
        }
        composable(route = Screen.PrivacyPolicy) {
            TrackPageLifecycle(Screen.PrivacyPolicy, token, sessionId, apiService)
            // PrivacyPolicyScreen()
        }
        composable(route = Screen.UseAgreement) {
            TrackPageLifecycle(Screen.UseAgreement, token, sessionId, apiService)
            // UseAgreementScreen()
        }
    }
}