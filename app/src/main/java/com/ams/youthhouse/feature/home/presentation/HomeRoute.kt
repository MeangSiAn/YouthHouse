package com.ams.youthhouse.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.ui.extension.CollectUiEffect
import com.ams.youthhouse.feature.home.presentation.guide.HomeGuide

@Composable
fun HomeRoute(
    onNoticeClick: (NoticeUiModel) -> Unit,
    onSeeAllNoticesClick: () -> Unit,
    onSeeAllScheduleClick: () -> Unit,
    onComplexClick: (String, String) -> Unit,
    onSeeAllComplexesClick: () -> Unit,
    onVisitNoteClick: (kaptCode: String, name: String) -> Unit,
    onCompareVisitsClick: () -> Unit,
    onGuideClick: (HomeGuide) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            is HomeContract.Effect.NavigateToDetail -> onNoticeClick(effect.notice)
            HomeContract.Effect.NavigateToNoticeList -> onSeeAllNoticesClick()
            HomeContract.Effect.NavigateToSchedule -> onSeeAllScheduleClick()
            is HomeContract.Effect.NavigateToComplexDetail ->
                onComplexClick(effect.kaptCode, effect.name)
            HomeContract.Effect.NavigateToTrade -> onSeeAllComplexesClick()
            is HomeContract.Effect.NavigateToVisitNote ->
                onVisitNoteClick(effect.kaptCode, effect.name)
            HomeContract.Effect.NavigateToVisitCompare -> onCompareVisitsClick()
            is HomeContract.Effect.NavigateToGuide -> onGuideClick(effect.guide)
        }
    }

    HomeScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
