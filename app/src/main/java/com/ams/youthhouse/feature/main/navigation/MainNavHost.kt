package com.ams.youthhouse.feature.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.ams.youthhouse.feature.note.presentation.navigation.noteScreen
import com.ams.youthhouse.feature.home.presentation.navigation.HomeDestination
import com.ams.youthhouse.feature.home.presentation.navigation.homeScreen
import com.ams.youthhouse.feature.notice.presentation.navigation.navigateToNoticeDetail
import com.ams.youthhouse.feature.notice.presentation.navigation.noticeDetailScreen
import com.ams.youthhouse.feature.notice.presentation.navigation.noticeScreen
import com.ams.youthhouse.feature.schedule.presentation.navigation.scheduleScreen
import com.ams.youthhouse.feature.settings.presentation.navigation.settingsScreen

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
        scheduleScreen(
            onNoticeClick = navController::navigateToNoticeDetail,
            onBrowseNoticesClick = { navController.navigateToTab(MainTab.NOTICE) },
        )
        // 탭에서 빠져 지금은 도달 경로가 없다. 기능이 생기면 MainTab에 항목만 되살린다.
        noteScreen()
        settingsScreen()
    }
}
