package com.example.ivopay.app.ui.main

import com.example.ivopay.R

sealed class MainTabItem(
    val route: String,
    val nameRes: String,
    val activeIcon: Int,
    val normalIcon: Int
) {
    object Home : MainTabItem("home", "Home", R.drawable.iv_tab_home_sel, R.drawable.iv_tab_home_nor)
    object Bill : MainTabItem("MyBill", "Tagihan", R.drawable.iv_tab_bill_sel, R.drawable.iv_tab_bill_nor)
    object Mine : MainTabItem("mine", "Mine", R.drawable.iv_tab_set_sel, R.drawable.iv_tab_set_nor)
}