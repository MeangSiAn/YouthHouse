package com.ams.myjeonse.feature.notice.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ams.myjeonse.core.common.time.TodayProvider
import com.ams.myjeonse.core.notice.domain.repository.NoticeRepository
import com.ams.myjeonse.core.notice.domain.repository.RegionPreferenceRepository
import com.ams.myjeonse.core.notice.presentation.model.NoticeUiModel
import com.ams.myjeonse.core.notice.presentation.model.toUiModel
import com.ams.myjeonse.core.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoticeViewModel @Inject constructor(
    noticeRepository: NoticeRepository,
    private val regionPreferenceRepository: RegionPreferenceRepository,
    todayProvider: TodayProvider,
) : BaseViewModel<
    NoticeContract.State,
    NoticeContract.Action,
    NoticeContract.Effect,
    >(
    initialState = NoticeContract.State(),
) {

    /**
     * **uiState가 아니라 저장소 Flow에서 직접 파생시킨다.**
     *
     * uiState에서 파생시키면 `initialState`의 `selectedRegion = null` 때문에
     * 저장된 지역이 도착하기 전에 "전체 조회"가 한 번 발사된다.
     * 전체 조회는 384건(약 553KB)이라 낭비가 크고 목록이 한 번 깜빡인다.
     * DataStore Flow는 값이 준비되기 전에는 emit하지 않으므로 여기서 기다린다.
     */
    val noticePagingData: Flow<PagingData<NoticeUiModel>> =
        regionPreferenceRepository.selectedRegion
            .distinctUntilChanged()
            .flatMapLatest { region -> noticeRepository.getNotices(region) }
            // D-day는 "지금"이 있어야 계산된다. Flow 조립 시점에 한 번 읽어 페이지마다 재계산하지 않는다.
            .map { pagingData ->
                val today = todayProvider.today()
                pagingData.map { notice -> notice.toUiModel(today) }
            }
            .cachedIn(viewModelScope)

    init {
        regionPreferenceRepository.selectedRegion
            .distinctUntilChanged()
            .onEach { region ->
                updateState { copy(selectedRegion = region, isRegionLoaded = true) }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: NoticeContract.Action) {
        when (action) {
            is NoticeContract.Action.NoticeClicked -> {
                sendEffect(NoticeContract.Effect.NavigateToDetail(action.notice))
            }

            // 상태를 직접 바꾸지 않는다. 저장소에 쓰면 Flow가 되돌려준다(단방향).
            is NoticeContract.Action.RegionSelected -> {
                viewModelScope.launch {
                    regionPreferenceRepository.setSelectedRegion(action.region)
                }
            }
        }
    }
}
