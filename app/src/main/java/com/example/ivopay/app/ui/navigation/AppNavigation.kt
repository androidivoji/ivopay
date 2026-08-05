package com.example.ivopay.app.ui.navigation

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ivopay.app.ui.auth.SelectRoleScreen
import com.example.ivopay.app.ui.auth.SelectRoleViewModel
import com.example.ivopay.app.ui.lender.borrower.BorrowerDetailScreen
import com.example.ivopay.app.ui.lender.detail.AlreadyPaidBillDetailScreen
import com.example.ivopay.app.ui.lender.detail.ChooseContractsScreen
import com.example.ivopay.app.ui.lender.detail.ViewContractPageScreen
import com.example.ivopay.app.ui.lender.portofolio.toberecharged.ToBeRechargedDetailScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsign.BorrowerSignContractsScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsign.PlatformSignContractsScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsign.WaitSignContractsScreen
import com.example.ivopay.app.ui.loan.ApplyLoanScreen
import com.example.ivopay.app.ui.loan.ApplySucceedScreen
import com.example.ivopay.app.ui.login.GestureLoginScreen
import com.example.ivopay.app.ui.login.GestureLoginViewModel
import com.example.ivopay.app.ui.login.LoginScreen
import com.example.ivopay.app.ui.login.LoginViewModel
import com.example.ivopay.app.ui.main.LenderMainDashboardScreen
import com.example.ivopay.app.ui.main.MainDashboardScreen
import com.example.ivopay.app.ui.mine.BaseInfoScreen
import com.example.ivopay.app.ui.mine.ContactInfoScreen
import com.example.ivopay.app.ui.mine.ContactInfoV2Screen
import com.example.ivopay.app.ui.mine.JobInfoV2Screen
import com.example.ivopay.app.ui.mine.LenderBasicInfoScreen
import com.example.ivopay.app.ui.mine.LogoutAndExitScreen
import com.example.ivopay.app.ui.mine.MyProfileScreen
import com.example.ivopay.app.ui.mine.PersonalInfoScreen
import com.example.ivopay.app.util.SessionManager

object Screen {
    const val SelectRole = "select_role"
    const val Main = "main"
    const val LenderMain = "l_main"
    const val Login = "login_screen"
    const val LogoutAndExit = "LogoutAndExitPage"
    const val MyProfile = "MyProfile"
    const val BaseInfo = "BaseInfo"
    const val PersonalInfoV2 = "PersonalInfoV2"
    const val ContactInfo = "ContactInfoPage"
    const val ContactInfoV2 = "ContactInfoV2Page"
    const val JobInfoV2 = "JobInfoV2"
    const val ApplyLoan = "ApplyLoan"
    const val AccountLogout = "AccountLogoutPage"
    const val LenderBasicInfo = "lender_basic_info"
    const val GestureLogin = "GestureLogin"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 0. Screen Select Role (Pilih Borrower / Lender)
        composable(Screen.SelectRole) {
            val roleViewModel: SelectRoleViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            
            SelectRoleScreen(
                isLoggedIn = sessionManager.isUserLoggedIn(),
                onUploadTrackingEvent = { /* Logika tracking */ },
                onNavigateToBorrowerMain = {
                    navController.navigate(Screen.Main) {
                        popUpTo(Screen.SelectRole) { inclusive = true }
                    }
                },
                onNavigateToLenderLogin = { isLender ->
                    // Membawa parameter role=1 jika isLender == true
                    val roleParam = if (isLender) "1" else "0"
                    navController.navigate("${Screen.Login}?role=$roleParam")
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
                    roleViewModel.fetchLenderUserInfo(
                        onSuccess = { hasInm ->
                            onFinished(hasInm)
                        },
                        onError = { error ->
                            // Handle error, misal tampilkan toast
                            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }

        // 1. Dashboard Utama Peminjam (Main)
        composable(Screen.Main) {
            MainDashboardScreen(
                isLogin = sessionManager.isUserLoggedIn(),
                userName = sessionManager.getUserFullName(),
                userPhone = sessionManager.getMobileNumber(),
                onNavigateToDetail = { route -> navController.navigate(route) },
                onNavigateToLogin = { navController.navigate(Screen.Login) },
                onUploadTrackingEvent = { /* Tracking event */ }
            )
        }

        // 2. Dashboard Lender / Mitra (l_main)
        composable(Screen.LenderMain) {
            LenderMainDashboardScreen(
                lenderStatus = 1,
                rootNavController = navController
            )
        }

        // 3. Screen Login (Mendukung query parameter ?role=1)
        composable(
            route = "${Screen.Login}?role={role}",
            arguments = listOf(
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = "0"
                }
            )
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "0"
            val isLender = role == "1"

            val loginViewModel = remember {
                LoginViewModel(context).apply {
                    setRole(isLender)
                }
            }

            LoginScreen(
                viewModel = loginViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigate = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.SelectRole) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Screen.GestureLogin}?phone={phone}",
            arguments = listOf(navArgument("phone") { defaultValue = "" })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val gestureViewModel = remember { GestureLoginViewModel(context).apply { init(phone) } }
            
            GestureLoginScreen(
                viewModel = gestureViewModel,
                onNavigate = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.SelectRole) { inclusive = true }
                    }
                },
                onNavigateBackToLogin = { reset ->
                    if (reset) {
                        sessionManager.saveSavedPhoneNumber("") // Clear saved phone
                        navController.navigate(Screen.Login) {
                            popUpTo(Screen.SelectRole) { inclusive = false }
                        }
                    } else {
                        // Phone Login with send_code=1 logic can be added here
                        navController.navigate("${Screen.Login}?role=${if(sessionManager.getUserRole()==1) "1" else "0"}")
                    }
                }
            )
        }

        // 4. Screen Logout dan Profile
        composable(Screen.LogoutAndExit) {
            LogoutAndExitScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAccountLogout = { navController.navigate(Screen.AccountLogout) },
                onLogoutConfirmed = {
                    sessionManager.clearSession()
                    navController.navigate(Screen.SelectRole) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MyProfile) {
            MyProfileScreen(
                isLoggedIn = sessionManager.isUserLoggedIn(),
                onBackClick = { navController.popBackStack() },
                onNavigateToStep = { stepRoute, isFinished ->
                    navController.navigate("$stepRoute?infoFinished=$isFinished")
                },
                onNavigateToLogin = { navController.navigate(Screen.Login) }
            )
        }

        // 5. Alur Informasi Pengguna (KYC / Onboarding)
        composable(Screen.BaseInfo) {
            BaseInfoScreen(
                onBackClick = { navController.popBackStack() },
                onNextClick = { navController.navigate(Screen.PersonalInfoV2) },
                onSelectKtpPhoto = { /* Panggil Intent kamera */ },
                onOpenTermsAndConditions = { navController.navigate("TermsAndConditionsPage") }
            )
        }

        composable(Screen.PersonalInfoV2) {
            PersonalInfoScreen(
                onBackClick = { navController.popBackStack() },
                onNextClick = { navController.navigate(Screen.ContactInfo) }
            )
        }

        composable(Screen.ContactInfo) {
            ContactInfoScreen(
                onBackClick = { navController.popBackStack() },
                onNextClick = { navController.navigate(Screen.JobInfoV2) }
            )
        }

        composable(Screen.ContactInfoV2) {
            ContactInfoV2Screen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { _, _ -> navController.navigate("BankInfo") }
            )
        }

        composable(Screen.JobInfoV2) {
            JobInfoV2Screen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { navController.navigate(Screen.Main) },
                onTakeWorkProofPhoto = { _, onPhotoCaptured ->
                    onPhotoCaptured("path/to/work_proof_image.jpg")
                }
            )
        }

        // 6. Alur Pinjaman (Loan)
        composable(Screen.ApplyLoan) {
            ApplyLoanScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.navigate("ApplySucceedPage") {
                        popUpTo(Screen.Main) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = "ApplySucceedPage?noc={noc}&cash_type={cash_type}&mob={mob}&need_confirm={need_confirm}",
            arguments = listOf(
                navArgument("noc") { defaultValue = "" },
                navArgument("cash_type") { defaultValue = "" },
                navArgument("mob") { defaultValue = "" },
                navArgument("need_confirm") { defaultValue = "0" }
            )
        ) { backStackEntry ->
            val noc = backStackEntry.arguments?.getString("noc") ?: ""
            val cashType = backStackEntry.arguments?.getString("cash_type") ?: ""
            val mob = backStackEntry.arguments?.getString("mob") ?: ""
            val needConfirmStr = backStackEntry.arguments?.getString("need_confirm") ?: "0"
            val needConfirm = needConfirmStr == "1"

            val prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE)
            val showRatePop = !prefs.getBoolean("showRatePop", false)
            if (showRatePop) {
                prefs.edit().putBoolean("showRatePop", true).apply()
            }

            ApplySucceedScreen(
                ocEui = false,
                cashType = cashType,
                needConfirm = needConfirm,
                mob = mob,
                noc = noc,
                showInitialRatePopup = showRatePop,
                onNavigateHome = {
                    navController.navigate(Screen.Main) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateJmo = {
                    navController.navigate("JMOPage") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateAppStore = { appId ->
                    val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$appId".toUri())
                    context.startActivity(intent)
                },
                onNavigateBpjsDetail = {
                    navController.navigate("CommonHtmlPage?link=static/img/bpjs_insurance.html")
                },
                onConfirmInsurance = { /* API Call */ }
            )
        }

        composable(Screen.AccountLogout) {
            // AccountLogoutScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.LenderBasicInfo) {
            LenderBasicInfoScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.navigate(Screen.LenderMain) {
                        popUpTo(Screen.SelectRole) { inclusive = true }
                    }
                },
                onSelectPhoto = { _, _ -> }
            )
        }

        // 7. Alur Kontrak Lender
        composable(
            route = "sign_contracts/{odi}",
            arguments = listOf(navArgument("odi") { type = NavType.StringType })
        ) { backStackEntry ->
            val odi = backStackEntry.arguments?.getString("odi") ?: ""
            WaitSignContractsScreen(
                odi = odi,
                onBackClick = { navController.popBackStack() },
                onNavigateToBorrowerSign = { mdi ->
                    navController.navigate("borrower_sign_contracts/$mdi")
                },
                onNavigateToPlatformSign = { mdi ->
                    navController.navigate("platform_sign_contracts/$mdi")
                }
            )
        }

        composable(
            route = "borrower_sign_contracts/{mdi}",
            arguments = listOf(navArgument("mdi") { type = NavType.StringType })
        ) { backStackEntry ->
            val mdi = backStackEntry.arguments?.getString("mdi") ?: ""
            BorrowerSignContractsScreen(
                mdi = mdi,
                onBackClick = { navController.popBackStack() },
                onNavigateToPlatformSign = { targetMdi ->
                    navController.navigate("platform_sign_contracts/$targetMdi") {
                        popUpTo("borrower_sign_contracts/{mdi}") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "platform_sign_contracts/{mdi}",
            arguments = listOf(navArgument("mdi") { type = NavType.StringType })
        ) { backStackEntry ->
            val mdi = backStackEntry.arguments?.getString("mdi") ?: ""
            PlatformSignContractsScreen(
                mdi = mdi,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "borrow_contracts_list/{odi}",
            arguments = listOf(navArgument("odi") { type = NavType.StringType })
        ) { backStackEntry ->
            val odi = backStackEntry.arguments?.getString("odi") ?: ""
            ToBeRechargedDetailScreen(
                odi = odi,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "borrower_detail/{ati}",
            arguments = listOf(navArgument("ati") { type = NavType.StringType })
        ) { backStackEntry ->
            val ati = backStackEntry.arguments?.getString("ati") ?: ""
            BorrowerDetailScreen(
                ati = ati,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "already_paid_bill_detail/{odi}",
            arguments = listOf(navArgument("odi") { type = NavType.StringType })
        ) { backStackEntry ->
            val odi = backStackEntry.arguments?.getString("odi") ?: ""
            AlreadyPaidBillDetailScreen(
                odi = odi,
                onBackClick = { navController.popBackStack() },
                onNavigateToChooseContracts = { mdi ->
                    navController.navigate("choose_contracts?mdi=$mdi")
                }
            )
        }

        composable(
            route = "choose_contracts?mdi={mdi}&cno={cno}",
            arguments = listOf(
                navArgument("mdi") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("cno") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val mdi = backStackEntry.arguments?.getString("mdi")
            val cno = backStackEntry.arguments?.getString("cno")

            ChooseContractsScreen(
                mdi = mdi,
                cno = cno,
                onBackClick = { navController.popBackStack() },
                onNavigateToViewContractsPage = { mdiParam, type ->
                    navController.navigate("view_contracts_page/$mdiParam/$type")
                },
                onNavigateToViewContractsPage2 = { cnoParam, type ->
                    navController.navigate("view_contracts_page2/$cnoParam/$type")
                }
            )
        }

        composable(
            route = "view_contracts_page/{mdi}/{type}",
            arguments = listOf(
                navArgument("mdi") { type = NavType.StringType },
                navArgument("type") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val mdi = backStackEntry.arguments?.getString("mdi") ?: ""
            val type = backStackEntry.arguments?.getInt("type") ?: 1

            ViewContractPageScreen(
                mdi = mdi,
                type = type,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}