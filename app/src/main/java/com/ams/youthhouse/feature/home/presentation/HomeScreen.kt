package com.ams.youthhouse.feature.home.presentation

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
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.component.BlockLabel
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.component.SettingsRow
import com.ams.youthhouse.core.designsystem.component.SettingsRowGroup
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.notice.presentation.component.NoticeItemCard
import com.ams.youthhouse.core.notice.presentation.component.NoticeRegionSpinner
import com.ams.youthhouse.core.notice.presentation.model.NoticeStatus
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.notice.presentation.model.previewNoticeUiModel
import com.ams.youthhouse.feature.home.presentation.component.NoOpenNoticeGuide
import com.ams.youthhouse.feature.home.presentation.component.RegionStatusBar
import com.ams.youthhouse.feature.home.presentation.component.RegionUnsetBanner
import com.ams.youthhouse.feature.home.presentation.component.UrgentDeadlineCard
import com.ams.youthhouse.feature.home.presentation.model.HomeSummaryUiModel
import com.ams.youthhouse.feature.home.presentation.model.UrgentNoticeUiModel

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

    // 기획서 H-01은 홈을 2블록(공고 알림 / 임장기록)으로 잡았지만, 임장노트 기능이 없어
    // 두 번째 블록은 비워 둔 채 "매물 등록" 버튼만 놓여 있었다. 그 버튼은 준비 중 화면으로
    // 이어지는 막다른 길이라 블록째로 걷어냈다. 기능이 생기면 되살린다.
    noticeBlock(uiState = uiState, summary = summary, onAction = onAction)

    // 아래 두 블록은 지역 필터와 무관하다 — 찜과 관심 단지는 사용자가 직접 고른 것이라
    // 지역을 바꿨다고 사라지면 안 된다. 그래서 noticeBlock 바깥에 둔다.
    scheduleSection(uiState = uiState, onAction = onAction)
    favoriteComplexSection(uiState = uiState, onAction = onAction)

    item { FootnoteText(text = stringResource(R.string.home_footnote)) }
}

/**
 * 공고 알림 블록 — 시간이 지나면 사라지는 것들.
 *
 * 기획서는 이 블록을 "전세알림"이라 부르지만 그대로 쓰지 않는다.
 * 실제로 여기 실리는 건 전세임대(약 21%)뿐 아니라 매입임대·행복주택·국민임대 같은
 * 월세 기반 공고와 공공분양(소유권 취득)까지다. 라벨이 내용을 좁게 말하면 안 된다.
 */
private fun LazyListScope.noticeBlock(
    uiState: HomeContract.State,
    summary: HomeSummaryUiModel,
    onAction: (HomeContract.Action) -> Unit,
) {
    item { BlockLabel(text = stringResource(R.string.home_block_notice)) }

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
                // 이 숫자는 임대와 분양을 합쳐 센 값이다. 지역명만 두면 무엇의 합계인지
                // 드러나지 않아 목록에 섞여 보이는 분양과 어긋나 보인다.
                SectionHeader(
                    title = stringResource(R.string.home_section_region_status),
                    trailingText = stringResource(R.string.home_section_region_status_scope),
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
}

/**
 * 내 일정 — 찜한 공고 중 마감이 가까운 것 몇 건.
 *
 * 찜이 없거나 전부 마감됐으면 섹션째로 감춘다. 빈 카드를 두면 홈이 길어지기만 하고,
 * 찜하는 법은 공고 탭에서 이미 안내한다.
 */
private fun LazyListScope.scheduleSection(
    uiState: HomeContract.State,
    onAction: (HomeContract.Action) -> Unit,
) {
    if (uiState.upcomingFavorites.isEmpty()) return

    item {
        SectionHeader(
            title = stringResource(R.string.home_section_schedule),
            trailingText = stringResource(
                R.string.home_section_schedule_more,
                uiState.favoriteNoticeCount,
            ),
            onTrailingClick = { onAction(HomeContract.Action.SeeAllScheduleClicked) },
        )
    }

    item {
        SettingsRowGroup {
            uiState.upcomingFavorites.forEachIndexed { index, notice ->
                SettingsRow(
                    title = notice.title,
                    description = notice.supplyInstitutionName,
                    value = notice.statusLabel,
                    onClick = { onAction(HomeContract.Action.NoticeClicked(notice)) },
                    showDivider = index != uiState.upcomingFavorites.lastIndex,
                )
            }
        }
    }
}

/**
 * 관심 단지 — 매매 탭에서 하트를 누른 단지.
 *
 * 기획서는 여기에 최근 실거래가까지 얹지만 싣지 않는다. 시세는 단지마다 상세를
 * 한 번씩 더 불러야 나오는 값이라, 홈을 여는 것만으로 관심 단지 수만큼 요청이 나간다.
 */
private fun LazyListScope.favoriteComplexSection(
    uiState: HomeContract.State,
    onAction: (HomeContract.Action) -> Unit,
) {
    if (uiState.favoriteComplexes.isEmpty()) return

    item {
        SectionHeader(
            title = stringResource(R.string.home_section_favorite_complex),
            trailingText = stringResource(
                R.string.home_section_favorite_complex_more,
                uiState.favoriteComplexCount,
            ),
            onTrailingClick = { onAction(HomeContract.Action.SeeAllComplexesClicked) },
        )
    }

    item {
        SettingsRowGroup {
            uiState.favoriteComplexes.forEachIndexed { index, complex ->
                SettingsRow(
                    title = complex.name,
                    description = complex.regionLabel.ifBlank { null },
                    onClick = { onAction(HomeContract.Action.ComplexClicked(complex)) },
                    showDivider = index != uiState.favoriteComplexes.lastIndex,
                )
            }
        }
    }
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
            trailingText = stringResource(
                R.string.home_section_see_all,
                totalCount ?: notices.size,
            ),
            onTrailingClick = { onAction(HomeContract.Action.SeeAllNoticesClicked) },
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
    selectedRegion = com.ams.youthhouse.core.notice.domain.model.NoticeRegion.SEOUL,
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
