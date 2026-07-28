package com.example.ivopay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.blankj.utilcode.util.Utils
import com.example.ivopay.app.ui.auth.SelectRoleScreen
import com.example.ivopay.app.ui.lender.borrower.BorrowerDetailScreen
import com.example.ivopay.app.ui.lender.detail.AlreadyPaidBillDetailScreen
import com.example.ivopay.app.ui.lender.detail.ChooseContractsScreen
import com.example.ivopay.app.ui.lender.portofolio.toberecharged.ToBeRechargedDetailScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsign.BorrowerSignContractsScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsign.PlatformSignContractsScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsign.WaitSignContractsScreen
import com.example.ivopay.app.ui.loan.ApplyLoanScreen
import com.example.ivopay.app.ui.loan.ApplySucceedScreen
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.init(application)

        // Menangkap data rute yang dikirim oleh SplashActivity (Default: "select_role" atau "main")
        val targetRoute = intent.getStringExtra("TARGET_ROUTE") ?: "select_role"

        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context) }

            NavHost(
                navController = navController,
                startDestination = targetRoute
            ) {
                // 0. Screen Select Role (Pilih Borrower / Lender)
                composable("select_role") {
                    SelectRoleScreen(
                        isLoggedIn = sessionManager.isUserLoggedIn(),
                        onUploadTrackingEvent = { event ->
                            // Panggil analytics tracker (AppsFlyer/Adjust/Firebase)
                        },
                        onNavigateToBorrowerMain = {
                            navController.navigate("main") {
                                popUpTo("select_role") { inclusive = true }
                            }
                        },
                        onNavigateToLenderLogin = {
//                            navController.navigate("login_screen?role=1")
                            navController.navigate("lender_basic_info")
                        },
                        onNavigateToLenderBasicInfo = {
                            navController.navigate("lender_basic_info")
                        },
                        onNavigateToLenderMain = {
                            navController.navigate("l_main") {
                                popUpTo("select_role") { inclusive = true }
                            }
                        },
                        onFetchLenderUserInfo = { onFinished ->
                            // Contoh simulasi fetching user info lender via API/ViewModel
                            val hasInm = false // Ganti dengan hasil dari response API
                            onFinished(hasInm)
                            if (!hasInm) {
                                navController.navigate("lender_basic_info")
                            } else {
                                navController.navigate("l_main") {
                                    popUpTo("select_role") { inclusive = true }
                                }
                            }
                        }
                    )
                }

                // 1. Dashboard Utama Peminjam (Main)
                composable("main") {
                    MainDashboardScreen(
                        isLogin = sessionManager.isUserLoggedIn(),
                        onNavigateToDetail = { route ->
                            navController.navigate(route)
                        },
                        onNavigateToLogin = {
                            navController.navigate("login_screen")
                        },
                        onUploadTrackingEvent = { eventCode ->
                            // Logika tracking analytics
                        }
                    )
                }

                // 2. Dashboard Lender / Mitra (l_main)
                composable("l_main") {
                    LenderMainDashboardScreen(
                        lenderStatus = 1,
                        rootNavController = navController
                    )
                }

                // 3. Screen Login
                composable("login_screen") {
                    val loginViewModel = remember { LoginViewModel(context) }

                    LoginScreen(
                        viewModel = loginViewModel,
                        onBackClick = { navController.popBackStack() },
                        onNavigate = { route ->
                            navController.navigate(route)
                        }
                    )
                }

                // 4. Screen Berhenti / Keluar (LogoutAndExitPage)
                composable("LogoutAndExitPage") {
                    LogoutAndExitScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToAccountLogout = {
                            navController.navigate("AccountLogoutPage")
                        },
                        onLogoutConfirmed = {
                            sessionManager.clearSession()
                            navController.navigate("select_role") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable("MyProfile") {
                    MyProfileScreen(
                        isLoggedIn = sessionManager.isUserLoggedIn(),
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNavigateToStep = { stepRoute, isFinished ->
                            navController.navigate("$stepRoute?infoFinished=$isFinished")
                        },
                        onNavigateToLogin = {
                            navController.navigate("login_screen")
                        }
                    )
                }

                composable("BaseInfo") {
                    BaseInfoScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNextClick = { baseInfoData ->
                            navController.navigate("PersonalInfoV2")
                        },
                        onSelectKtpPhoto = {
                            // Panggil intent kamera / SDK OCR
                        },
                        onOpenTermsAndConditions = {
                            navController.navigate("TermsAndConditionsPage")
                        }
                    )
                }

                composable("PersonalInfoV2") {
                    PersonalInfoScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNextClick = { personalInfoData ->
                            navController.navigate("ContactInfoPage")
                        }
                    )
                }

                composable("ContactInfoPage") {
                    ContactInfoScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNextClick = { emergencyContacts ->
                            navController.navigate("JobInfoV2")
                        }
                    )
                }

                composable("ContactInfoV2Page") {
                    ContactInfoV2Screen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSubmitClick = { contacts, addressInfo ->
                            navController.navigate("BankInfo")
                        }
                    )
                }

                composable("JobInfoV2") {
                    JobInfoV2Screen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSubmitClick = { jobData ->
                            navController.navigate("main")
                        },
                        onTakeWorkProofPhoto = { docTypeIndex, onPhotoCaptured ->
                            onPhotoCaptured("path/to/work_proof_image.jpg")
                        }
                    )
                }

                composable("ApplyLoan") {
                    ApplyLoanScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSubmitSuccess = {
                            navController.navigate("ApplySucceedPage") {
                                popUpTo("main") { inclusive = false }
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
                            navController.navigate("main") {
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
                        onConfirmInsurance = { nocId ->
                            // uploadNocInsurance API
                        }
                    )
                }

                // 5. Screen Hapus Akun (AccountLogoutPage)
                composable("AccountLogoutPage") {
                    // AccountLogoutScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable("lender_basic_info") {
                    LenderBasicInfoScreen(
                        onBackClick = { navController.popBackStack() },
                        onSubmitSuccess = {
                            navController.navigate("l_main") {
                                popUpTo("select_role") { inclusive = true }
                            }
                        },
                        onSelectPhoto = { index, onCaptured ->
                            // Panggil intent kamera / image picker di sini
                            // Setelah dapet hasil bitmap:
                            // onCaptured(bitmapResult)
                        }
                    )
                }

                composable(
                    route = "sign_contracts/{odi}",
                    arguments = listOf(
                        navArgument("odi") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val odi = backStackEntry.arguments?.getString("odi") ?: ""

                    // Panggil Screen Compose pengganti halaman Vue WaitSignContacts
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

                // Rute pendukung lainnya (Borrower & Platform Sign)
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
                                // replace route seperti pada Vue _routeReplace
                                popUpTo("borrower_sign_contracts/{mdi}") { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "platform_sign_contracts/{mdi}",
                    arguments = listOf(
                        navArgument("mdi") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val mdi = backStackEntry.arguments?.getString("mdi") ?: ""

                    PlatformSignContractsScreen(
                        mdi = mdi,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "borrow_contracts_list/{odi}",
                    arguments = listOf(
                        navArgument("odi") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val odi = backStackEntry.arguments?.getString("odi") ?: ""

                    ToBeRechargedDetailScreen(
                        odi = odi,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "borrower_detail/{ati}",
                    arguments = listOf(
                        navArgument("ati") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val ati = backStackEntry.arguments?.getString("ati") ?: ""

                    BorrowerDetailScreen(
                        ati = ati,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "already_paid_bill_detail/{odi}",
                    arguments = listOf(
                        navArgument("odi") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val odi = backStackEntry.arguments?.getString("odi") ?: ""

                    AlreadyPaidBillDetailScreen(
                        odi = odi,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToChooseContracts = { mdi ->
                            navController.navigate("choose_contracts/$mdi")
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
            }
        }
    }
}