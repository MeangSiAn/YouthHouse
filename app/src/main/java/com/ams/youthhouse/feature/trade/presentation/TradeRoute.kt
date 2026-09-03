package com.ams.youthhouse.feature.trade.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ams.youthhouse.core.ui.extension.CollectUiEffect
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination

@Composable
fun TradeRoute(
    onComplexClick: (kaptCode: String, name: String) -> Unit,
    onNoteClick: (SiteVisitNoteDestination) -> Unit,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TradeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            is TradeContract.Effect.NavigateToDetail ->
                onComplexClick(effect.kaptCode, effect.name)

            is TradeContract.Effect.NavigateToNote -> onNoteClick(effect.destination)

            TradeContract.Effect.NavigateToCompare -> onCompareClick()
        }
    }

    TradeScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
