package com.ams.myjeonse.feature.notice.presentation.detail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ams.myjeonse.core.ui.extension.CollectUiEffect
import kotlinx.coroutines.launch

@Composable
fun NoticeDetailRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoticeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    val resources = LocalResources.current
    // 이펙트 콜백에서 suspend 함수(showSnackbar)를 호출하기 위한 스코프.
    val scope = rememberCoroutineScope()

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            is NoticeDetailContract.Effect.OpenUrl -> uriHandler.openUri(effect.url)

            is NoticeDetailContract.Effect.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(resources.getString(effect.messageRes))
            }
        }
    }

    NoticeDetailScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}
