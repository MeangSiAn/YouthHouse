package com.ams.myjeonse.feature.notice.presentation

import com.ams.myjeonse.core.presentation.contract.UiAction
import com.ams.myjeonse.core.presentation.contract.UiEffect
import com.ams.myjeonse.core.presentation.contract.UiState
import com.ams.myjeonse.core.notice.domain.model.NoticeRegion
import com.ams.myjeonse.core.notice.presentation.model.NoticeUiModel

object NoticeContract {

    /**
     * 목록 데이터와 로딩·에러 상태는 Paging의 `LazyPagingItems`/`LoadState`가 소유한다.
     * 여기엔 목록을 만들어 내는 입력(필터)만 둔다.
     *
     * @param selectedRegion `null`이면 전체 지역
     */
    data class State(
        val selectedRegion: NoticeRegion? = null,
        /** 저장된 지역이 도착하기 전인지. 그 전에는 스피너를 조작해도 의미가 없다. */
        val isRegionLoaded: Boolean = false,
    ) : UiState

    sealed interface Action : UiAction {
        data class NoticeClicked(val notice: NoticeUiModel) : Action
        data class RegionSelected(val region: NoticeRegion?) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val notice: NoticeUiModel) : Effect
    }
}
