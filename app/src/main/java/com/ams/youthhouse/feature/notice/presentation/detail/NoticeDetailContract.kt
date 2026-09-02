package com.ams.youthhouse.feature.notice.presentation.detail

import androidx.annotation.StringRes
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel

object NoticeDetailContract {

    /** 공고 데이터는 Navigation 인자로 이미 전달받았으므로 재조회가 없다. */
    data class State(
        val notice: NoticeUiModel,
        val isFavorite: Boolean = false,
    ) : UiState

    sealed interface Action : UiAction {
        data object OpenOriginalClicked : Action
        data object ApplyClicked : Action
        data object MapClicked : Action
        data object FavoriteClicked : Action
    }

    sealed interface Effect : UiEffect {
        data class OpenUrl(val url: String) : Effect

        /**
         * 외부 지도앱에 주소를 검색어로 넘긴다. geo: URI 조립은 Android 타입이 필요해
         * Route가 맡고, 여기는 주소 문자열만 담는다.
         */
        data class OpenMap(val address: String) : Effect
        data class ShowMessage(@param:StringRes val messageRes: Int) : Effect
    }
}
