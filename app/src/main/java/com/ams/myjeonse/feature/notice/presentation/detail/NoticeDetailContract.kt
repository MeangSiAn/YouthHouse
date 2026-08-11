package com.ams.myjeonse.feature.notice.presentation.detail

import androidx.annotation.StringRes
import com.ams.myjeonse.core.presentation.contract.UiAction
import com.ams.myjeonse.core.presentation.contract.UiEffect
import com.ams.myjeonse.core.presentation.contract.UiState
import com.ams.myjeonse.core.notice.presentation.model.NoticeUiModel

object NoticeDetailContract {

    /** 공고 데이터는 Navigation 인자로 이미 전달받았으므로 재조회가 없다. */
    data class State(
        val notice: NoticeUiModel,
    ) : UiState

    sealed interface Action : UiAction {
        data object OpenOriginalClicked : Action
    }

    sealed interface Effect : UiEffect {
        data class OpenUrl(val url: String) : Effect
        data class ShowMessage(@param:StringRes val messageRes: Int) : Effect
    }
}
