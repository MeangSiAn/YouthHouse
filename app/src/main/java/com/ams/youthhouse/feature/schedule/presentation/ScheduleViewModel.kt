package com.ams.youthhouse.feature.schedule.presentation

import androidx.lifecycle.viewModelScope
import com.ams.youthhouse.core.common.time.TodayProvider
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeRepository
import com.ams.youthhouse.core.notice.presentation.model.toUiModel
import com.ams.youthhouse.core.presentation.base.BaseViewModel
import com.ams.youthhouse.feature.schedule.domain.filterUpcomingDeadlines
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val favoriteNoticeRepository: FavoriteNoticeRepository,
    private val todayProvider: TodayProvider,
) : BaseViewModel<
    ScheduleContract.State,
    ScheduleContract.Action,
    ScheduleContract.Effect,
    >(
    initialState = ScheduleContract.State(),
) {

    init {
        favoriteNoticeRepository.favorites
            .onEach { favorites ->
                // 화면이 밤을 넘겨 떠 있는 경우보다 찜 변경이 훨씬 잦으므로
                // "지금"은 emit마다 다시 읽는다. 자정이 지나면 다음 변경 때 따라온다.
                val today = todayProvider.today()
                val upcoming = favorites.filterUpcomingDeadlines(today)
                updateState {
                    copy(
                        isLoading = false,
                        upcoming = upcoming.map { it.toUiModel(today) },
                        // 마감 임박에 오른 공고는 아래 목록에서 뺀다. 같은 카드가 두 번 보이면
                        // 찜이 두 건인지 헷갈리고, 급한 것이 급해 보이지도 않는다.
                        favorites = (favorites - upcoming.toSet()).map { it.toUiModel(today) },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: ScheduleContract.Action) {
        when (action) {
            is ScheduleContract.Action.NoticeClicked -> {
                sendEffect(ScheduleContract.Effect.NavigateToDetail(action.notice))
            }

            // 이 화면에서 하트를 끄면 곧 목록에서 사라진다(전부 찜에서 파생되므로).
            is ScheduleContract.Action.FavoriteClicked -> {
                viewModelScope.launch {
                    favoriteNoticeRepository.toggle(action.notice.source)
                }
            }

            ScheduleContract.Action.BrowseNoticesClicked -> {
                sendEffect(ScheduleContract.Effect.NavigateToNoticeList)
            }
        }
    }
}
