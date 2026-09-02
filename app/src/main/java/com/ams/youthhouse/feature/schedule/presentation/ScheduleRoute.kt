package com.ams.youthhouse.feature.schedule.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.ui.extension.CollectUiEffect

@Composable
fun ScheduleRoute(
    onNoticeClick: (NoticeUiModel) -> Unit,
    onBrowseNoticesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            is ScheduleContract.Effect.NavigateToDetail -> onNoticeClick(effect.notice)
            ScheduleContract.Effect.NavigateToNoticeList -> onBrowseNoticesClick()
        }
    }

    ScheduleScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
