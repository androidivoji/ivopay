package com.example.ivopay.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ivopay.app.ui.auth.SelectRoleScreen
import com.example.ivopay.app.ui.auth.SelectRoleViewModel
import com.example.ivopay.app.ui.components.FaceDetectionView
import com.example.ivopay.app.ui.loan.ApplyLoanScreen
import com.example.ivopay.app.ui.loan.ApplyLoanViewModel
import com.example.ivopay.app.ui.loan.ApplySucceedScreen
import com.example.ivopay.app.ui.loan.BorrowerSignContractsScreen
import com.example.ivopay.app.ui.loan.BorrowerSignContractsViewModel
import com.example.ivopay.app.ui.loan.OtherProductScreen
import com.example.ivopay.app.ui.loan.OtherProductViewModel
import com.example.ivopay.app.ui.login.GestureCreateScreen
import com.example.ivopay.app.ui.login.GestureCreateViewModel
import com.example.ivopay.app.ui.login.GestureLoginScreen
import com.example.ivopay.app.ui.login.GestureLoginViewModel
import com.example.ivopay.app.ui.login.LoginScreen
import com.example.ivopay.app.ui.login.LoginViewModel
import com.example.ivopay.app.ui.main.LenderMainDashboardScreen
import com.example.ivopay.app.ui.main.MainDashboardScreen
import com.example.ivopay.app.ui.main.MainTabItem
import com.example.ivopay.app.ui.mine.BaseInfoScreen
import com.example.ivopay.app.ui.mine.BaseInfoViewModel
import com.example.ivopay.app.ui.mine.ChangeBindPhoneScreen
import com.example.ivopay.app.ui.mine.ChangeBindPhoneViewModel
import com.example.ivopay.app.ui.mine.ContactInfoScreen
import com.example.ivopay.app.ui.mine.ContactInfoViewModel
import com.example.ivopay.app.ui.mine.JobInfoV2Screen
import com.example.ivopay.app.ui.mine.JobInfoV2ViewModel
import com.example.ivopay.app.ui.mine.LogoutAndExitScreen
import com.example.ivopay.app.ui.mine.MyProfileScreen
import com.example.ivopay.app.ui.mine.MyProfileViewModel
import com.example.ivopay.app.ui.mine.PersonalInfoScreen
import com.example.ivopay.app.ui.mine.PersonalInfoV2ViewModel
import com.example.ivopay.app.ui.splash.SplashScreen
import com.example.ivopay.app.ui.splash.SplashViewModel
import com.example.ivopay.app.util.SessionManager

object Screen {
    const val Splash = "splash_screen"
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
    const val GestureCreate = "GestureCreate"
    const val MyContracts = "my_contracts"
    const val BillDetails = "BillDetails"
    const val Repay = "RepayPage"
    const val BankInfo = "BankInfo"
    const val RegisterInfoWaiting = "RegisterInfoWaitingPage"
    const val UnderReview = "UnderReviewPage"
    const val A_Apply = "A_ApplyPage"
    const val OtherProduct = "OtherProductPage"
    const val FaceDetection = "FaceDetection"
    const val ChangeBindPhone = "ChangeBindPhone"
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
        // 0. Splash Screen
        composable(Screen.Splash) {
            val splashViewModel = remember { SplashViewModel(context) }
            SplashScreen(
                viewModel = splashViewModel,
                onNavigate = { state ->
                    navController.navigate(Screen.SelectRole) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }

        // 1. Screen Select Role
        composable(Screen.SelectRole) {
            val roleViewModel: SelectRoleViewModel = viewModel()
            
            SelectRoleScreen(
                isLoggedIn = sessionManager.isUserLoggedIn(),
                onUploadTrackingEvent = { },
                onNavigateToBorrowerMain = {
                    navController.navigate(Screen.Main) {
                        popUpTo(Screen.SelectRole) { inclusive = true }
                    }
                },
                onNavigateToLenderLogin = { isLender ->
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
                        onSuccess = { hasInm -> onFinished(hasInm) },
                        onError = { error -> android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show() }
                    )
                }
            )
        }

        // 2. Dashboard Utama
        composable(Screen.Main) {
            MainDashboardScreen(
                isLogin = sessionManager.isUserLoggedIn(),
                userName = sessionManager.getUserFullName(),
                userPhone = sessionManager.getMobileNumber(),
                onNavigateToDetail = { route -> navController.navigate(route) },
                onNavigateToLogin = { navController.navigate(Screen.Login) },
                onUploadTrackingEvent = { }
            )
        }

        composable(Screen.LenderMain) {
            LenderMainDashboardScreen(lenderStatus = 1, rootNavController = navController)
        }

        // 4. Login
        composable(
            route = "${Screen.Login}?role={role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "0" })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "0"
            val isLender = role == "1"
            val loginViewModel = remember { LoginViewModel(context).apply { setRole(isLender) } }
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
            route = "${Screen.GestureLogin}?phone={phone}&role={role}",
            arguments = listOf(navArgument("phone") { defaultValue = "" }, navArgument("role") { defaultValue = "0" })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "0"
            val gestureViewModel = remember { GestureLoginViewModel(context).apply { init(phone, role.toInt()) } } 
            GestureLoginScreen(
                viewModel = gestureViewModel,
                onNavigate = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.SelectRole) { inclusive = true }
                    }
                },
                onNavigateBackToLogin = { reset ->
                    if (reset) {
                        sessionManager.saveSavedPhoneNumber("")
                        navController.navigate(Screen.Login) { popUpTo(Screen.SelectRole) { inclusive = false } }
                    } else {
                        navController.navigate("${Screen.Login}?role=${gestureViewModel.userRole}")
                    }
                }
            )
        }

        composable(
            route = "${Screen.GestureCreate}?fromPage={fromPage}",
            arguments = listOf(navArgument("fromPage") { defaultValue = "" })
        ) { backStackEntry ->
            val gestureCreateViewModel: GestureCreateViewModel = viewModel { GestureCreateViewModel(context) }
            GestureCreateScreen(
                viewModel = gestureCreateViewModel,
                onBackClick = { navController.popBackStack() },
                onSuccess = { navController.navigate(Screen.Main) { popUpTo(0) { inclusive = true } } }
            )
        }

        // 5. Logout & Profile
        composable(Screen.LogoutAndExit) {
            LogoutAndExitScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAccountLogout = { navController.navigate(Screen.AccountLogout) },
                onLogoutConfirmed = {
                    sessionManager.clearSession()
                    navController.navigate(Screen.SelectRole) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.MyProfile) {
            val profileViewModel: MyProfileViewModel = viewModel { MyProfileViewModel(context) }
            MyProfileScreen(
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToStep = { stepRoute, _ -> navController.navigate(stepRoute) },
                onNavigateToLogin = { navController.navigate(Screen.Login) }
            )
        }

        // 6. KYC Info
        composable(route = "${Screen.BaseInfo}?infoFinished={infoFinished}", arguments = listOf(navArgument("infoFinished") { defaultValue = "" })) { backStackEntry ->
            val isNeedBack = backStackEntry.arguments?.getString("infoFinished") == "1"
            val baseInfoViewModel: BaseInfoViewModel = viewModel { BaseInfoViewModel(context) }
            BaseInfoScreen(viewModel = baseInfoViewModel, onBackClick = { navController.popBackStack() }, onNextClick = { if (isNeedBack) navController.popBackStack() else navController.navigate(Screen.PersonalInfoV2) })
        }

        composable(route = "${Screen.PersonalInfoV2}?infoFinished={infoFinished}", arguments = listOf(navArgument("infoFinished") { defaultValue = "" })) { backStackEntry ->
            val isNeedBack = backStackEntry.arguments?.getString("infoFinished") == "1"
            val personalViewModel: PersonalInfoV2ViewModel = viewModel { PersonalInfoV2ViewModel(context) }
            PersonalInfoScreen(viewModel = personalViewModel, onBackClick = { navController.popBackStack() }, onNextClick = { if (isNeedBack) navController.popBackStack() else navController.navigate(Screen.ContactInfo) })
        }

        composable(route = "${Screen.ContactInfo}?infoFinished={infoFinished}", arguments = listOf(navArgument("infoFinished") { defaultValue = "" })) { backStackEntry ->
            val isNeedBack = backStackEntry.arguments?.getString("infoFinished") == "1"
            val contactViewModel: ContactInfoViewModel = viewModel { ContactInfoViewModel(context) }
            ContactInfoScreen(viewModel = contactViewModel, onBackClick = { navController.popBackStack() }, onNextClick = { if (isNeedBack) navController.popBackStack() else navController.navigate(Screen.JobInfoV2) })
        }

        composable(route = "${Screen.JobInfoV2}?infoFinished={infoFinished}", arguments = listOf(navArgument("infoFinished") { defaultValue = "" })) { backStackEntry ->
            val isNeedBack = backStackEntry.arguments?.getString("infoFinished") == "1"
            val jobViewModel: JobInfoV2ViewModel = viewModel { JobInfoV2ViewModel(context) }
            JobInfoV2Screen(
                viewModel = jobViewModel,
                onBackClick = { navController.popBackStack() },
                onNextClick = { cme, fcoa, tnpo ->
                    if (isNeedBack) navController.popBackStack()
                    else if (cme?.uico == true) {
                        if (cme.wof == false) {
                            if (fcoa?.get("psw")?.asInt == 1) navController.navigate(Screen.ApplyLoan) 
                            else navController.navigate(Screen.Main)
                        } else navController.navigate(Screen.ApplyLoan)
                    } else navController.navigate(Screen.MyProfile)
                }
            )
        }

        // 7. Apply Loan Flow
        composable(Screen.ApplyLoan) {
            // Share ViewModel with FaceDetection via parent entry
            val applyViewModel: ApplyLoanViewModel = viewModel {
                ApplyLoanViewModel(context)
            }
            ApplyLoanScreen(
                viewModel = applyViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
                onSubmitSuccess = { noc ->
                    navController.navigate("ApplySucceedPage?noc=$noc&showPop=1") { popUpTo(Screen.Main) { inclusive = false } }
                }
            )
        }

        composable(Screen.FaceDetection) {
            // Gunakan remember untuk mengambil BackStackEntry agar aman dari recomposition
            val applyEntry = remember(it) { navController.getBackStackEntry(Screen.ApplyLoan) }
            val applyViewModel: ApplyLoanViewModel = viewModel(
                viewModelStoreOwner = applyEntry
            )

            FaceDetectionView(
                onImageCaptured = { bitmap ->
                    applyViewModel.handleFaceDetectResult(bitmap)
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = "ApplySucceedPage?noc={noc}&cash_type={cash_type}&mob={mob}&need_confirm={need_confirm}&showPop={showPop}",
            arguments = listOf(
                navArgument("noc") { defaultValue = "" },
                navArgument("cash_type") { defaultValue = "" },
                navArgument("mob") { defaultValue = "" },
                navArgument("need_confirm") { defaultValue = "0" },
                navArgument("showPop") { defaultValue = "0" }
            )
        ) { backStackEntry ->
            val noc = backStackEntry.arguments?.getString("noc") ?: ""
            val cashType = backStackEntry.arguments?.getString("cash_type") ?: ""
            val mob = backStackEntry.arguments?.getString("mob") ?: ""
            val needConfirm = backStackEntry.arguments?.getString("need_confirm") == "1"
            val showInitialPop = backStackEntry.arguments?.getString("showPop") == "1"

            ApplySucceedScreen(
                ocEui = sessionManager.getActStatus() == "1",
                cashType = cashType,
                needConfirm = needConfirm,
                mob = mob,
                noc = noc,
                showInitialRatePopup = showInitialPop,
                onNavigateHome = { navController.navigate(Screen.Main) { popUpTo(0) { inclusive = true } } },
                onNavigateJmo = { navController.navigate("JMOPage") { popUpTo(0) { inclusive = true } } },
                onNavigateAppStore = { },
                onNavigateBpjsDetail = { },
                onConfirmInsurance = { }
            )
        }

        composable(Screen.OtherProduct) {
            val otherProductViewModel: OtherProductViewModel = viewModel {
                OtherProductViewModel(context)
            }
            OtherProductScreen(viewModel = otherProductViewModel, onBackClick = { navController.popBackStack() })
        }

        composable(Screen.ChangeBindPhone) {
            val changePhoneViewModel = viewModel {
                ChangeBindPhoneViewModel(context)
            }
            ChangeBindPhoneScreen(
                viewModel = changePhoneViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToBill = { navController.navigate(MainTabItem.Bill.route) },
                onNavigateHome = {
                    navController.navigate(Screen.Main) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Screen.BillDetails}?bill={bill}",
            arguments = listOf(navArgument("bill") { type = NavType.StringType })
        ) { backStackEntry ->
            val noc = backStackEntry.arguments?.getString("bill") ?: ""
            val billViewModel = remember { com.example.ivopay.app.ui.bill.BillDetailsViewModel(context) }
            com.example.ivopay.app.ui.bill.BillDetailsScreen(
                noc = noc,
                viewModel = billViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(
            route = "${Screen.Repay}?bill={bill}&pre_pay={pre_pay}&cur_pay={cur_pay}",
            arguments = listOf(
                navArgument("bill") { type = NavType.StringType },
                navArgument("pre_pay") { defaultValue = "0" },
                navArgument("cur_pay") { defaultValue = "0" }
            )
        ) { backStackEntry ->
            val billJson = backStackEntry.arguments?.getString("bill") ?: ""
            val prePay = backStackEntry.arguments?.getString("pre_pay") == "1"
            val curPay = backStackEntry.arguments?.getString("cur_pay") == "1"
            val repayViewModel = remember { com.example.ivopay.app.ui.repay.RepayViewModel(context) }
            
            com.example.ivopay.app.ui.repay.RepayScreen(
                billJson = billJson,
                isPrePay = prePay,
                isCurPay = curPay,
                viewModel = repayViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateBcaGuide = { }
            )
        }

        composable(
            route = "BorrowerSignContracts?noc={noc}&wiue={wiue}",
            arguments = listOf(
                navArgument("noc") { defaultValue = "" },
                navArgument("wiue") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            )
        ) { backStackEntry ->
            val noc = backStackEntry.arguments?.getString("noc") ?: ""
            val isWiue = backStackEntry.arguments?.getBoolean("wiue") ?: false
            val signViewModel: BorrowerSignContractsViewModel = viewModel()
            
            BorrowerSignContractsScreen(
                noc = noc,
                isWiue = isWiue,
                viewModel = signViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.AccountLogout) {
            val logoutViewModel: com.example.ivopay.app.ui.mine.AccountLogoutViewModel = viewModel {
                com.example.ivopay.app.ui.mine.AccountLogoutViewModel(context)
            }
            com.example.ivopay.app.ui.mine.AccountLogoutScreen(
                viewModel = logoutViewModel,
                onBackClick = { navController.popBackStack() },
                onLogoutSuccess = {
                    navController.navigate(Screen.SelectRole) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProduct = { type, rasn ->
                    // Navigasi ke produk cicilan sesuai rtin_pudtyp di Vue
                    when (type) {
                        5 -> navController.navigate("InlgCash?rasn=$rasn")
                        6 -> navController.navigate("Ci6Cash?rasn=$rasn")
                        7 -> navController.navigate("Ci7Cash?rasn=$rasn")
                        0 -> navController.navigate(Screen.ApplyLoan)
                        else -> navController.navigate(Screen.Main)
                    }
                }
            )
        }
    }
}
