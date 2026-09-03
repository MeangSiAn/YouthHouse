package com.ams.youthhouse.feature.trade.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ams.youthhouse.feature.trade.presentation.TradeRoute
import com.ams.youthhouse.feature.trade.presentation.compare.VisitCompareRoute
import com.ams.youthhouse.feature.trade.presentation.detail.ComplexDetailRoute
import com.ams.youthhouse.feature.trade.presentation.note.SiteVisitNoteRoute
import kotlinx.serialization.Serializable

/** 매매 탭의 navigation contract. 다른 feature는 이 타입까지만 참조한다. */
@Serializable
data object TradeDestination

/**
 * 공고 상세와 달리 모델 전체를 싣지 않는다 — [kaptCode]로 재조회가 가능하기 때문.
 * [name]은 로드 전 앱바에 잠깐 보여줄 표시용이다.
 */
@Serializable
data class ComplexDetailDestination(
    val kaptCode: String,
    val name: String,
)

/**
 * 임장노트 편집.
 *
 * 새 노트는 단지 상세에서만 열리므로 그 화면이 이미 가진 단지 요약을 같이 싣는다 —
 * 노트 하나 쓰자고 상세를 다시 내려받지 않기 위해서다. 이미 있는 노트를 목록에서 열 때는
 * 코드와 이름만 있으면 되고, 나머지는 저장된 값을 쓴다.
 */
@Serializable
data class SiteVisitNoteDestination(
    val kaptCode: String,
    val name: String,
    val regionLabel: String = "",
    val builtYear: String? = null,
    val householdCount: Int? = null,
    val subwayLabel: String? = null,
    /** Double 인자는 라우트 직렬화 지원이 버전마다 달라 문자열로 싣는다. */
    val referenceArea: String? = null,
    val referenceAmount: Long? = null,
)

@Serializable
data object VisitCompareDestination

fun NavGraphBuilder.tradeScreen(
    onComplexClick: (kaptCode: String, name: String) -> Unit,
    onNoteClick: (SiteVisitNoteDestination) -> Unit,
    onCompareClick: () -> Unit,
) {
    composable<TradeDestination> {
        TradeRoute(
            onComplexClick = onComplexClick,
            onNoteClick = onNoteClick,
            onCompareClick = onCompareClick,
        )
    }
}

fun NavGraphBuilder.complexDetailScreen(
    onBackClick: () -> Unit,
    onNoteClick: (SiteVisitNoteDestination) -> Unit,
) {
    composable<ComplexDetailDestination> {
        ComplexDetailRoute(onBackClick = onBackClick, onNoteClick = onNoteClick)
    }
}

fun NavGraphBuilder.siteVisitNoteScreen(
    onBackClick: () -> Unit,
) {
    composable<SiteVisitNoteDestination> {
        SiteVisitNoteRoute(onBackClick = onBackClick)
    }
}

fun NavGraphBuilder.visitCompareScreen(
    onBackClick: () -> Unit,
    onNoteClick: (SiteVisitNoteDestination) -> Unit,
) {
    composable<VisitCompareDestination> {
        VisitCompareRoute(onBackClick = onBackClick, onNoteClick = onNoteClick)
    }
}

fun NavController.navigateToComplexDetail(kaptCode: String, name: String) {
    navigate(ComplexDetailDestination(kaptCode = kaptCode, name = name))
}

fun NavController.navigateToSiteVisitNote(destination: SiteVisitNoteDestination) {
    navigate(destination)
}

fun NavController.navigateToVisitCompare() {
    navigate(VisitCompareDestination)
}
