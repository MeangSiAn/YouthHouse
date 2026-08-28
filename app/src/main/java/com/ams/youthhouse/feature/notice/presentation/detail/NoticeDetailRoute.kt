package com.ams.youthhouse.feature.notice.presentation.detail

import android.net.Uri
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
import com.ams.youthhouse.R
import com.ams.youthhouse.core.ui.extension.CollectUiEffect
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

            is NoticeDetailContract.Effect.OpenMap -> {
                // geo:0,0?q=<주소> — 기기에 깔린 지도앱이 주소를 검색어로 받아 연다.
                // 지도앱이 하나도 없으면 UriHandler가 IllegalArgumentException을 던진다.
                try {
                    uriHandler.openUri("geo:0,0?q=${Uri.encode(effect.address)}")
                } catch (_: IllegalArgumentException) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            resources.getString(R.string.notice_detail_no_map_app),
                        )
                    }
                }
            }

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
