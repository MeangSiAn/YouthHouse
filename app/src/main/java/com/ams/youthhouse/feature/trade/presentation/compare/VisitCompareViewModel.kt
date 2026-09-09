package com.ams.youthhouse.feature.trade.presentation.compare

import androidx.lifecycle.viewModelScope
import com.ams.youthhouse.core.presentation.base.BaseViewModel
import com.ams.youthhouse.core.complex.domain.repository.SiteVisitNoteRepository
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class VisitCompareViewModel @Inject constructor(
    noteRepository: SiteVisitNoteRepository,
) : BaseViewModel<
    VisitCompareContract.State,
    VisitCompareContract.Action,
    VisitCompareContract.Effect,
    >(
    initialState = VisitCompareContract.State(),
) {

    init {
        noteRepository.notes
            .onEach { notes ->
                updateState {
                    val codes = notes.map { it.kaptCode }
                    copy(
                        notes = notes,
                        isLoaded = true,
                        // 처음 열면 최근 것부터 셋을 골라 둔다. 빈 표를 보여주고 고르라고 하지 않는다.
                        // 이후에는 지워진 단지만 빼고 사용자의 선택을 지킨다.
                        selectedCodes = if (isLoaded) {
                            selectedCodes.filter { it in codes }
                        } else {
                            codes.take(VisitCompareContract.MAX_SELECTION)
                        },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: VisitCompareContract.Action) {
        when (action) {
            is VisitCompareContract.Action.SelectionToggled -> updateState {
                copy(selectedCodes = selectedCodes.toggled(action.kaptCode))
            }

            is VisitCompareContract.Action.NoteClicked -> sendEffect(
                VisitCompareContract.Effect.NavigateToNote(
                    SiteVisitNoteDestination(kaptCode = action.kaptCode, name = action.name),
                ),
            )
        }
    }

    /** 꽉 찬 상태에서 하나 더 고르면 가장 먼저 고른 것이 빠진다 — 막아 버리면 바꿀 방법이 없다. */
    private fun List<String>.toggled(kaptCode: String): List<String> = when {
        kaptCode in this -> this - kaptCode
        size < VisitCompareContract.MAX_SELECTION -> this + kaptCode
        else -> drop(1) + kaptCode
    }
}
