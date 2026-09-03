package com.ams.youthhouse.feature.trade.presentation.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ams.youthhouse.core.common.time.TodayProvider
import com.ams.youthhouse.core.presentation.base.BaseViewModel
import com.ams.youthhouse.feature.trade.domain.model.ComplexSnapshot
import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.domain.repository.SiteVisitNoteRepository
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SiteVisitNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: SiteVisitNoteRepository,
    private val todayProvider: TodayProvider,
) : BaseViewModel<
    SiteVisitNoteContract.State,
    SiteVisitNoteContract.Action,
    SiteVisitNoteContract.Effect,
    >(
    initialState = savedStateHandle.toRoute<SiteVisitNoteDestination>().toInitialState(),
) {

    init {
        viewModelScope.launch {
            // 편집 중인 초안이 DB 갱신에 덮이면 안 되므로 계속 구독하지 않고 한 번만 읽는다.
            val existing = noteRepository.observe(currentState.kaptCode).first()
            updateState {
                if (existing == null) {
                    copy(isLoaded = true, visitedOn = todayProvider.today())
                } else {
                    fromExisting(existing)
                }
            }
        }
    }

    override fun handleAction(action: SiteVisitNoteContract.Action) {
        when (action) {
            SiteVisitNoteContract.Action.DatePickerOpened ->
                updateState { copy(isDatePickerShown = true) }

            SiteVisitNoteContract.Action.DatePickerDismissed ->
                updateState { copy(isDatePickerShown = false) }

            is SiteVisitNoteContract.Action.VisitedOnPicked ->
                updateState { copy(visitedOn = action.yyyyMmDd, isDatePickerShown = false) }

            is SiteVisitNoteContract.Action.ViewedUnitChanged ->
                updateState { copy(viewedUnit = action.text) }

            is SiteVisitNoteContract.Action.RatingChanged ->
                updateState { copy(ratings = ratings.with(action.criterion, action.score)) }

            is SiteVisitNoteContract.Action.WalkMinutesChanged ->
                updateState {
                    copy(walkMinutesText = action.text.filter(Char::isDigit).take(MAX_WALK_DIGITS))
                }

            is SiteVisitNoteContract.Action.DefectChanged ->
                updateState { copy(defectStatus = action.status) }

            is SiteVisitNoteContract.Action.MemoChanged ->
                updateState { copy(memo = action.text) }

            SiteVisitNoteContract.Action.SaveClicked -> save()

            SiteVisitNoteContract.Action.DeleteClicked ->
                updateState { copy(isDeleteDialogShown = true) }

            SiteVisitNoteContract.Action.DeleteDismissed ->
                updateState { copy(isDeleteDialogShown = false) }

            SiteVisitNoteContract.Action.DeleteConfirmed -> delete()
        }
    }

    private fun save() {
        val state = currentState
        if (!state.canSave) return
        viewModelScope.launch {
            noteRepository.save(state.toNote())
            sendEffect(SiteVisitNoteContract.Effect.NavigateBack)
        }
    }

    private fun delete() {
        viewModelScope.launch {
            noteRepository.delete(currentState.kaptCode)
            sendEffect(SiteVisitNoteContract.Effect.NavigateBack)
        }
    }

    private companion object {
        /** 세 자리면 충분하다. 역까지 999분 넘게 걷는 단지는 없다. */
        const val MAX_WALK_DIGITS = 3
    }
}

private fun SiteVisitNoteDestination.toInitialState() = SiteVisitNoteContract.State(
    kaptCode = kaptCode,
    complexName = name,
    regionLabel = regionLabel,
    snapshot = ComplexSnapshot(
        builtYear = builtYear,
        householdCount = householdCount,
        subwayLabel = subwayLabel,
        referenceArea = referenceArea?.toDoubleOrNull(),
        referenceAmount = referenceAmount,
    ),
)

private fun SiteVisitNoteContract.State.fromExisting(note: SiteVisitNote) = copy(
    isLoaded = true,
    isExisting = true,
    regionLabel = note.regionLabel.ifBlank { regionLabel },
    // 상세에서 다시 열었다면 그 시점 요약이 더 새롭다. 목록에서 열었다면(빈 요약) 저장된 것을 쓴다.
    snapshot = if (snapshot.isEmpty) note.snapshot else snapshot,
    visitedOn = note.visitedOn,
    viewedUnit = note.viewedUnit,
    ratings = note.ratings,
    walkMinutesText = note.walkToStationMinutes?.toString().orEmpty(),
    defectStatus = note.defectStatus,
    memo = note.memo,
)

private fun SiteVisitNoteContract.State.toNote() = SiteVisitNote(
    kaptCode = kaptCode,
    complexName = complexName,
    regionLabel = regionLabel,
    visitedOn = visitedOn,
    viewedUnit = viewedUnit.trim(),
    ratings = ratings,
    walkToStationMinutes = walkMinutes,
    defectStatus = defectStatus,
    memo = memo.trim(),
    snapshot = snapshot,
    // 수정 시각은 저장소가 찍는다.
    updatedAtMillis = 0L,
)
