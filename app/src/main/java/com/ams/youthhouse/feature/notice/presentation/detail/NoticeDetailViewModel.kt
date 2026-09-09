package com.ams.youthhouse.feature.notice.presentation.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ams.youthhouse.R
import com.ams.youthhouse.core.common.time.TodayProvider
import com.ams.youthhouse.core.notice.domain.model.mergeDetail
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeRepository
import com.ams.youthhouse.core.notice.domain.repository.NoticeRepository
import com.ams.youthhouse.core.notice.domain.repository.toFavoriteKey
import com.ams.youthhouse.core.notice.presentation.model.toUiModel
import com.ams.youthhouse.core.presentation.base.BaseViewModel
import com.ams.youthhouse.feature.notice.presentation.navigation.NoticeDetailDestination
import com.ams.youthhouse.feature.notice.presentation.navigation.noticeDetailTypeMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val favoriteNoticeRepository: FavoriteNoticeRepository,
    private val noticeRepository: NoticeRepository,
    private val todayProvider: TodayProvider,
) : BaseViewModel<
    NoticeDetailContract.State,
    NoticeDetailContract.Action,
    NoticeDetailContract.Effect,
    >(
    // NavController가 백스택 인자로 저장해 둔 값을 읽는다.
    // 이 인자는 프로세스 사망 시에도 시스템이 저장/복원하므로 별도 캐시가 필요 없다.
    initialState = NoticeDetailContract.State(
        notice = savedStateHandle
            .toRoute<NoticeDetailDestination>(noticeDetailTypeMap)
            .notice,
    ),
) {

    init {
        val key = currentState.notice.source.toFavoriteKey()
        favoriteNoticeRepository.favoriteKeys
            .onEach { keys -> updateState { copy(isFavorite = key in keys) } }
            .launchIn(viewModelScope)

        loadDetail()
    }

    /**
     * 목록 응답에 없는 필드(주소·문의처·난방방식·잔금)를 단건 조회로 채운다.
     *
     * **실패해도 화면을 오류로 바꾸지 않는다.** 넘겨받은 스냅숏만으로도 공고는 읽히고,
     * 무엇보다 찜한 공고가 마감되면 서버 목록에서 사라져 404가 정상 경로가 된다.
     * 그때 "불러오지 못했습니다"를 띄우면 사용자가 저장해 둔 기록을 잃은 것처럼 보인다.
     */
    private fun loadDetail() {
        val noticeId = currentState.notice.source.noticeId
        if (noticeId.isBlank()) return

        launchCatching(
            onError = { throwable ->
                Log.w(TAG, "공고 상세를 보강하지 못해 목록에서 받은 값으로 표시합니다", throwable)
            },
        ) {
            val detail = noticeRepository.getNotice(noticeId)
            val merged = currentState.notice.source.mergeDetail(detail)
            updateState { copy(notice = merged.toUiModel(todayProvider.today())) }
        }
    }

    private fun openUrl(url: String?) {
        if (url.isNullOrBlank()) {
            sendEffect(NoticeDetailContract.Effect.ShowMessage(R.string.notice_detail_no_url))
        } else {
            sendEffect(NoticeDetailContract.Effect.OpenUrl(url))
        }
    }

    override fun handleAction(action: NoticeDetailContract.Action) {
        when (action) {
            NoticeDetailContract.Action.ApplyClicked -> openUrl(currentState.notice.applyUrl)

            NoticeDetailContract.Action.OpenOriginalClicked -> {
                val url = currentState.notice.detailUrl
                if (url.isNullOrBlank()) {
                    sendEffect(
                        NoticeDetailContract.Effect.ShowMessage(R.string.notice_detail_no_url),
                    )
                } else {
                    sendEffect(NoticeDetailContract.Effect.OpenUrl(url))
                }
            }

            // 링크는 주소가 있을 때만 그려지므로 여기서 null이면 무시해도 된다.
            NoticeDetailContract.Action.MapClicked -> {
                currentState.notice.fullAddress
                    ?.takeIf { it.isNotBlank() }
                    ?.let { sendEffect(NoticeDetailContract.Effect.OpenMap(it)) }
            }

            // 상태를 직접 뒤집지 않는다. DB에 쓰면 init의 favoriteKeys Flow가 되돌려준다.
            NoticeDetailContract.Action.FavoriteClicked -> {
                viewModelScope.launch {
                    favoriteNoticeRepository.toggle(currentState.notice.source)
                }
            }
        }
    }

    private companion object {
        const val TAG = "NoticeDetailViewModel"
    }
}
