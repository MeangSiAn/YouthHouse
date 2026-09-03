package com.ams.youthhouse.feature.trade.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ams.youthhouse.core.presentation.base.BaseViewModel
import com.ams.youthhouse.core.ui.error.toUserMessageRes
import com.ams.youthhouse.feature.trade.domain.model.ComplexDetail
import com.ams.youthhouse.feature.trade.domain.model.FavoriteComplex
import com.ams.youthhouse.feature.trade.domain.model.RecentComplex
import com.ams.youthhouse.feature.trade.domain.repository.ComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.FavoriteComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.RecentComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.SiteVisitNoteRepository
import com.ams.youthhouse.feature.trade.presentation.navigation.ComplexDetailDestination
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplexDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val complexRepository: ComplexRepository,
    private val favoriteComplexRepository: FavoriteComplexRepository,
    private val recentComplexRepository: RecentComplexRepository,
    siteVisitNoteRepository: SiteVisitNoteRepository,
) : BaseViewModel<
    ComplexDetailContract.State,
    ComplexDetailContract.Action,
    ComplexDetailContract.Effect,
    >(
    initialState = savedStateHandle.toRoute<ComplexDetailDestination>().let { destination ->
        ComplexDetailContract.State(
            kaptCode = destination.kaptCode,
            name = destination.name,
        )
    },
) {

    init {
        load()

        favoriteComplexRepository.favoriteCodes
            .onEach { codes -> updateState { copy(isFavorite = currentState.kaptCode in codes) } }
            .launchIn(viewModelScope)

        siteVisitNoteRepository.observe(currentState.kaptCode)
            .onEach { note -> updateState { copy(note = note) } }
            .launchIn(viewModelScope)
    }

    private fun load() {
        updateState { copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            try {
                val detail = complexRepository.getDetail(
                    kaptCode = currentState.kaptCode,
                    months = TREND_MONTHS,
                )
                updateState {
                    copy(
                        isLoading = false,
                        detail = detail,
                        name = detail.name.ifBlank { name },
                        // 세대가 많은(면적 큰) 대표 평형을 기본 선택한다.
                        selectedArea = detail.areas.firstOrNull(),
                    )
                }
                // 조회에 성공한 뒤에 남긴다 — 열리지도 않은 단지를 최근 목록에 올리지 않는다.
                recentComplexRepository.record(
                    RecentComplex(
                        kaptCode = detail.kaptCode,
                        name = detail.name,
                        regionLabel = detail.address.orEmpty(),
                    ),
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                updateState {
                    copy(isLoading = false, errorRes = throwable.toUserMessageRes())
                }
            }
        }
    }

    override fun handleAction(action: ComplexDetailContract.Action) {
        when (action) {
            is ComplexDetailContract.Action.AreaSelected -> {
                updateState { copy(selectedArea = action.area) }
            }

            ComplexDetailContract.Action.FavoriteClicked -> {
                val detail = currentState.detail ?: return
                viewModelScope.launch {
                    favoriteComplexRepository.toggle(
                        FavoriteComplex(
                            kaptCode = detail.kaptCode,
                            name = detail.name,
                            regionLabel = detail.address.orEmpty(),
                        ),
                    )
                }
            }

            ComplexDetailContract.Action.NoteClicked -> {
                val detail = currentState.detail ?: return
                sendEffect(
                    ComplexDetailContract.Effect.NavigateToNote(
                        detail.toNoteDestination(currentState.selectedArea),
                    ),
                )
            }

            ComplexDetailContract.Action.RetryClicked -> load()
        }
    }

    private companion object {
        /** 기획서 스파크 차트가 8포인트 안팎 — 월별 평균 12개면 충분하다. */
        const val TREND_MONTHS = 12
    }
}

/** 노트 편집기에 실어 보낼 단지 요약. 보고 있던 평형의 최근 실거래가 "방문 당시 시세"로 남는다. */
private fun ComplexDetail.toNoteDestination(selectedArea: Double?) = SiteVisitNoteDestination(
    kaptCode = kaptCode,
    name = name,
    regionLabel = address.orEmpty(),
    builtYear = useApprovalDate?.take(YEAR_LENGTH),
    householdCount = householdCount,
    subwayLabel = listOfNotNull(transit.subwayLine, transit.subwayStation, transit.subwayWalkTime)
        .joinToString(separator = " · ")
        .ifBlank { null },
    referenceArea = selectedArea?.toString(),
    referenceAmount = selectedArea?.let { trendsByArea[it]?.latestAmount },
)

private const val YEAR_LENGTH = 4
