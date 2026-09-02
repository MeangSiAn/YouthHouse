package com.ams.youthhouse.feature.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.presentation.component.NoticeItemCard
import com.ams.youthhouse.core.notice.presentation.model.NoticeStatus
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.notice.presentation.model.previewNoticeUiModel

/**
 * 기획서 dev2.0 SCREEN 03 — 내 일정.
 *
 * 기획서의 3구간(다가오는 일정 / 찜한 공고 / 지원 이력) 중 데이터가 실재하는
 * 두 구간만 만든다. 지원 이력은 "신청함" 수동 체크 기능이 생겨야 성립한다.
 */
@Composable
fun ScheduleScreen(
    uiState: ScheduleContract.State,
    onAction: (ScheduleContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        uiState.isEmpty -> ScheduleEmpty(
            onBrowseClick = { onAction(ScheduleContract.Action.BrowseNoticesClicked) },
            modifier = modifier,
        )

        else -> ScheduleContent(uiState = uiState, onAction = onAction, modifier = modifier)
    }
}

@Composable
private fun ScheduleContent(
    uiState: ScheduleContract.State,
    onAction: (ScheduleContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Text(
                text = stringResource(R.string.schedule_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.semanticColors.ink,
            )
        }

        if (uiState.upcoming.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.schedule_section_upcoming),
                    trailingText = stringResource(R.string.schedule_section_upcoming_window),
                )
            }
            noticeCards(
                notices = uiState.upcoming,
                keyPrefix = "upcoming",
                onAction = onAction,
            )
        }

        item {
            SectionHeader(
                title = stringResource(R.string.schedule_section_favorites),
                trailingText = pluralStringResource(
                    R.plurals.schedule_favorite_count,
                    uiState.favorites.size,
                    uiState.favorites.size,
                ),
            )
        }
        noticeCards(
            notices = uiState.favorites,
            keyPrefix = "favorite",
            onAction = onAction,
        )

        item { FootnoteText(text = stringResource(R.string.schedule_footnote)) }
    }
}

private fun LazyListScope.noticeCards(
    notices: List<NoticeUiModel>,
    keyPrefix: String,
    onAction: (ScheduleContract.Action) -> Unit,
) {
    // 찜 목록은 (category, pblancId)가 고유해 안정 키를 줄 수 있다.
    // 하트를 꺼서 항목이 빠질 때 아래 카드들이 자연스럽게 따라 올라온다.
    items(
        count = notices.size,
        key = { index ->
            val n = notices[index].source
            "$keyPrefix:${n.category.name}:${n.pblancId}"
        },
    ) { index ->
        val notice = notices[index]
        NoticeItemCard(
            notice = notice,
            onClick = { onAction(ScheduleContract.Action.NoticeClicked(notice)) },
            isFavorite = true,
            onFavoriteClick = { onAction(ScheduleContract.Action.FavoriteClicked(notice)) },
        )
    }
}

@Composable
private fun ScheduleEmpty(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        EmptyContent(
            title = stringResource(R.string.schedule_empty_title),
            description = stringResource(R.string.schedule_empty_description),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextButton(onClick = onBrowseClick) {
                Text(text = stringResource(R.string.schedule_empty_action))
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun ScheduleScreenPreview() {
    AppTheme {
        ScheduleScreen(
            uiState = ScheduleContract.State(
                isLoading = false,
                upcoming = listOf(previewNoticeUiModel()),
                favorites = listOf(
                    previewNoticeUiModel(),
                    // 미리보기의 LazyColumn 키가 (category, pblancId)라 분야를 갈라 중복을 피한다.
                    previewNoticeUiModel(
                        category = NoticeCategory.SALE,
                        title = "울산다운2 A-9 신혼희망타운",
                        status = NoticeStatus.OPEN,
                        statusLabel = "D-126",
                    ),
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun ScheduleScreenEmptyPreview() {
    AppTheme {
        ScheduleScreen(
            uiState = ScheduleContract.State(isLoading = false),
            onAction = {},
        )
    }
}
