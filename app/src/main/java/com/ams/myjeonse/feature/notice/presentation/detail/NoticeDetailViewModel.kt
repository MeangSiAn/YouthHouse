package com.ams.myjeonse.feature.notice.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.ams.myjeonse.R
import com.ams.myjeonse.core.presentation.base.BaseViewModel
import com.ams.myjeonse.feature.notice.presentation.navigation.NoticeDetailDestination
import com.ams.myjeonse.feature.notice.presentation.navigation.noticeDetailTypeMap
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
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

    override fun handleAction(action: NoticeDetailContract.Action) {
        when (action) {
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
        }
    }
}
