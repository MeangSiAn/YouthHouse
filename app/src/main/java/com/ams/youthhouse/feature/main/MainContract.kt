package com.ams.youthhouse.feature.main

import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState

object MainContract {

    /**
     * 선택된 탭은 NavController가 단일 진실 공급원이므로 State에 두지 않는다.
     * 여기에는 탭 호스트 자체의 화면 상태만 둔다.
     */
    data class State(
        val isBottomBarVisible: Boolean = true,
    ) : UiState

    sealed interface Action : UiAction {
        data class BottomBarVisibilityChanged(
            val isVisible: Boolean,
        ) : Action
    }

    sealed interface Effect : UiEffect {
        data object None : Effect
    }
}
