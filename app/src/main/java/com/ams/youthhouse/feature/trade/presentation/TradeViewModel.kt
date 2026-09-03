package com.ams.youthhouse.feature.trade.presentation

import androidx.lifecycle.viewModelScope
import com.ams.youthhouse.core.presentation.base.BaseViewModel
import com.ams.youthhouse.core.ui.error.toUserMessageRes
import com.ams.youthhouse.feature.trade.domain.repository.ComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.FavoriteComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.RecentComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.SiteVisitNoteRepository
import com.ams.youthhouse.feature.trade.presentation.navigation.SiteVisitNoteDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class TradeViewModel @Inject constructor(
    private val complexRepository: ComplexRepository,
    favoriteComplexRepository: FavoriteComplexRepository,
    recentComplexRepository: RecentComplexRepository,
    siteVisitNoteRepository: SiteVisitNoteRepository,
) : BaseViewModel<
    TradeContract.State,
    TradeContract.Action,
    TradeContract.Effect,
    >(
    initialState = TradeContract.State(),
) {

    // uiState와 별도로 두는 이유: 디바운스는 "입력 스트림"에 걸어야 한다.
    // uiState에서 파생시키면 관심 단지 변경 같은 무관한 갱신에도 검색이 다시 돈다.
    private val queryInput = MutableStateFlow("")

    init {
        favoriteComplexRepository.favorites
            .onEach { favorites ->
                updateState { copy(favorites = favorites, isFavoritesLoaded = true) }
            }
            .launchIn(viewModelScope)

        recentComplexRepository.recents
            .onEach { recents -> updateState { copy(recents = recents) } }
            .launchIn(viewModelScope)

        siteVisitNoteRepository.notes
            .onEach { notes -> updateState { copy(notes = notes) } }
            .launchIn(viewModelScope)

        queryInput
            .debounce(SEARCH_DEBOUNCE_MILLIS)
            .distinctUntilChanged()
            .mapLatest { query -> query to searchOrNull(query) }
            .onEach { (query, outcome) ->
                // 디바운스 사이에 입력이 더 들어왔다면 이 결과는 낡았다. 버린다.
                if (query != currentState.query.trim()) return@onEach
                updateState {
                    copy(
                        isSearching = false,
                        results = outcome?.getOrNull().orEmpty(),
                        searchErrorRes = outcome?.exceptionOrNull()?.toUserMessageRes(),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /** 2자 미만이면 검색하지 않는다(`null`). 결과/실패는 Result로 접어 상태 갱신 한 곳에서 푼다. */
    private suspend fun searchOrNull(query: String) =
        if (query.length < TradeContract.MIN_QUERY_LENGTH) {
            null
        } else {
            try {
                Result.success(complexRepository.search(query))
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                Result.failure(throwable)
            }
        }

    override fun handleAction(action: TradeContract.Action) {
        when (action) {
            is TradeContract.Action.QueryChanged -> {
                val trimmed = action.query.trim()
                updateState {
                    copy(
                        query = action.query,
                        isSearching = trimmed.length >= TradeContract.MIN_QUERY_LENGTH,
                        searchErrorRes = null,
                    )
                }
                queryInput.value = trimmed
            }

            is TradeContract.Action.ComplexClicked -> {
                sendEffect(TradeContract.Effect.NavigateToDetail(action.kaptCode, action.name))
            }

            is TradeContract.Action.NoteClicked -> {
                sendEffect(
                    TradeContract.Effect.NavigateToNote(
                        SiteVisitNoteDestination(kaptCode = action.kaptCode, name = action.name),
                    ),
                )
            }

            TradeContract.Action.CompareClicked -> {
                sendEffect(TradeContract.Effect.NavigateToCompare)
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
