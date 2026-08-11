package com.ams.myjeonse.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.ams.myjeonse.core.common.time.TodayProvider
import com.ams.myjeonse.core.notice.domain.model.NoticeRegion
import com.ams.myjeonse.core.notice.domain.repository.NoticeRepository
import com.ams.myjeonse.core.notice.domain.repository.RegionPreferenceRepository
import com.ams.myjeonse.core.presentation.base.BaseViewModel
import com.ams.myjeonse.core.ui.error.toUserMessageRes
import com.ams.myjeonse.feature.home.domain.toHomeSummary
import com.ams.myjeonse.feature.home.presentation.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val regionPreferenceRepository: RegionPreferenceRepository,
    private val todayProvider: TodayProvider,
) : BaseViewModel<
    HomeContract.State,
    HomeContract.Action,
    HomeContract.Effect,
    >(
    initialState = HomeContract.State(),
) {

    private var loadJob: Job? = null

    init {
        regionPreferenceRepository.selectedRegion
            .distinctUntilChanged()
            .onEach { region -> load(region) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: HomeContract.Action) {
        when (action) {
            is HomeContract.Action.RegionSelected -> {
                // 상태를 직접 바꾸지 않는다. 저장소에 쓰면 Flow가 되돌려주고 재조회가 걸린다.
                viewModelScope.launch {
                    regionPreferenceRepository.setSelectedRegion(action.region)
                }
            }

            is HomeContract.Action.NoticeClicked -> {
                sendEffect(HomeContract.Effect.NavigateToDetail(action.notice))
            }

            HomeContract.Action.SeeAllNoticesClicked -> {
                sendEffect(HomeContract.Effect.NavigateToNoticeList)
            }

            HomeContract.Action.RetryClicked -> load(currentState.selectedRegion)
        }
    }

    private fun load(region: NoticeRegion?) {
        loadJob?.cancel()

        updateState {
            copy(
                isRegionLoaded = true,
                selectedRegion = region,
                isLoading = true,
                errorMessageRes = null,
            )
        }

        // 지역을 고르면 그 지역 전 건을 받아 클라이언트에서 계산한다(최대 76건, 약 106KB).
        // 지역이 없으면 전체 384건(약 553KB)이라 받을 수 없으므로 맛보기 몇 건만 받는다.
        val maxCount = if (region == null) UNSET_REGION_PREVIEW_COUNT else REGION_SNAPSHOT_MAX_COUNT

        loadJob = launchCatching(
            onError = { throwable ->
                updateState {
                    copy(isLoading = false, errorMessageRes = throwable.toUserMessageRes())
                }
            },
        ) {
            val today = todayProvider.today()
            val notices = noticeRepository.getNoticeSnapshot(region, maxCount)
            val summary = notices.toHomeSummary(today).toUiModel(today)

            updateState { copy(isLoading = false, summary = summary) }
        }
    }

    private companion object {
        /** 지역 단위 최대가 76건이라 이 값이면 전 건을 덮는다. */
        const val REGION_SNAPSHOT_MAX_COUNT = 500
        const val UNSET_REGION_PREVIEW_COUNT = 3
    }
}
