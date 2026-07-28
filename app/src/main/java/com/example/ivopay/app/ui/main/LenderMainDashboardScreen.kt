package com.example.ivopay.app.ui.main

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ivopay.app.ui.lender.home.LenderHomeScreen
import com.example.ivopay.app.ui.lender.home.LenderHomeViewModel
import com.example.ivopay.app.ui.lender.portofolio.LenderPortfolioScreen
import com.example.ivopay.app.ui.lender.settings.LenderSettingsScreen
import com.example.ivopay.app.ui.lender.settings.LenderSettingsViewModel
import com.example.ivopay.app.ui.navigation.Screen
import com.example.ivopay.app.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderMainDashboardScreen(
    rootNavController: NavHostController,
    lenderStatus: Int = 1, // Diambil dari state/ViewModel kamu
    initialShowPopup: Boolean = false // Set true jika ingin langsung menampilkan popup kualifikasi
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // State untuk kontrol PopUp Not Qualified (v-model:show="showUnqualifiedPop")
    var showUnqualifiedPop by remember { mutableStateOf(initialShowPopup) }

    // Efek ketika halaman dimuat (mounted) -> Mengadopsi _canScreenshot('true')
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    // Mengatur daftar menu tab secara dinamis berdasarkan lenderStatus (computed: getTabItemList)
    val tabItems = remember(lenderStatus) {
        if (lenderStatus == 1) {
            listOf(LenderTabItem.Home, LenderTabItem.Portfolio, LenderTabItem.Setting)
        } else {
            listOf(LenderTabItem.Home, LenderTabItem.Setting)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Pengganti <var-bottom-navigation>
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(60.dp)
            ) {
                tabItems.forEach { item ->
                    val isActive = currentRoute == item.route

                    NavigationBarItem(
                        selected = isActive,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    // Memastikan tidak terjadi tumpukan stack halaman yang berulang
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Image(
                                painter = painterResource(id = if (isActive) item.activeIcon else item.normalIcon),
                                contentDescription = item.nameRes,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.nameRes,
                                // Warna aktif mengikuti Vue: #FE5455, warna tidak aktif abu-abu
                                color = if (isActive) Color(0xFFFE5455) else Color(0x8A000000),
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent // Menghilangkan background pill bawaan material3
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Kontainer Konten Utama (Pengganti <router-view> dan .main-router-view)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8F8)) // background-color: #f8f8f8
        ) {
            NavHost(
                navController = navController,
                startDestination = LenderTabItem.Home.route
            ) {
                composable(LenderTabItem.Home.route) {
                    val homeViewModel = remember { LenderHomeViewModel(context) }

                    LenderHomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToDetail = { ati ->
                            rootNavController.navigate("borrower_detail/$ati")
                        },
                        onNavigateToProfile = {
                            rootNavController.navigate(Screen.LenderBasicInfo)
                        },
                        onNavigateToPortfolio = {
                            navController.navigate(LenderTabItem.Portfolio.route)
                        }
                    )
                }
                composable(LenderTabItem.Portfolio.route) {
                    LenderPortfolioScreen(
                        navController = rootNavController
                    )
                }
                composable(LenderTabItem.Setting.route) {
                    val settingsViewModel = remember { LenderSettingsViewModel(context) }

                    LenderSettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigate = { targetRoute ->
                            // Pindah ke halaman seperti lender_basic_info, my_contracts, dll.
                            rootNavController.navigate(targetRoute)
                        },
                        onNavigateToLogin = {
                            rootNavController.navigate("login_screen?role=1")
                        },
                        onLogoutClick = {
                            // Hapus session dan kembali ke Select Role / Login
                            val sessionManager = SessionManager(context)
                            sessionManager.clearSession()

                            rootNavController.navigate(Screen.SelectRole) {
                                popUpTo(Screen.LenderMain) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }

    // Modal Popup Notifikasi Unqualified (Pengganti <van-popup>)
    if (showUnqualifiedPop) {
        Dialog(onDismissRequest = { showUnqualifiedPop = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.82f) // width: 82vw
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Setelah penilaian, Anda saat ini tidak memenuhi syarat untuk aplikasi. Silakan melamar di hari lain.",
                        color = Color(0xFF262626),
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showUnqualifiedPop = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "OK", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// Layar Sementara untuk Representasi Halaman Internal Tab
@Composable
fun DummyScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, fontSize = 18.sp, color = Color.Gray)
    }
}