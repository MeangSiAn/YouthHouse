package com.ams.youthhouse.feature.trade.presentation.note

import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState
import com.ams.youthhouse.feature.trade.domain.model.ComplexSnapshot
import com.ams.youthhouse.feature.trade.domain.model.DefectStatus
import com.ams.youthhouse.feature.trade.domain.model.VisitCriterion
import com.ams.youthhouse.feature.trade.domain.model.VisitRatings

object SiteVisitNoteContract {

    /**
     * 편집 중인 초안. 저장 전까지 DB에 닿지 않는다.
     *
     * [walkMinutesText]는 입력 원문이다 — 숫자로 바로 바꾸면 지우는 도중 빈 칸을 표현할 수 없다.
     */
    data class State(
        val kaptCode: String,
        val complexName: String,
        val regionLabel: String,
        val snapshot: ComplexSnapshot,
        val isLoaded: Boolean = false,
        /** 이미 저장된 노트를 고치는 중인가. 삭제 버튼과 저장 버튼 문구가 갈린다. */
        val isExisting: Boolean = false,
        /** `YYYYMMDD` */
        val visitedOn: String = "",
        val viewedUnit: String = "",
        val ratings: VisitRatings = VisitRatings(),
        val walkMinutesText: String = "",
        val defectStatus: DefectStatus = DefectStatus.UNCHECKED,
        val memo: String = "",
        val isDatePickerShown: Boolean = false,
        val isDeleteDialogShown: Boolean = false,
    ) : UiState {
        val walkMinutes: Int?
            get() = walkMinutesText.toIntOrNull()

        /** 빈 노트는 저장하지 않는다 — 항목 하나라도 남겨야 기록이다. */
        val canSave: Boolean
            get() = isLoaded && (
                !ratings.isEmpty ||
                    memo.isNotBlank() ||
                    walkMinutes != null ||
                    defectStatus != DefectStatus.UNCHECKED ||
                    viewedUnit.isNotBlank()
                )
    }

    sealed interface Action : UiAction {
        data object DatePickerOpened : Action
        data object DatePickerDismissed : Action
        data class VisitedOnPicked(val yyyyMmDd: String) : Action
        data class ViewedUnitChanged(val text: String) : Action
        data class RatingChanged(val criterion: VisitCriterion, val score: Int?) : Action
        data class WalkMinutesChanged(val text: String) : Action
        data class DefectChanged(val status: DefectStatus) : Action
        data class MemoChanged(val text: String) : Action
        data object SaveClicked : Action
        data object DeleteClicked : Action
        data object DeleteConfirmed : Action
        data object DeleteDismissed : Action
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
    }
}
