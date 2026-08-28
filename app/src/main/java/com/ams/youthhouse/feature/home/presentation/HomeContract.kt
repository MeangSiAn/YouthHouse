package com.ams.youthhouse.feature.home.presentation

import androidx.annotation.StringRes
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState
import com.ams.youthhouse.feature.home.presentation.model.HomeSummaryUiModel

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
        data object RetryClicked : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val notice: NoticeUiModel) : Effect
        data object NavigateToNoticeList : Effect
    }
}
