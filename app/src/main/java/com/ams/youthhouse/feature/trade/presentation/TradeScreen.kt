package com.ams.youthhouse.feature.trade.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.feature.trade.domain.model.AptComplex
import com.ams.youthhouse.feature.trade.domain.model.ComplexSnapshot
import com.ams.youthhouse.feature.trade.domain.model.DefectStatus
import com.ams.youthhouse.feature.trade.domain.model.FavoriteComplex
import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.domain.model.VisitCriterion
import com.ams.youthhouse.feature.trade.domain.model.VisitRatings
import com.ams.youthhouse.feature.trade.presentation.component.SiteVisitNoteCard

/**
 * 기획서 dev2.0 SCREEN 04 — 매매.
 *
 * 검색어가 2자 이상이면 검색 결과, 아니면 관심 단지 목록을 보여준다.
 * 검색은 자체 백엔드의 단지명 인덱스를 쓴다(공공 API 직접 호출 아님).
 */
@Composable
fun TradeScreen(
    uiState: TradeContract.State,
    onAction: (TradeContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Text(
                text = stringResource(R.string.trade_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.semanticColors.ink,
            )
        }

        item {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { onAction(TradeContract.Action.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(R.string.trade_search_hint)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(AppRadius.card),
            )
        }

        if (uiState.isSearchMode) {
            searchResults(uiState, onAction)
        } else {
            favoriteComplexes(uiState, onAction)
            siteVisitNotes(uiState, onAction)
        }

        item { FootnoteText(text = stringResource(R.string.trade_footnote)) }
    }
}

/**
 * 기획서 SCREEN 04 하단 "내 임장노트". 노트는 단지 상세에서만 쓸 수 있어 여기엔
 * 쓰기 버튼이 없고, 대신 둘 이상 모이면 비교 진입점이 생긴다.
 */
private fun LazyListScope.siteVisitNotes(
    uiState: TradeContract.State,
    onAction: (TradeContract.Action) -> Unit,
) {
    item {
        SectionHeader(
            title = stringResource(R.string.note_section_mine),
            trailingText = pluralStringResource(
                R.plurals.trade_result_count,
                uiState.notes.size,
                uiState.notes.size,
            ).takeIf { uiState.notes.isNotEmpty() },
        )
    }

    if (uiState.notes.isEmpty()) {
        item {
            Text(
                text = stringResource(R.string.note_list_hint),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.semanticColors.ink45,
            )
        }
        return
    }

    items(uiState.notes, key = { it.kaptCode }) { note ->
        SiteVisitNoteCard(
            note = note,
            onClick = {
                onAction(
                    TradeContract.Action.NoteClicked(
                        kaptCode = note.kaptCode,
                        name = note.complexName,
                    ),
                )
            },
        )
    }

    if (uiState.canCompareNotes) {
        item {
            OutlinedButton(
                onClick = { onAction(TradeContract.Action.CompareClicked) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadius.button),
            ) {
                Text(text = stringResource(R.string.note_compare))
            }
        }
    }
}

private fun LazyListScope.searchResults(
    uiState: TradeContract.State,
    onAction: (TradeContract.Action) -> Unit,
) {
    item {
        SectionHeader(
            title = stringResource(R.string.trade_section_results),
            trailingText = pluralStringResource(
                R.plurals.trade_result_count,
                uiState.results.size,
                uiState.results.size,
            ).takeUnless { uiState.isSearching },
        )
    }

    when {
        uiState.isSearching -> item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.xxl),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            }
        }

        uiState.searchErrorRes != null -> item {
            EmptyContent(title = stringResource(uiState.searchErrorRes))
        }

        uiState.results.isEmpty() -> item {
            EmptyContent(title = stringResource(R.string.trade_search_empty))
        }

        else -> item {
            // K-apt는 300세대 이상 의무관리 단지 위주라 빌라·소형 단지는 없을 수 있다.
            ComplexRowGroup {
                uiState.results.forEachIndexed { index, complex ->
                    ComplexRow(
                        name = complex.name,
                        regionLabel = complex.regionLabel,
                        onClick = {
                            onAction(
                                TradeContract.Action.ComplexClicked(
                                    kaptCode = complex.kaptCode,
                                    name = complex.name,
                                ),
                            )
                        },
                    )
                    if (index != uiState.results.lastIndex) {
                        HorizontalDivider(
                            thickness = AppSize.border,
                            color = AppTheme.semanticColors.line2,
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.favoriteComplexes(
    uiState: TradeContract.State,
    onAction: (TradeContract.Action) -> Unit,
) {
    item {
        SectionHeader(
            title = stringResource(R.string.trade_section_favorites),
            trailingText = pluralStringResource(
                R.plurals.trade_result_count,
                uiState.favorites.size,
                uiState.favorites.size,
            ).takeIf { uiState.favorites.isNotEmpty() },
        )
    }

    if (uiState.isFavoritesLoaded && uiState.favorites.isEmpty()) {
        item {
            EmptyContent(
                title = stringResource(R.string.trade_favorites_empty_title),
                description = stringResource(R.string.trade_favorites_empty_description),
            )
        }
        return
    }

    item {
        ComplexRowGroup {
            uiState.favorites.forEachIndexed { index, favorite ->
                ComplexRow(
                    name = favorite.name,
                    regionLabel = favorite.regionLabel,
                    onClick = {
                        onAction(
                            TradeContract.Action.ComplexClicked(
                                kaptCode = favorite.kaptCode,
                                name = favorite.name,
                            ),
                        )
                    },
                )
                if (index != uiState.favorites.lastIndex) {
                    HorizontalDivider(
                        thickness = AppSize.border,
                        color = AppTheme.semanticColors.line2,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplexRowGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(AppSize.border, AppTheme.semanticColors.line),
                shape = RoundedCornerShape(AppRadius.card),
            ),
    ) {
        content()
    }
}

@Composable
private fun ComplexRow(
    name: String,
    regionLabel: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md + AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.semanticColors.ink,
        )
        if (regionLabel.isNotBlank()) {
            Text(
                text = regionLabel,
                style = AppTextStyles.monoCaption,
                color = AppTheme.semanticColors.ink45,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun TradeScreenFavoritesPreview() {
    AppTheme {
        TradeScreen(
            uiState = TradeContract.State(
                isFavoritesLoaded = true,
                favorites = listOf(
                    FavoriteComplex("A15105302", "관악푸르지오", "서울특별시 관악구 봉천동"),
                    FavoriteComplex("A68134004", "다운동아", "울산광역시 중구 다운동"),
                ),
                notes = listOf(
                    SiteVisitNote(
                        kaptCode = "A15105302",
                        complexName = "관악푸르지오",
                        regionLabel = "서울특별시 관악구 봉천동",
                        visitedOn = "20260720",
                        viewedUnit = "84㎡ · 12층",
                        ratings = VisitRatings(mapOf(VisitCriterion.LIGHT to 4)),
                        walkToStationMinutes = 8,
                        defectStatus = DefectStatus.NONE,
                        memo = "남향 채광 좋음.",
                        snapshot = ComplexSnapshot.EMPTY,
                        updatedAtMillis = 0L,
                    ),
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun TradeScreenSearchPreview() {
    AppTheme {
        TradeScreen(
            uiState = TradeContract.State(
                query = "관악",
                results = listOf(
                    AptComplex("A15303203", "관악우방", "서울특별시 금천구 시흥동"),
                    AptComplex("A15105302", "관악푸르지오", "서울특별시 관악구 봉천동"),
                ),
            ),
            onAction = {},
        )
    }
}
