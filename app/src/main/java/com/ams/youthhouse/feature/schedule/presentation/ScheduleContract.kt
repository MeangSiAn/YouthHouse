package com.ams.youthhouse.feature.schedule.presentation

import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState

object ScheduleContract {

    /**
     * 두 구간 모두 찜에서 파생된다 — [upcoming]은 접수중이면서 마감이 임박한 것만 추린 것이고,
     * [favorites]는 그것을 뺀 나머지다. 한 공고는 한 구간에만 보인다.
     */
    data class State(
        val isLoading: Boolean = true,
        /** 접수중 + 마감 D-7 이내. 마감이 가까운 순. */
        val upcoming: List<NoticeUiModel> = emptyList(),
        /** [upcoming]에 오르지 않은 찜. 최근에 찜한 순. */
        val favorites: List<NoticeUiModel> = emptyList(),
    ) : UiState {
        val isEmpty: Boolean get() = !isLoading && upcoming.isEmpty() && favorites.isEmpty()
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
