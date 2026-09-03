package com.ams.youthhouse.feature.trade.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ams.youthhouse.feature.trade.presentation.TradeRoute
import com.ams.youthhouse.feature.trade.presentation.detail.ComplexDetailRoute
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

fun NavGraphBuilder.tradeScreen(
    onComplexClick: (kaptCode: String, name: String) -> Unit,
) {
    composable<TradeDestination> {
        TradeRoute(onComplexClick = onComplexClick)
    }
}

fun NavGraphBuilder.complexDetailScreen(
    onBackClick: () -> Unit,
) {
    composable<ComplexDetailDestination> {
        ComplexDetailRoute(onBackClick = onBackClick)
    }
}

fun NavController.navigateToComplexDetail(kaptCode: String, name: String) {
    navigate(ComplexDetailDestination(kaptCode = kaptCode, name = name))
}
