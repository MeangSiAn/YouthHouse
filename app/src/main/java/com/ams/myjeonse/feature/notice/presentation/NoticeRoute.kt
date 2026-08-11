package com.ams.myjeonse.feature.notice.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.ams.myjeonse.core.ui.extension.CollectUiEffect
import com.ams.myjeonse.core.notice.presentation.model.NoticeUiModel

@Composable
fun NoticeRoute(
    onNoticeClick: (NoticeUiModel) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoticeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val noticeItems = viewModel.noticePagingData.collectAsLazyPagingItems()

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            is NoticeContract.Effect.NavigateToDetail -> onNoticeClick(effect.notice)
        }
    }

    NoticeScreen(
        uiState = uiState,
        noticeItems = noticeItems,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
