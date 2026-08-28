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
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            is HomeContract.Effect.NavigateToDetail -> onNoticeClick(effect.notice)
            HomeContract.Effect.NavigateToNoticeList -> onSeeAllNoticesClick()
        }
    }

    HomeScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
