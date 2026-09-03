package com.ams.youthhouse.feature.trade.presentation.compare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ams.youthhouse.core.ui.extension.CollectUiEffect
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination

@Composable
fun VisitCompareRoute(
    onBackClick: () -> Unit,
    onNoteClick: (SiteVisitNoteDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitCompareViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            is VisitCompareContract.Effect.NavigateToNote -> onNoteClick(effect.destination)
        }
    }

    VisitCompareScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
