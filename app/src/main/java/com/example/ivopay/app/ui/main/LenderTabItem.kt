package com.example.ivopay.app.ui.main

import com.example.ivopay.R

sealed class LenderTabItem(
    val route: String,
    val nameRes: String, // String resource ID atau nama teks langsung
    val activeIcon: Int,
    val normalIcon: Int
) {
    object Home : LenderTabItem(
        route = "l_home",
        nameRes = "Home",
        activeIcon = R.drawable.iv_tab_home_sel, // Pastikan aset PNG/Vector ada di res/drawable
        normalIcon = R.drawable.iv_tab_home_nor
    )

    object Portfolio : LenderTabItem(
        route = "l_portfolio",
        nameRes = "Portofolio",
        activeIcon = R.drawable.iv_tab_invest_sel,
        normalIcon = R.drawable.iv_tab_invest_nor
    )

    object Setting : LenderTabItem(
        route = "l_setting",
        nameRes = "Mine",
        activeIcon = R.drawable.iv_tab_set_sel,
        normalIcon = R.drawable.iv_tab_set_nor
    )
}