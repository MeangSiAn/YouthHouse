package com.ams.myjeonse.feature.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.ams.myjeonse.feature.favorite.presentation.navigation.favoriteScreen
import com.ams.myjeonse.feature.home.presentation.navigation.HomeDestination
import com.ams.myjeonse.feature.home.presentation.navigation.homeScreen
import com.ams.myjeonse.feature.notice.presentation.navigation.navigateToNoticeDetail
import com.ams.myjeonse.feature.notice.presentation.navigation.noticeDetailScreen
import com.ams.myjeonse.feature.notice.presentation.navigation.noticeScreen
import com.ams.myjeonse.feature.settings.presentation.navigation.settingsScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier,
    ) {
        homeScreen(
            onNoticeClick = navController::navigateToNoticeDetail,
            onSeeAllNoticesClick = { navController.navigateToTab(MainTab.NOTICE) },
        )
        noticeScreen(onNoticeClick = navController::navigateToNoticeDetail)
        noticeDetailScreen(onBackClick = { navController.popBackStack() })
        favoriteScreen()
        settingsScreen()
    }
}
