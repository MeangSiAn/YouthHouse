package com.ams.youthhouse.feature.schedule.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.feature.schedule.presentation.ScheduleRoute
import kotlinx.serialization.Serializable

/** 내 일정 탭의 navigation contract. 다른 feature는 이 타입까지만 참조한다. */
@Serializable
data object ScheduleDestination

fun NavGraphBuilder.scheduleScreen(
    onNoticeClick: (NoticeUiModel) -> Unit,
    onBrowseNoticesClick: () -> Unit,
) {
    composable<ScheduleDestination> {
        ScheduleRoute(
            onNoticeClick = onNoticeClick,
            onBrowseNoticesClick = onBrowseNoticesClick,
        )
    }
}
