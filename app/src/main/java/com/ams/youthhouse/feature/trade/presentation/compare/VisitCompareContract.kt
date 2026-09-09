package com.ams.youthhouse.feature.trade.presentation.compare

import com.ams.youthhouse.core.presentation.contract.UiAction
import com.ams.youthhouse.core.presentation.contract.UiEffect
import com.ams.youthhouse.core.presentation.contract.UiState
import com.ams.youthhouse.core.complex.domain.VisitComparison
import com.ams.youthhouse.core.complex.domain.compareVisits
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination

object VisitCompareContract {

    /**
     * 임장노트가 있는 단지 중 골라서 나란히 본다.
     *
     * 폰 화면에 열 세 개가 한계라 [MAX_SELECTION]으로 막는다. 고른 순서를 지켜야
     * 사용자가 "첫 번째 열에 둔 단지"를 기준으로 읽을 수 있으므로 Set이 아니라 List다.
     */
    data class State(
        val notes: List<SiteVisitNote> = emptyList(),
        val isLoaded: Boolean = false,
        val selectedCodes: List<String> = emptyList(),
    ) : UiState {
        val selectedNotes: List<SiteVisitNote>
            get() = selectedCodes.mapNotNull { code -> notes.firstOrNull { it.kaptCode == code } }

        val comparison: VisitComparison
            get() = selectedNotes.compareVisits()

        val hasEnoughNotes: Boolean
            get() = notes.size >= MIN_NOTES

        val hasEnoughSelection: Boolean
            get() = selectedNotes.size >= MIN_NOTES
    }

    sealed interface Action : UiAction {
        data class SelectionToggled(val kaptCode: String) : Action
        data class NoteClicked(val kaptCode: String, val name: String) : Action
    }

    sealed interface Effect : UiEffect {
        data class NavigateToNote(val destination: SiteVisitNoteDestination) : Effect
    }

    const val MIN_NOTES = 2
    const val MAX_SELECTION = 3
}
