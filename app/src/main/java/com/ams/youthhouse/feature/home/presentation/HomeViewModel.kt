package com.ams.youthhouse.feature.home.presentation

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ams.youthhouse.core.common.time.TodayProvider
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory.RENTAL
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory.SALE
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.repository.NoticeFilterRepository
import com.ams.youthhouse.core.notice.domain.repository.NoticeRepository
import com.ams.youthhouse.core.presentation.base.BaseViewModel
import com.ams.youthhouse.core.ui.error.toUserMessageRes
import com.ams.youthhouse.feature.home.domain.toHomeSummary
import com.ams.youthhouse.feature.home.presentation.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val noticeFilterRepository: NoticeFilterRepository,
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
        noticeFilterRepository.selectedRegion
            .distinctUntilChanged()
            .onEach { region -> load(region) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: HomeContract.Action) {
        when (action) {
            is HomeContract.Action.RegionSelected -> {
                // 상태를 직접 바꾸지 않는다. 저장소에 쓰면 Flow가 되돌려주고 재조회가 걸린다.
                viewModelScope.launch {
                    noticeFilterRepository.setSelectedRegion(action.region)
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

        // 지역을 고르면 그 지역 전 건을 받아 클라이언트에서 계산한다(임대 최대 76건, 분양 최대 27건).
        // 지역이 없으면 임대만 384건(약 553KB)이라 받을 수 없으므로 맛보기 몇 건만 받는다.
        val maxCount = if (region == null) UNSET_REGION_PREVIEW_COUNT else REGION_SNAPSHOT_MAX_COUNT

        loadJob = launchCatching(
            onError = { throwable ->
                updateState {
                    copy(isLoading = false, errorMessageRes = throwable.toUserMessageRes())
                }
            },
        ) {
            val today = todayProvider.today()
            val notices = fetchAllCategories(region, maxCount)
            val summary = notices.toHomeSummary(today).toUiModel(today)

            updateState { copy(isLoading = false, summary = summary) }
        }
    }

    /**
     * 임대와 분양을 병렬로 받아 합친다.
     *
     * **한쪽만 실패하면 성공한 쪽으로 진행한다.** 분양은 63건짜리 부가 정보라
     * 그것 하나 때문에 홈 전체를 막을 이유가 없다. 다만 조용히 넘기지 않도록 로그를 남긴다.
     * 둘 다 실패하면 예외를 던져 [launchCatching]의 에러 화면으로 간다.
     */
    private suspend fun fetchAllCategories(
        region: NoticeRegion?,
        maxCount: Int,
    ): List<Notice> = coroutineScope {
        // 임대는 주 상품이라 실패하면 화면을 세우지 않는다(예외를 그대로 올려 에러 UI로).
        val rental = async { noticeRepository.getNoticeSnapshot(RENTAL, region, maxCount) }

        // 분양은 부가 정보라 실패해도 임대만으로 진행한다.
        val sale = async {
            try {
                noticeRepository.getNoticeSnapshot(SALE, region, maxCount)
            } catch (exception: CancellationException) {
                // runCatching을 쓰면 취소까지 삼켜 loadJob.cancel()이 무력화된다.
                throw exception
            } catch (throwable: Throwable) {
                Log.w(TAG, "공공분양 공고를 불러오지 못해 공공임대만 표시합니다", throwable)
                null
            }
        }

        rental.await() + sale.await().orEmpty()
    }

    private companion object {
        const val TAG = "HomeViewModel"

        /** 지역 단위 최대가 임대 76건 + 분양 27건이라 이 값이면 전 건을 덮는다. */
        const val REGION_SNAPSHOT_MAX_COUNT = 500
        /**
         * 지역 미설정 화면의 맛보기 건수.
         *
         * 3으로 두면 상위 3행이 같은 공고의 단지별 분할일 때(분양 1158은 7행)
         * 중복 제거 후 카드가 1장만 남는다. 행당 약 1.4KB라 넉넉히 받아 거른다.
         */
        const val UNSET_REGION_PREVIEW_COUNT = 10
    }
}
