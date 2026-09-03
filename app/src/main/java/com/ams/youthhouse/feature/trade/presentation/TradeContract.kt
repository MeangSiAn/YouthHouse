package com.ams.youthhouse.feature.trade.presentation

import androidx.annotation.StringRes
import com.ams.youthhouse.feature.trade.domain.model.AptComplex
import com.ams.youthhouse.feature.trade.domain.model.FavoriteComplex
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState

object TradeContract {

    /**
     * 화면은 두 모드다 — 검색어가 2자 이상이면 검색 결과, 아니면 관심 단지 목록.
     * 입력 중 상태([query])는 즉시 반영되고, [results]는 디바운스를 거쳐 따라온다.
     */
    data class State(
        val query: String = "",
        val isSearching: Boolean = false,
        val results: List<AptComplex> = emptyList(),
        @param:StringRes val searchErrorRes: Int? = null,
        val favorites: List<FavoriteComplex> = emptyList(),
        val isFavoritesLoaded: Boolean = false,
    ) : UiState {
        val isSearchMode: Boolean get() = query.trim().length >= MIN_QUERY_LENGTH
    }

    sealed interface Action : UiAction {
        data class QueryChanged(val query: String) : Action
        data class ComplexClicked(val kaptCode: String, val name: String) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val kaptCode: String, val name: String) : Effect
    }

    /** 백엔드가 2자 미만 검색을 거부한다(422). */
    const val MIN_QUERY_LENGTH = 2
}
