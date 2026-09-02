package com.ams.youthhouse.feature.notice.presentation

import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeKey
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState

object NoticeContract {

    /**
     * 목록 데이터와 로딩·에러 상태는 Paging의 `LazyPagingItems`/`LoadState`가 소유한다.
     * 여기엔 목록을 만들어 내는 입력(필터)만 둔다.
     *
     * @param selectedRegion `null`이면 전체 지역
     */
    data class State(
        val selectedRegion: NoticeRegion? = null,
        val selectedCategory: NoticeCategory = NoticeCategory.RENTAL,
        /** 저장된 필터가 도착하기 전인지. 그 전에는 스피너를 조작해도 의미가 없다. */
        val isFilterLoaded: Boolean = false,
        /** 하트를 채울 공고들. 카드가 자기 키의 포함 여부를 본다. */
        val favoriteKeys: Set<FavoriteNoticeKey> = emptySet(),
    ) : UiState

    sealed interface Action : UiAction {
        data class NoticeClicked(val notice: NoticeUiModel) : Action
        data class FavoriteClicked(val notice: NoticeUiModel) : Action
        data class RegionSelected(val region: NoticeRegion?) : Action
        data class CategorySelected(val category: NoticeCategory) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val notice: NoticeUiModel) : Effect
    }
}
