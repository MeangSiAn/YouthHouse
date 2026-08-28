package com.ams.youthhouse.feature.note.presentation

import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState

object NoteContract {

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
