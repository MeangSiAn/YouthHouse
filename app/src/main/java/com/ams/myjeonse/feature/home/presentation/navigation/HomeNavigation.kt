package com.ams.myjeonse.feature.home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ams.myjeonse.core.notice.presentation.model.NoticeUiModel
import com.ams.myjeonse.feature.home.presentation.HomeRoute
import kotlinx.serialization.Serializable

/** 홈 탭의 navigation contract. 다른 feature는 이 타입까지만 참조한다. */
@Serializable
data object HomeDestination

fun NavGraphBuilder.homeScreen(
    onNoticeClick: (NoticeUiModel) -> Unit,
    onSeeAllNoticesClick: () -> Unit,
) {
    composable<HomeDestination> {
        HomeRoute(
            onNoticeClick = onNoticeClick,
            onSeeAllNoticesClick = onSeeAllNoticesClick,
        )
    }
}
