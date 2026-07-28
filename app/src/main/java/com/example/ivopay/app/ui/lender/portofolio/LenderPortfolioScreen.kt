package com.example.ivopay.app.ui.lender.portofolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ivopay.app.ui.lender.portofolio.alreadypaid.AlreadyPaidBillScreen
import com.example.ivopay.app.ui.lender.portofolio.toberecharged.ToBeRechargedBillScreen
import com.example.ivopay.app.ui.lender.portofolio.waitsignature.WaitSignatureScreen
import kotlinx.coroutines.launch

val HeaderRed = Color(0xFFFE5455)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderPortfolioScreen(
    navController: NavHostController,
    viewModel: LenderPortfolioViewModel = remember { LenderPortfolioViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        "Agreement to be signed",
        "Order to be recharged",
        "Order already paid"
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )

    // Sync antara Pager Swipe dengan Tab Selected State
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(pagerState.currentPage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        // Top App Bar / Title Header
        Surface(
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(Color.White)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Portofolio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }

        // Penggunaan SecondaryScrollableTabRow & TabRowDefaults.SecondaryIndicator
        SecondaryScrollableTabRow(
            selectedTabIndex = uiState.selectedTabIndex,
            containerColor = Color.White,
            contentColor = HeaderRed,
            edgePadding = 8.dp,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        selectedTabIndex = uiState.selectedTabIndex
                    ),
                    color = HeaderRed
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val count = when (index) {
                    0 -> uiState.waitSignCount
                    1 -> uiState.toRechargeCount
                    else -> 0
                }

                Tab(
                    selected = uiState.selectedTabIndex == index,
                    onClick = {
                        viewModel.onTabSelected(index)
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        if (count > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ) {
                                        Text(if (count > 99) "99+" else count.toString())
                                    }
                                }
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.selectedTabIndex == index) HeaderRed else Color.Gray,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        } else {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.selectedTabIndex == index) HeaderRed else Color.Gray
                            )
                        }
                    }
                )
            }
        }

        // Pager Content untuk 3 Tab Child
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> WaitSignatureScreen(
                    onUpdateCount = { count -> viewModel.updateWaitSignCount(count) },
                    onNavigateToDetail = { odi ->
                        navController.navigate("sign_contracts/$odi")
                    }
                )
                1 -> ToBeRechargedBillScreen(
                    onUpdateCount = { count -> viewModel.updateRechargeCount(count) },
                    onNavigateToDetail = { odi ->
                        navController.navigate("borrow_contracts_list/$odi")
                    }
                )
                2 -> AlreadyPaidBillScreen(
                    onNavigateToDetail = { odi ->
                        navController.navigate("already_paid_bill_detail/$odi")
                    }
                )
            }
        }
    }
}