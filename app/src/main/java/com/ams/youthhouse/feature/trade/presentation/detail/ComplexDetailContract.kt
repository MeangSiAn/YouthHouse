package com.ams.youthhouse.feature.trade.presentation.detail

import androidx.annotation.StringRes
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState
import com.ams.youthhouse.feature.trade.domain.model.ComplexDetail
import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination

object ComplexDetailContract {

    /**
     * 공고 상세와 달리 재조회가 가능한 화면이다(kaptCode가 전역 고유).
     * 그래서 Navigation 인자는 코드+이름뿐이고 본문은 여기서 로드한다.
     */
    data class State(
        val kaptCode: String,
        /** 로드 전에도 앱바에 띄울 이름. 로드되면 K-apt 공식 명칭으로 대체된다. */
        val name: String,
        val isLoading: Boolean = true,
        @param:StringRes val errorRes: Int? = null,
        val detail: ComplexDetail? = null,
        /** 선택된 전용면적. `null`이면 아직 데이터가 없다. */
        val selectedArea: Double? = null,
        val isFavorite: Boolean = false,
        /** 이 단지에 남긴 임장노트. 없으면 `null`. */
        val note: SiteVisitNote? = null,
    ) : UiState

    sealed interface Action : UiAction {
        data class AreaSelected(val area: Double) : Action
        data object FavoriteClicked : Action
        data object NoteClicked : Action
        data object RetryClicked : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToNote(val destination: SiteVisitNoteDestination) : Effect
    }
}
