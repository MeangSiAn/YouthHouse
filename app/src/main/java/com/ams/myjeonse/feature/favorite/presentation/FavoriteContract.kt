package com.ams.myjeonse.feature.favorite.presentation

import com.ams.myjeonse.core.presentation.contract.UiAction
import com.ams.myjeonse.core.presentation.contract.UiEffect
import com.ams.myjeonse.core.presentation.contract.UiState

object FavoriteContract {

    data class State(
        val isLoading: Boolean = false,
    ) : UiState

    sealed interface Action : UiAction {
        data object Refreshed : Action
    }

    sealed interface Effect : UiEffect {
        data object None : Effect
    }
}
