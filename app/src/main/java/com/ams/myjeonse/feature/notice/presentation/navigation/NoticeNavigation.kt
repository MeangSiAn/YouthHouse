package com.ams.myjeonse.feature.notice.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import com.ams.myjeonse.core.ui.navigation.JsonNavType
import com.ams.myjeonse.feature.notice.presentation.NoticeRoute
import com.ams.myjeonse.feature.notice.presentation.detail.NoticeDetailRoute
import com.ams.myjeonse.core.notice.presentation.model.NoticeUiModel
import kotlinx.serialization.Serializable
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/** 공고 탭의 navigation contract. 다른 feature는 이 타입까지만 참조한다. */
@Serializable
data object NoticeDestination

/**
 * 상세 화면은 공고 데이터를 통째로 인자로 받는다.
 *
 * 이 API에는 상세 조회 오퍼레이션이 없고, 같은 공고가 시군구별로 쪼개져 내려와
 * `pblancId`로도 `(pblancId, houseSn)`으로도 한 행을 특정할 수 없다.
 * 따라서 식별자로 다시 찾는 방식이 성립하지 않는다.
 *
 * 인자는 NavController가 백스택에 저장하므로 프로세스 사망 후에도 그대로 복원된다.
 */
@Serializable
data class NoticeDetailDestination(
    val notice: NoticeUiModel,
)

internal val noticeDetailTypeMap: Map<KType, NavType<*>> = mapOf(
    typeOf<NoticeUiModel>() to JsonNavType(NoticeUiModel.serializer()),
)

fun NavGraphBuilder.noticeScreen(
    onNoticeClick: (NoticeUiModel) -> Unit,
) {
    composable<NoticeDestination> {
        NoticeRoute(onNoticeClick = onNoticeClick)
    }
}

/**
 * 상세는 [NoticeDestination]의 중첩 그래프가 아니라 형제 목적지로 둔다.
 * 중첩하면 `NavDestination.hierarchy`에 공고 탭이 포함되어 바텀바가 계속 보인다.
 */
fun NavGraphBuilder.noticeDetailScreen(
    onBackClick: () -> Unit,
) {
    composable<NoticeDetailDestination>(typeMap = noticeDetailTypeMap) {
        NoticeDetailRoute(onBackClick = onBackClick)
    }
}

fun NavController.navigateToNoticeDetail(notice: NoticeUiModel) {
    navigate(NoticeDetailDestination(notice))
}
