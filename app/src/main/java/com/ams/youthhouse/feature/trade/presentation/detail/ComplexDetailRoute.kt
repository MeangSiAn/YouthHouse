package com.ams.youthhouse.feature.trade.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ComplexDetailRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ComplexDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ComplexDetailScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
