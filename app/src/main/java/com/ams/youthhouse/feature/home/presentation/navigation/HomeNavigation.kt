package com.ams.youthhouse.feature.home.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.feature.home.presentation.HomeRoute
import com.ams.youthhouse.feature.home.presentation.guide.GuideScreen
import com.ams.youthhouse.feature.home.presentation.guide.HomeGuide
import kotlinx.serialization.Serializable

/** 홈 탭의 navigation contract. 다른 feature는 이 타입까지만 참조한다. */
@Serializable
data object HomeDestination

/** 가이드 글. enum 이름을 싣고 화면에서 되찾는다 — 모르는 이름이면 첫 글로 떨어진다. */
@Serializable
data class GuideDestination(val guideName: String)

fun NavGraphBuilder.homeScreen(
    onNoticeClick: (NoticeUiModel) -> Unit,
    onSeeAllNoticesClick: () -> Unit,
    onSeeAllScheduleClick: () -> Unit,
    onComplexClick: (String, String) -> Unit,
    onSeeAllComplexesClick: () -> Unit,
    onVisitNoteClick: (kaptCode: String, name: String) -> Unit,
    onCompareVisitsClick: () -> Unit,
    onGuideClick: (HomeGuide) -> Unit,
) {
    composable<HomeDestination> {
        HomeRoute(
            onNoticeClick = onNoticeClick,
            onSeeAllNoticesClick = onSeeAllNoticesClick,
            onSeeAllScheduleClick = onSeeAllScheduleClick,
            onComplexClick = onComplexClick,
            onSeeAllComplexesClick = onSeeAllComplexesClick,
            onVisitNoteClick = onVisitNoteClick,
            onCompareVisitsClick = onCompareVisitsClick,
            onGuideClick = onGuideClick,
        )
    }
}

fun NavGraphBuilder.guideScreen(
    onBackClick: () -> Unit,
) {
    composable<GuideDestination> { backStackEntry ->
        val destination = backStackEntry.toRoute<GuideDestination>()
        GuideScreen(
            guide = HomeGuide.fromName(destination.guideName),
            onBackClick = onBackClick,
        )
    }
}

fun NavController.navigateToGuide(guide: HomeGuide) {
    navigate(GuideDestination(guideName = guide.name))
}
