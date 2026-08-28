package com.ams.youthhouse.core.ui.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.ams.youthhouse.core.presentation.contract.UiEffect
import kotlinx.coroutines.flow.Flow

/**
 * 일회성 [UiEffect]를 lifecycle을 인식하며 수집한다.
 *
 * `LaunchedEffect { uiEffect.collect { } }`만 쓰면 화면이 백그라운드로 내려가도 수집이 계속되어
 * 화면 전환·스낵바 같은 이벤트가 보이지 않는 상태에서 소비된다.
 */
@Composable
fun <E : UiEffect> CollectUiEffect(
    uiEffect: Flow<E>,
    onEffect: (E) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEffect by rememberUpdatedState(onEffect)

    LaunchedEffect(uiEffect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            uiEffect.collect { effect -> currentOnEffect(effect) }
        }
    }
}
