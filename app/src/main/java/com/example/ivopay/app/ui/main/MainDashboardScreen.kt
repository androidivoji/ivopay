package com.example.ivopay.app.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ivopay.app.ui.bill.BillItem
import com.example.ivopay.app.ui.bill.MyBillScreen
import com.example.ivopay.app.ui.home.BorrowerHomeViewModel
import com.example.ivopay.app.ui.home.HomeScreen
import com.example.ivopay.app.ui.mine.MineScreen

@Composable
fun MainDashboardScreen(
    isLogin: Boolean = false, // Ambil status asli dari SessionManager/ViewModel kamu
    onNavigateToLogin: () -> Unit = {},
    onUploadTrackingEvent: (String) -> Unit = {}, // Untuk menangani _uploadEvent("N8")
    userName: String? = null,
    userPhone: String? = null,
    maxLimitAmount: Long = 5000000,
    isUnderReview: Boolean = false,
    hasContract: Boolean = false,
    myBillList: List<BillItem> = emptyList(),
    isBillRefreshing: Boolean = false,
    onRefreshBill: () -> Unit = {},
    onCancelBill: (String) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Logika computed data & mounted() di Vue dipindah ke sini
    val tabItems = remember(isLogin) {
        if (isLogin) {
            listOf(MainTabItem.Home, MainTabItem.Bill, MainTabItem.Mine)
        } else {
            listOf(MainTabItem.Home, MainTabItem.Mine)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Box kontainer untuk menggabungkan NavigationBar dan FAB Kustom di tengah
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Pengganti <var-bottom-navigation>
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    tabItems.forEach { item ->
                        val isActive = currentRoute == item.route

                        NavigationBarItem(
                            selected = isActive,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
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
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.nameRes,
                                    color = if (isActive) Color(0xFFFE5455) else Color(0x8A000000),
                                    fontSize = 12.sp,
                                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.White, // Atau Color.Transparent
                                selectedIconColor = Color.Unspecified,
                                unselectedIconColor = Color.Unspecified
                            )
                        )
                    }
                }

                // Tombol FAB Tengah Dinamis (Menggantikan #fab di Vue)
                if (!isLogin) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 60.dp) // Mengangkat tombol agar melayang di tengah atas bar
                            .size(56.dp)
                            .background(Color(0xFFFE5455), shape = CircleShape)
                            .clickable {
                                // Pemicu tracking analitik _uploadEvent("N8")
                                onUploadTrackingEvent("N8")
                                // Pindah ke layar Login
                                onNavigateToLogin()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "Login",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Kontainer Halaman utama (main-router-view)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFFFCFC))
        ) {
            NavHost(
                navController = navController,
                startDestination = MainTabItem.Home.route
            ) {
                // 1. TAB HOME SCREEN
                composable(MainTabItem.Home.route) {
                    val homeViewModel: BorrowerHomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                        BorrowerHomeViewModel(context)
                    }
                    
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToDetail = { routeName ->
                            onNavigateToDetail(routeName)
                        }
                    )
                }

                if (isLogin) {
                    composable(MainTabItem.Bill.route) {
                        MyBillScreen(
                            billList = myBillList,
                            isRefreshing = isBillRefreshing,
                            onRefresh = onRefreshBill,
                            onItemClick = { bill ->
                                // Navigasi detail sesuai logika router Vue (e.g. BillDetails / InlgBillDetails)
                                onNavigateToDetail("BillDetails/${bill.noc}")
                            },
                            onCancelBill = onCancelBill
                        )
                    }
                }
                // 3. Tab Mine / Profil
                composable(MainTabItem.Mine.route) {
                    MineScreen(
                        isLoggedIn = isLogin,
                        userName = userName,
                        userPhone = userPhone,
                        appVersion = "1.0.0",
                        isUnderReview = isUnderReview,
                        hasContract = hasContract,
                        onNavigate = { routeName ->
                            when (routeName) {
                                "MyBill" -> navController.navigate(MainTabItem.Bill.route)
                                else -> onNavigateToDetail(routeName)
                            }
                        },
                        onNavigateToLogin = onNavigateToLogin
                    )
                }
            }
        }
    }
}