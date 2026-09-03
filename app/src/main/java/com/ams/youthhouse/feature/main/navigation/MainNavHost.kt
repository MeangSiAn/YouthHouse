package com.ams.youthhouse.feature.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.ams.youthhouse.feature.home.presentation.navigation.HomeDestination
import com.ams.youthhouse.feature.home.presentation.navigation.guideScreen
import com.ams.youthhouse.feature.home.presentation.navigation.homeScreen
import com.ams.youthhouse.feature.home.presentation.navigation.navigateToGuide
import com.ams.youthhouse.feature.notice.presentation.navigation.navigateToNoticeDetail
import com.ams.youthhouse.feature.notice.presentation.navigation.noticeDetailScreen
import com.ams.youthhouse.feature.notice.presentation.navigation.noticeScreen
import com.ams.youthhouse.feature.schedule.presentation.navigation.scheduleScreen
import com.ams.youthhouse.feature.settings.presentation.navigation.settingsScreen
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination
import com.ams.youthhouse.feature.trade.presentation.navigation.complexDetailScreen
import com.ams.youthhouse.feature.trade.presentation.navigation.navigateToComplexDetail
import com.ams.youthhouse.feature.trade.presentation.navigation.navigateToSiteVisitNote
import com.ams.youthhouse.feature.trade.presentation.navigation.navigateToVisitCompare
import com.ams.youthhouse.feature.trade.presentation.navigation.siteVisitNoteScreen
import com.ams.youthhouse.feature.trade.presentation.navigation.tradeScreen
import com.ams.youthhouse.feature.trade.presentation.navigation.visitCompareScreen

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
            onSeeAllScheduleClick = { navController.navigateToTab(MainTab.SCHEDULE) },
            onComplexClick = navController::navigateToComplexDetail,
            onSeeAllComplexesClick = { navController.navigateToTab(MainTab.TRADE) },
            onVisitNoteClick = { kaptCode, name ->
                navController.navigateToSiteVisitNote(
                    SiteVisitNoteDestination(kaptCode = kaptCode, name = name),
                )
            },
            onCompareVisitsClick = navController::navigateToVisitCompare,
            onGuideClick = navController::navigateToGuide,
        )
        guideScreen(onBackClick = { navController.popBackStack() })
        noticeScreen(onNoticeClick = navController::navigateToNoticeDetail)
        noticeDetailScreen(onBackClick = { navController.popBackStack() })
        scheduleScreen(
            onNoticeClick = navController::navigateToNoticeDetail,
            onBrowseNoticesClick = { navController.navigateToTab(MainTab.NOTICE) },
        )
        tradeScreen(
            onComplexClick = navController::navigateToComplexDetail,
            onNoteClick = navController::navigateToSiteVisitNote,
            onCompareClick = navController::navigateToVisitCompare,
        )
        complexDetailScreen(
            onBackClick = { navController.popBackStack() },
            onNoteClick = navController::navigateToSiteVisitNote,
        )
        siteVisitNoteScreen(onBackClick = { navController.popBackStack() })
        visitCompareScreen(
            onBackClick = { navController.popBackStack() },
            onNoteClick = navController::navigateToSiteVisitNote,
        )
        settingsScreen()
    }
}
