package com.ams.youthhouse.feature.settings.presentation

import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState

/** 지금 열려 있는 선택 다이얼로그. */
enum class SettingsDialog { NONE, REGION, CATEGORY }

object SettingsContract {

    data class State(
        val isLoading: Boolean = true,
        val selectedRegion: NoticeRegion? = null,
        val selectedCategory: NoticeCategory = NoticeCategory.RENTAL,
        val versionName: String = "",
        val openDialog: SettingsDialog = SettingsDialog.NONE,
    ) : UiState

    sealed interface Action : UiAction {
        data object RegionRowClicked : Action
        data object CategoryRowClicked : Action
        data class RegionSelected(val region: NoticeRegion?) : Action
        data class CategorySelected(val category: NoticeCategory) : Action
        data object DialogDismissed : Action
        data object PrivacyPolicyRowClicked : Action
    }

    sealed interface Effect : UiEffect {
        /** 앱 밖(브라우저)으로 나가는 일회성 이벤트. 나머지 설정은 저장소 Flow로 되돌아온다. */
        data class OpenUrl(val url: String) : Effect
    }
}
