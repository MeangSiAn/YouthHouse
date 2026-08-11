package com.ams.myjeonse.feature.home.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.myjeonse.R
import com.ams.myjeonse.core.designsystem.component.EmptyContent
import com.ams.myjeonse.core.designsystem.component.FootnoteText
import com.ams.myjeonse.core.designsystem.component.SectionHeader
import com.ams.myjeonse.core.designsystem.theme.AppSpacing
import com.ams.myjeonse.core.designsystem.theme.AppTheme
import com.ams.myjeonse.core.notice.presentation.component.NoticeItemCard
import com.ams.myjeonse.core.notice.presentation.component.NoticeRegionSpinner
import com.ams.myjeonse.core.notice.presentation.model.NoticeStatus
import com.ams.myjeonse.core.notice.presentation.model.NoticeUiModel
import com.ams.myjeonse.core.notice.presentation.model.previewNoticeUiModel
import com.ams.myjeonse.feature.home.presentation.component.NoOpenNoticeGuide
import com.ams.myjeonse.feature.home.presentation.component.RegionStatusBar
import com.ams.myjeonse.feature.home.presentation.component.RegionUnsetBanner
import com.ams.myjeonse.feature.home.presentation.component.UrgentDeadlineCard
import com.ams.myjeonse.feature.home.presentation.model.HomeSummaryUiModel
import com.ams.myjeonse.feature.home.presentation.model.UrgentNoticeUiModel

@Composable
fun HomeScreen(
    uiState: HomeContract.State,
    onAction: (HomeContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            NoticeRegionSpinner(
                selectedRegion = uiState.selectedRegion,
                onRegionSelected = { region ->
                    onAction(HomeContract.Action.RegionSelected(region))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        when {
            !uiState.isRegionLoaded || uiState.isLoading -> item { LoadingBlock() }

            uiState.errorMessageRes != null -> item {
                ErrorBlock(
                    messageRes = uiState.errorMessageRes,
                    onRetryClick = { onAction(HomeContract.Action.RetryClicked) },
                )
            }

            else -> homeContent(uiState = uiState, onAction = onAction)
        }
    }
}

private fun LazyListScope.homeContent(
    uiState: HomeContract.State,
    onAction: (HomeContract.Action) -> Unit,
) {
    val summary = uiState.summary ?: return

    when (uiState.mode) {
        HomeMode.REGION_UNSET -> {
            item {
                RegionUnsetBanner(
                    onSelectRegionClick = { onAction(HomeContract.Action.SeeAllNoticesClicked) },
                )
            }
            // 지역 미설정이면 몇 건만 받아오므로 "오늘 올라온" 조건을 걸면 대부분 비게 된다.
            // 기획서 EMP-01처럼 둘러볼 거리를 남기기 위해 최근 공고를 그대로 보여준다.
            noticeSection(
                titleRes = R.string.home_section_recent_notice,
                notices = summary.recentNotices,
                totalCount = null,
                onAction = onAction,
            )
        }

        HomeMode.NO_OPEN_NOTICE -> item {
            NoOpenNoticeGuide(
                regionName = uiState.selectedRegion?.regionName.orEmpty(),
                nextOpenDate = summary.nextOpenDate,
                onChangeRegionClick = { onAction(HomeContract.Action.RegionSelected(null)) },
                onSeeAllClick = { onAction(HomeContract.Action.SeeAllNoticesClicked) },
            )
        }

        HomeMode.DEFAULT -> {
            summary.urgent?.let { urgent ->
                item { UrgentBlock(urgent = urgent, onAction = onAction) }
            }
            noticeSection(
                titleRes = R.string.home_section_new_notice,
                notices = summary.todayNotices,
                totalCount = summary.todayNoticeTotalCount,
                onAction = onAction,
            )
            item {
                SectionHeader(
                    title = stringResource(R.string.home_section_region_status),
                    trailingText = uiState.selectedRegion?.regionName,
                )
            }
            item {
                RegionStatusBar(
                    openCount = summary.openCount,
                    upcomingCount = summary.upcomingCount,
                    closingTodayCount = summary.closingTodayCount,
                )
            }
        }
    }

    item { FootnoteText(text = stringResource(R.string.home_footnote)) }
}

private fun LazyListScope.noticeSection(
    @StringRes titleRes: Int,
    notices: List<NoticeUiModel>,
    totalCount: Int?,
    onAction: (HomeContract.Action) -> Unit,
) {
    item {
        SectionHeader(
            title = stringResource(titleRes),
            trailingText = totalCount?.let { stringResource(R.string.home_section_count, it) },
        )
    }

    if (notices.isEmpty()) {
        item { EmptyContent(title = stringResource(R.string.home_new_notice_empty)) }
        return
    }

    items(notices.size) { index ->
        val notice = notices[index]
        NoticeItemCard(
            notice = notice,
            onClick = { onAction(HomeContract.Action.NoticeClicked(notice)) },
        )
    }
}

@Composable
private fun UrgentBlock(
    urgent: UrgentNoticeUiModel,
    onAction: (HomeContract.Action) -> Unit,
) {
    UrgentDeadlineCard(
        daysLeft = urgent.daysLeft,
        title = urgent.notice.title,
        deadlineText = urgent.deadlineDate
            ?.let { stringResource(R.string.home_urgent_deadline, it) }
            .orEmpty(),
        onClick = { onAction(HomeContract.Action.NoticeClicked(urgent.notice)) },
    )
}

@Composable
private fun LoadingBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xxxl),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBlock(
    messageRes: Int,
    onRetryClick: () -> Unit,
) {
    EmptyContent(title = stringResource(messageRes)) {
        TextButton(onClick = onRetryClick) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenDefaultPreview() {
    AppTheme {
        HomeScreen(uiState = previewState(), onAction = {})
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenRegionUnsetPreview() {
    AppTheme {
        HomeScreen(
            uiState = previewState().copy(selectedRegion = null),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenNoOpenNoticePreview() {
    AppTheme {
        HomeScreen(
            uiState = previewState().copy(
                summary = previewSummary().copy(hasOpenNotice = false, openCount = 0),
            ),
            onAction = {},
        )
    }
}

private fun previewState() = HomeContract.State(
    isRegionLoaded = true,
    selectedRegion = com.ams.myjeonse.core.notice.domain.model.NoticeRegion.SEOUL,
    summary = previewSummary(),
)

private fun previewSummary() = HomeSummaryUiModel(
    urgent = UrgentNoticeUiModel(
        notice = previewNoticeUiModel(),
        daysLeft = 2,
        deadlineDate = "2026.08.05",
    ),
    todayNotices = listOf(
        previewNoticeUiModel(),
        previewNoticeUiModel(
            title = "서울공릉 신혼희망타운 행복주택",
            status = NoticeStatus.UPCOMING,
            statusLabel = "D-16 시작",
            supplyTypeName = "행복주택",
            regionName = "서울특별시 노원구",
        ),
    ),
    todayNoticeTotalCount = 2,
    recentNotices = listOf(previewNoticeUiModel()),
    openCount = 6,
    upcomingCount = 4,
    closingTodayCount = 1,
    nextOpenDate = "2026.08.16",
    hasOpenNotice = true,
)
