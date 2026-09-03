package com.ams.youthhouse.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.ui.extension.CollectUiEffect

@Composable
fun HomeRoute(
    onNoticeClick: (NoticeUiModel) -> Unit,
    onSeeAllNoticesClick: () -> Unit,
    onSeeAllScheduleClick: () -> Unit,
    onComplexClick: (String, String) -> Unit,
    onSeeAllComplexesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            is HomeContract.Effect.NavigateToDetail -> onNoticeClick(effect.notice)
            HomeContract.Effect.NavigateToNoticeList -> onSeeAllNoticesClick()
            HomeContract.Effect.NavigateToSchedule -> onSeeAllScheduleClick()
            is HomeContract.Effect.NavigateToComplexDetail ->
                onComplexClick(effect.kaptCode, effect.name)
            HomeContract.Effect.NavigateToTrade -> onSeeAllComplexesClick()
        }
    }

    HomeScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
