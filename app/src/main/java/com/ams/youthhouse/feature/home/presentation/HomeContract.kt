package com.ams.youthhouse.feature.home.presentation

import androidx.annotation.StringRes
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState
import com.ams.youthhouse.feature.home.presentation.model.HomeSummaryUiModel
import com.ams.youthhouse.feature.trade.domain.model.FavoriteComplex

/** 홈이 그리는 세 가지 화면. 기획서 SCREEN 02 / 10 / 01에 대응한다. */
enum class HomeMode {
    /** 지역 미설정 — 설정 유도 + 전체 최신 공고 몇 건 */
    REGION_UNSET,

    /** 지역은 정했는데 접수중인 공고가 없음 */
    NO_OPEN_NOTICE,

    /** 기본 — 마감임박 · 오늘의 새 공고 · 지역 현황 */
    DEFAULT,
}

object HomeContract {

    data class State(
        /** 저장된 지역이 도착하기 전. 이때는 어떤 모드인지 아직 알 수 없다. */
        val isRegionLoaded: Boolean = false,
        val selectedRegion: NoticeRegion? = null,
        val isLoading: Boolean = false,
        val summary: HomeSummaryUiModel? = null,
        @param:StringRes val errorMessageRes: Int? = null,
        /** 찜한 공고 중 접수중이면서 마감이 가까운 몇 건. 지역 필터와 무관하다. */
        val upcomingFavorites: List<NoticeUiModel> = emptyList(),
        val favoriteNoticeCount: Int = 0,
        /** 관심 단지 몇 곳. 실거래가는 싣지 않는다 — 단지마다 상세를 한 번씩 더 불러야 한다. */
        val favoriteComplexes: List<FavoriteComplex> = emptyList(),
        val favoriteComplexCount: Int = 0,
    ) : UiState {

        val mode: HomeMode
            get() = when {
                selectedRegion == null -> HomeMode.REGION_UNSET
                summary?.hasOpenNotice == false -> HomeMode.NO_OPEN_NOTICE
                else -> HomeMode.DEFAULT
            }
    }

    sealed interface Action : UiAction {
        data class RegionSelected(val region: NoticeRegion?) : Action
        data class NoticeClicked(val notice: NoticeUiModel) : Action
        data object SeeAllNoticesClicked : Action
        data object SeeAllScheduleClicked : Action
        data class ComplexClicked(val complex: FavoriteComplex) : Action
        data object SeeAllComplexesClicked : Action
        data object RetryClicked : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val notice: NoticeUiModel) : Effect
        data object NavigateToNoticeList : Effect
        data object NavigateToSchedule : Effect
        data class NavigateToComplexDetail(val kaptCode: String, val name: String) : Effect
        data object NavigateToTrade : Effect
    }
}
