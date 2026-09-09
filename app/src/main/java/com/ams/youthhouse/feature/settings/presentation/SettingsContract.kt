package com.ams.youthhouse.feature.settings.presentation

import com.ams.youthhouse.core.backup.domain.model.BackupSummary
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState

/** 지금 열려 있는 선택 다이얼로그. */
enum class SettingsDialog { NONE, REGION, CATEGORY }

/** 백업 작업 결과. 스낵바 한 줄로 알리고 사라진다. */
sealed interface BackupMessage {
    data class Exported(val summary: BackupSummary) : BackupMessage
    data class Imported(val summary: BackupSummary) : BackupMessage
    data object InvalidFile : BackupMessage
    data object Failed : BackupMessage
}

object SettingsContract {

    data class State(
        val isLoading: Boolean = true,
        val selectedRegion: NoticeRegion? = null,
        val selectedCategory: NoticeCategory = NoticeCategory.RENTAL,
        val versionName: String = "",
        val openDialog: SettingsDialog = SettingsDialog.NONE,
        /** 파일을 읽고 쓰는 동안. 두 번 누르지 못하게 행을 잠근다. */
        val isBackupBusy: Boolean = false,
        val backupMessage: BackupMessage? = null,
    ) : UiState

    sealed interface Action : UiAction {
        data object RegionRowClicked : Action
        data object CategoryRowClicked : Action
        data class RegionSelected(val region: NoticeRegion?) : Action
        data class CategorySelected(val category: NoticeCategory) : Action
        data object DialogDismissed : Action
        data object PrivacyPolicyRowClicked : Action
        data object ExportBackupClicked : Action
        data object ImportBackupClicked : Action
        /** 파일 선택기가 돌려준 위치. 취소하면 `null`. */
        data class BackupTargetPicked(val uri: String?) : Action
        data class BackupSourcePicked(val uri: String?) : Action
        data object BackupMessageShown : Action
    }

    sealed interface Effect : UiEffect {
        /** 앱 밖(브라우저)으로 나가는 일회성 이벤트. 나머지 설정은 저장소 Flow로 되돌아온다. */
        data class OpenUrl(val url: String) : Effect
        /** 시스템 "다른 이름으로 저장" 선택기를 연다. */
        data class PickBackupTarget(val suggestedFileName: String) : Effect
        /** 시스템 파일 열기 선택기를 연다. */
        data object PickBackupSource : Effect
    }
}
