package com.ams.youthhouse.feature.notice.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ams.youthhouse.core.common.time.TodayProvider
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeRepository
import com.ams.youthhouse.core.notice.domain.repository.NoticeFilterRepository
import com.ams.youthhouse.core.notice.domain.repository.NoticeRepository
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.notice.presentation.model.toUiModel
import com.ams.youthhouse.core.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class NoticeFilter(
    val category: NoticeCategory,
    val region: NoticeRegion?,
    val status: NoticeStatusFilter,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoticeViewModel @Inject constructor(
    noticeRepository: NoticeRepository,
    private val noticeFilterRepository: NoticeFilterRepository,
    private val favoriteNoticeRepository: FavoriteNoticeRepository,
    todayProvider: TodayProvider,
) : BaseViewModel<
    NoticeContract.State,
    NoticeContract.Action,
    NoticeContract.Effect,
    >(
    initialState = NoticeContract.State(),
) {

    private val filter: Flow<NoticeFilter> = combine(
        noticeFilterRepository.selectedCategory,
        noticeFilterRepository.selectedRegion,
        noticeFilterRepository.selectedStatusFilter,
    ) { category, region, status -> NoticeFilter(category, region, status) }
        .distinctUntilChanged()

    /**
     * **uiState가 아니라 저장소 Flow에서 직접 파생시킨다.**
     *
     * uiState에서 파생시키면 `initialState`의 기본 필터로 조회가 한 번 발사된 뒤
     * 저장된 필터가 도착해 다시 조회된다. 요청이 두 번 나가고 목록이 깜빡인다.
     * DataStore Flow는 값이 준비되기 전에는 emit하지 않으므로 여기서 기다린다.
     */
    val noticePagingData: Flow<PagingData<NoticeUiModel>> = filter
        .flatMapLatest { (category, region, status) ->
            // 세 조건 모두 서버가 거른다. 받아 온 페이지를 앱이 다시 거르지 않으므로
            // 페이지가 절반쯤 비어 스크롤이 헛도는 일이 없다.
            noticeRepository.getNotices(category, region, status)
                // D-day는 "지금"이 있어야 계산된다. Flow 조립 시점에 한 번 읽어 페이지마다 재계산하지 않는다.
                .map { pagingData ->
                    val today = todayProvider.today()
                    pagingData.map { notice -> notice.toUiModel(today) }
                }
        }
        .cachedIn(viewModelScope)

    init {
        filter
            .onEach { (category, region, status) ->
                updateState {
                    copy(
                        selectedCategory = category,
                        selectedRegion = region,
                        selectedStatus = status,
                        isFilterLoaded = true,
                    )
                }
            }
            .launchIn(viewModelScope)

        favoriteNoticeRepository.favoriteKeys
            .onEach { keys -> updateState { copy(favoriteKeys = keys) } }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: NoticeContract.Action) {
        when (action) {
            is NoticeContract.Action.NoticeClicked -> {
                sendEffect(NoticeContract.Effect.NavigateToDetail(action.notice))
            }

            // 하트도 단방향이다 — DB에 쓰면 favoriteKeys Flow가 되돌려준다.
            is NoticeContract.Action.FavoriteClicked -> {
                viewModelScope.launch {
                    favoriteNoticeRepository.toggle(action.notice.source)
                }
            }

            // 상태를 직접 바꾸지 않는다. 저장소에 쓰면 Flow가 되돌려준다(단방향).
            is NoticeContract.Action.RegionSelected -> {
                viewModelScope.launch {
                    noticeFilterRepository.setSelectedRegion(action.region)
                }
            }

            is NoticeContract.Action.CategorySelected -> {
                viewModelScope.launch {
                    noticeFilterRepository.setSelectedCategory(action.category)
                }
            }

            is NoticeContract.Action.StatusSelected -> {
                viewModelScope.launch {
                    noticeFilterRepository.setSelectedStatusFilter(action.status)
                }
            }
        }
    }
}
