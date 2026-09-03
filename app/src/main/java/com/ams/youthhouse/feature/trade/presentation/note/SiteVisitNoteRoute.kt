package com.ams.youthhouse.feature.trade.presentation.note

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ams.youthhouse.core.ui.extension.CollectUiEffect

@Composable
fun SiteVisitNoteRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SiteVisitNoteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            SiteVisitNoteContract.Effect.NavigateBack -> onBackClick()
        }
    }

    SiteVisitNoteScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
