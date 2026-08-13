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
import com.example.ivopay.app.ui.mine.*
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
                    // Handle navigation from Splash logic (Simplified for global logout)
                    navController.navigate(Screen.SelectRole) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }

        // 1. Screen Select Role (Pilih Borrower / Lender)
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
                            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }

        // 2. Dashboard Utama Peminjam (Main)
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

        // 3. Dashboard Lender / Mitra (l_main)
        composable(Screen.LenderMain) {
            LenderMainDashboardScreen(
                lenderStatus = 1,
                rootNavController = navController
            )
        }

        // 4. Screen Login
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
            route = "${Screen.GestureLogin}?phone={phone}&role={role}",
            arguments = listOf(
                navArgument("phone") { defaultValue = "" },
                navArgument("role") { defaultValue = "0" }
            )
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "0"
            val gestureViewModel = remember { 
                GestureLoginViewModel(context).apply { 
                    init(phone, role.toInt()) 
                } 
            } 
            
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
                        navController.navigate(Screen.Login) {
                            popUpTo(Screen.SelectRole) { inclusive = false }
                        }
                    } else {
                        val currentRole = gestureViewModel.userRole
                        navController.navigate("${Screen.Login}?role=$currentRole")
                    }
                }
            )
        }

        composable(
            route = "${Screen.GestureCreate}?fromPage={fromPage}",
            arguments = listOf(navArgument("fromPage") { defaultValue = "" })
        ) { backStackEntry ->
            val fromPage = backStackEntry.arguments?.getString("fromPage") ?: ""
            val gestureCreateViewModel: GestureCreateViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                GestureCreateViewModel(context)
            }
            GestureCreateScreen(
                viewModel = gestureCreateViewModel,
                onBackClick = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.Main) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 5. Screen Logout dan Profile
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
            val profileViewModel: com.example.ivopay.app.ui.mine.MyProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                com.example.ivopay.app.ui.mine.MyProfileViewModel(context)
            }
            MyProfileScreen(
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToStep = { stepRoute, isFinished ->
                    navController.navigate(stepRoute)
                },
                onNavigateToLogin = { navController.navigate(Screen.Login) }
            )
        }

        // 6. Alur Informasi Pengguna (KYC / Onboarding)
        composable(
            route = "${Screen.BaseInfo}?infoFinished={infoFinished}",
            arguments = listOf(navArgument("infoFinished") { defaultValue = "" })
        ) { backStackEntry ->
            val infoFinished = backStackEntry.arguments?.getString("infoFinished") ?: ""
            val isNeedBack = infoFinished == "1"

            val baseInfoViewModel: BaseInfoViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                BaseInfoViewModel(context)
            }
            BaseInfoScreen(
                viewModel = baseInfoViewModel,
                onBackClick = { navController.popBackStack() },
                onNextClick = { 
                    if (isNeedBack) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.PersonalInfoV2)
                    }
                }
            )
        }

        composable(
            route = "${Screen.PersonalInfoV2}?infoFinished={infoFinished}",
            arguments = listOf(navArgument("infoFinished") { defaultValue = "" })
        ) { backStackEntry ->
            val infoFinished = backStackEntry.arguments?.getString("infoFinished") ?: ""
            val isNeedBack = infoFinished == "1"

            val personalViewModel: PersonalInfoV2ViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                PersonalInfoV2ViewModel(context)
            }
            PersonalInfoScreen(
                viewModel = personalViewModel,
                onBackClick = { navController.popBackStack() },
                onNextClick = { 
                    if (isNeedBack) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.ContactInfo)
                    }
                }
            )
        }

        composable(
            route = "${Screen.ContactInfo}?infoFinished={infoFinished}",
            arguments = listOf(navArgument("infoFinished") { defaultValue = "" })
        ) { backStackEntry ->
            val infoFinished = backStackEntry.arguments?.getString("infoFinished") ?: ""
            val isNeedBack = infoFinished == "1"

            val contactViewModel: ContactInfoViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                ContactInfoViewModel(context)
            }
            ContactInfoScreen(
                viewModel = contactViewModel,
                onBackClick = { navController.popBackStack() },
                onNextClick = { 
                    if (isNeedBack) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.JobInfoV2)
                    }
                }
            )
        }

        composable(
            route = "${Screen.JobInfoV2}?infoFinished={infoFinished}",
            arguments = listOf(navArgument("infoFinished") { defaultValue = "" })
        ) { backStackEntry ->
            val infoFinished = backStackEntry.arguments?.getString("infoFinished") ?: ""
            val isNeedBack = infoFinished == "1"

            val jobViewModel: com.example.ivopay.app.ui.mine.JobInfoV2ViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                com.example.ivopay.app.ui.mine.JobInfoV2ViewModel(context)
            }

            com.example.ivopay.app.ui.mine.JobInfoV2Screen(
                viewModel = jobViewModel,
                onBackClick = { navController.popBackStack() },
                onNextClick = { cme, fcoa, tnpo ->
                    if (isNeedBack) {
                        navController.popBackStack()
                    } else if (cme?.uico == true) {
                        if (cme.wof == false) {
                            // Conditional loan navigation
                            val fcoaPsw = fcoa?.get("psw")?.asInt ?: 0
                            val tnpoPsw = tnpo?.get("psw")?.asInt ?: 0
                            
                            if (fcoaPsw == 1) {
                                navController.navigate(Screen.ApplyLoan) 
                            } else if (tnpoPsw == 1) {
                                navController.navigate(Screen.Main) // Map to Tadpole equivalent
                            } else {
                                navController.navigate(Screen.Main)
                            }
                        } else {
                            navController.navigate(Screen.ApplyLoan)
                        }
                    } else {
                        navController.navigate(Screen.MyProfile)
                    }
                },
                onTakeWorkProofPhoto = { callback ->
                    // Simulate camera result
                    // jobViewModel.updateField(jobViewModel.state.copy(wkptie_bitmap = bitmap))
                }
            )
        }

        // 7. Alur Pinjaman (Loan)
        composable(Screen.ApplyLoan) {
            val applyViewModel = remember { com.example.ivopay.app.ui.loan.ApplyLoanViewModel(context) }
            ApplyLoanScreen(
                viewModel = applyViewModel,
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = { noc ->
                    navController.navigate("ApplySucceedPage?noc=$noc&showPop=1") {
                        popUpTo(Screen.Main) { inclusive = false }
                    }
                }
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
            val needConfirmStr = backStackEntry.arguments?.getString("need_confirm") ?: "0"
            val showPopParam = backStackEntry.arguments?.getString("showPop") ?: "0"
            val needConfirm = needConfirmStr == "1"
            val showInitialPop = showPopParam == "1"

            val prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE)
            val showRatePop = !prefs.getBoolean("showRatePop", false) || showInitialPop
            if (showRatePop && !showInitialPop) {
                prefs.edit().putBoolean("showRatePop", true).apply()
            }

            ApplySucceedScreen(
                ocEui = sessionManager.getActStatus() == "1", // Example logic
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
            val lenderInfoViewModel = remember { LenderBasicInfoViewModel(context) }
            LenderBasicInfoScreen(
                viewModel = lenderInfoViewModel,
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.navigate(Screen.LenderMain) {
                        popUpTo(Screen.SelectRole) { inclusive = true }
                    }
                },
                onSelectPhoto = { _, _ -> }
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
            val signViewModel: BorrowerSignContractsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            
            BorrowerSignContractsScreen(
                noc = noc,
                isWiue = isWiue,
                viewModel = signViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 8. Alur Kontrak Lender
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
            LenderBorrowerSignContractsScreen(
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

        composable(Screen.MyContracts) {
            MyContractsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToChooseContracts = { cno ->
                    navController.navigate("choose_contracts?cno=$cno")
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
                onNavigateBcaGuide = { /* Navigate to BCA Guide */ }
            )
        }

        composable(Screen.BankInfo) {
            // BankInfoScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.RegisterInfoWaiting) {
            // RegisterInfoWaitingScreen()
        }

        composable(Screen.UnderReview) {
            // UnderReviewScreen()
        }

        composable(
            route = "${Screen.A_Apply}?lackin_flow_typ={lackin_flow_typ}&konfigurasi={konfigurasi}",
            arguments = listOf(
                navArgument("lackin_flow_typ") { defaultValue = "" },
                navArgument("konfigurasi") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val typ = backStackEntry.arguments?.getString("lackin_flow_typ") ?: ""
            val config = backStackEntry.arguments?.getString("konfigurasi") ?: ""
            // A_ApplyScreen(typ = typ, config = config)
        }
    }
}
