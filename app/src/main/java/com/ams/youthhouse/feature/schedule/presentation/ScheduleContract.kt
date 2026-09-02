package com.ams.youthhouse.feature.schedule.presentation

import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState

object ScheduleContract {

    /**
     * 두 구간 모두 찜에서 파생된다 — [upcoming]은 접수중이면서 마감이 임박한 것만 추린 것.
     * 별도 저장소가 아니라 같은 데이터의 다른 조명이므로, 항목이 양쪽에 함께 보일 수 있다.
     */
    data class State(
        val isLoading: Boolean = true,
        /** 접수중 + 마감 D-7 이내. 마감이 가까운 순. */
        val upcoming: List<NoticeUiModel> = emptyList(),
        /** 찜 전체. 최근에 찜한 순. */
        val favorites: List<NoticeUiModel> = emptyList(),
    ) : UiState {
        val isEmpty: Boolean get() = !isLoading && favorites.isEmpty()
    }

    sealed interface Action : UiAction {
        data class NoticeClicked(val notice: NoticeUiModel) : Action
        data class FavoriteClicked(val notice: NoticeUiModel) : Action
        data object BrowseNoticesClicked : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val notice: NoticeUiModel) : Effect
        data object NavigateToNoticeList : Effect
    }
}
