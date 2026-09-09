package com.ams.youthhouse.feature.trade.presentation

import androidx.annotation.StringRes
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState
import com.ams.youthhouse.core.complex.domain.model.AptComplex
import com.ams.youthhouse.core.complex.domain.model.FavoriteComplex
import com.ams.youthhouse.core.complex.domain.model.RecentComplex
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination

object TradeContract {

    /**
     * 화면은 두 모드다 — 검색어가 2자 이상이면 검색 결과, 아니면 관심 단지 + 임장노트 목록.
     * 입력 중 상태([query])는 즉시 반영되고, [results]는 디바운스를 거쳐 따라온다.
     */
    data class State(
        val query: String = "",
        val isSearching: Boolean = false,
        val results: List<AptComplex> = emptyList(),
        @param:StringRes val searchErrorRes: Int? = null,
        val favorites: List<FavoriteComplex> = emptyList(),
        /** 검색창 아래 줄. 최근에 연 단지로 한 번에 돌아가는 길이다. */
        val recents: List<RecentComplex> = emptyList(),
        val isFavoritesLoaded: Boolean = false,
        val notes: List<SiteVisitNote> = emptyList(),
    ) : UiState {
        val isSearchMode: Boolean get() = query.trim().length >= MIN_QUERY_LENGTH

        val canCompareNotes: Boolean get() = notes.size >= MIN_NOTES_TO_COMPARE
    }

    sealed interface Action : UiAction {
        data class QueryChanged(val query: String) : Action
        data class ComplexClicked(val kaptCode: String, val name: String) : Action
        data class NoteClicked(val kaptCode: String, val name: String) : Action
        data object CompareClicked : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val kaptCode: String, val name: String) : Effect
        data class NavigateToNote(val destination: SiteVisitNoteDestination) : Effect
        data object NavigateToCompare : Effect
    }

    /** 백엔드가 2자 미만 검색을 거부한다(422). */
    const val MIN_QUERY_LENGTH = 2

    const val MIN_NOTES_TO_COMPARE = 2
}
