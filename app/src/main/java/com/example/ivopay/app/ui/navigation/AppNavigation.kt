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
import com.example.ivopay.app.ui.lender.mycontracts.MyContractsScreen
import com.example.ivopay.app.ui.lender.portofolio.toberecharged.ToBeRechargedDetailScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsign.BorrowerSignContractsScreen as LenderBorrowerSignContractsScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsign.PlatformSignContractsScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsign.WaitSignContractsScreen
import com.example.ivopay.app.ui.loan.ApplyLoanScreen
import com.example.ivopay.app.ui.loan.ApplySucceedScreen
import com.example.ivopay.app.ui.loan.BorrowerSignContractsScreen
import com.example.ivopay.app.ui.loan.BorrowerSignContractsViewModel
import com.example.ivopay.app.ui.login.GestureCreateScreen
import com.example.ivopay.app.ui.login.GestureCreateViewModel
import com.example.ivopay.app.ui.login.GestureLoginScreen
import com.example.ivopay.app.ui.login.GestureLoginViewModel
import com.example.ivopay.app.ui.login.LoginScreen
import com.example.ivopay.app.ui.login.LoginViewModel
import com.example.ivopay.app.ui.main.LenderMainDashboardScreen
import com.example.ivopay.app.ui.main.MainDashboardScreen
import com.example.ivopay.app.ui.mine.BaseInfoScreen
import com.example.ivopay.app.ui.mine.BaseInfoViewModel
import com.example.ivopay.app.ui.mine.ContactInfoScreen
import com.example.ivopay.app.ui.mine.ContactInfoViewModel
import com.example.ivopay.app.ui.mine.JobInfoV2Screen
import com.example.ivopay.app.ui.mine.JobInfoV2ViewModel
import com.example.ivopay.app.ui.mine.LenderBasicInfoScreen
import com.example.ivopay.app.ui.mine.LenderBasicInfoViewModel
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
            val roleViewModel: SelectRoleViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            
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
            val gestureCreateViewModel: GestureCreateViewModel = androidx.lifecycle.viewmodel.compose.viewModel { GestureCreateViewModel(context) }
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
            val profileViewModel: MyProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel { MyProfileViewModel(context) }
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
            val baseInfoViewModel: BaseInfoViewModel = androidx.lifecycle.viewmodel.compose.viewModel { BaseInfoViewModel(context) }
            BaseInfoScreen(viewModel = baseInfoViewModel, onBackClick = { navController.popBackStack() }, onNextClick = { if (isNeedBack) navController.popBackStack() else navController.navigate(Screen.PersonalInfoV2) })
        }

        composable(route = "${Screen.PersonalInfoV2}?infoFinished={infoFinished}", arguments = listOf(navArgument("infoFinished") { defaultValue = "" })) { backStackEntry ->
            val isNeedBack = backStackEntry.arguments?.getString("infoFinished") == "1"
            val personalViewModel: PersonalInfoV2ViewModel = androidx.lifecycle.viewmodel.compose.viewModel { PersonalInfoV2ViewModel(context) }
            PersonalInfoScreen(viewModel = personalViewModel, onBackClick = { navController.popBackStack() }, onNextClick = { if (isNeedBack) navController.popBackStack() else navController.navigate(Screen.ContactInfo) })
        }

        composable(route = "${Screen.ContactInfo}?infoFinished={infoFinished}", arguments = listOf(navArgument("infoFinished") { defaultValue = "" })) { backStackEntry ->
            val isNeedBack = backStackEntry.arguments?.getString("infoFinished") == "1"
            val contactViewModel: ContactInfoViewModel = androidx.lifecycle.viewmodel.compose.viewModel { ContactInfoViewModel(context) }
            ContactInfoScreen(viewModel = contactViewModel, onBackClick = { navController.popBackStack() }, onNextClick = { if (isNeedBack) navController.popBackStack() else navController.navigate(Screen.JobInfoV2) })
        }

        composable(route = "${Screen.JobInfoV2}?infoFinished={infoFinished}", arguments = listOf(navArgument("infoFinished") { defaultValue = "" })) { backStackEntry ->
            val isNeedBack = backStackEntry.arguments?.getString("infoFinished") == "1"
            val jobViewModel: JobInfoV2ViewModel = androidx.lifecycle.viewmodel.compose.viewModel { JobInfoV2ViewModel(context) }
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
                },
                onTakeWorkProofPhoto = { }
            )
        }

        // 7. Apply Loan Flow
        composable(Screen.ApplyLoan) {
            // Share ViewModel with FaceDetection via parent entry
            val applyViewModel: com.example.ivopay.app.ui.loan.ApplyLoanViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                com.example.ivopay.app.ui.loan.ApplyLoanViewModel(context)
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
            val applyViewModel: com.example.ivopay.app.ui.loan.ApplyLoanViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                viewModelStoreOwner = applyEntry
            )

            com.example.ivopay.app.ui.components.FaceDetectionView(
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
            val otherProductViewModel: com.example.ivopay.app.ui.loan.OtherProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                com.example.ivopay.app.ui.loan.OtherProductViewModel(context)
            }
            com.example.ivopay.app.ui.loan.OtherProductScreen(viewModel = otherProductViewModel, onBackClick = { navController.popBackStack() })
        }
    }
}
