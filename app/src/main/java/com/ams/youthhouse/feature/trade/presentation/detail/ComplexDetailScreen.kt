package com.ams.youthhouse.feature.trade.presentation.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.common.format.formatManwonAsEokMan
import com.ams.youthhouse.core.common.format.formatThousands
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.component.SummaryEntry
import com.ams.youthhouse.core.designsystem.component.SummaryGrid
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.feature.trade.domain.model.AreaTrend
import com.ams.youthhouse.feature.trade.domain.model.ComplexDeal
import com.ams.youthhouse.feature.trade.domain.model.ComplexDetail
import com.ams.youthhouse.feature.trade.domain.model.TrendPoint
import com.ams.youthhouse.feature.trade.presentation.component.SparkBars

/**
 * 기획서 dev2.0 SCREEN 08 — 단지 상세.
 *
 * 기본정보(K-apt) + 평형별 실거래 추이(국토교통부)를 자체 백엔드가 합쳐 준 것을 그린다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplexDetailScreen(
    uiState: ComplexDetailContract.State,
    onAction: (ComplexDetailContract.Action) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = uiState.name)
                        uiState.detail?.address?.let { address ->
                            Text(
                                text = address,
                                style = AppTextStyles.monoCaption,
                                color = AppTheme.semanticColors.ink45,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.notice_detail_back),
                        )
                    }
                },
                actions = {
                    // 로드 전에는 저장할 지역 정보가 없어 하트를 숨긴다.
                    if (uiState.detail != null) {
                        IconButton(
                            onClick = { onAction(ComplexDetailContract.Action.FavoriteClicked) },
                        ) {
                            Icon(
                                imageVector = if (uiState.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = stringResource(
                                    if (uiState.isFavorite) {
                                        R.string.notice_favorite_remove
                                    } else {
                                        R.string.notice_favorite_add
                                    },
                                ),
                                tint = if (uiState.isFavorite) {
                                    AppTheme.semanticColors.close
                                } else {
                                    AppTheme.semanticColors.ink45
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            uiState.errorRes != null -> Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyContent(title = stringResource(uiState.errorRes)) {
                    TextButton(
                        onClick = { onAction(ComplexDetailContract.Action.RetryClicked) },
                    ) {
                        Text(text = stringResource(R.string.retry))
                    }
                }
            }

            uiState.detail != null -> DetailContent(
                detail = uiState.detail,
                selectedArea = uiState.selectedArea,
                onAction = onAction,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: ComplexDetail,
    selectedArea: Double?,
    onAction: (ComplexDetailContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        SummaryGrid(entries = detail.toSummaryEntries())

        if (detail.areas.isEmpty()) {
            EmptyContent(
                title = stringResource(R.string.complex_no_deals_title),
                description = detail.dealsNote,
            )
        } else {
            SectionHeader(
                title = stringResource(R.string.complex_section_trend),
                trailingText = stringResource(R.string.complex_trend_window),
            )
            AreaChips(
                areas = detail.areas,
                selectedArea = selectedArea,
                onAreaSelected = { onAction(ComplexDetailContract.Action.AreaSelected(it)) },
            )
            selectedArea?.let { area ->
                detail.trendsByArea[area]?.let { trend -> AreaTrendBlock(trend) }
            }
        }

        FootnoteText(text = stringResource(R.string.trade_footnote))
    }
}

@Composable
private fun AreaChips(
    areas: List<Double>,
    selectedArea: Double?,
    onAreaSelected: (Double) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        areas.forEach { area ->
            FilterChip(
                selected = area == selectedArea,
                onClick = { onAreaSelected(area) },
                label = { Text(text = area.formatArea()) },
            )
        }
    }
}

@Composable
private fun AreaTrendBlock(trend: AreaTrend) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.complex_latest_deal),
                    style = AppTextStyles.monoCaption,
                    color = AppTheme.semanticColors.ink45,
                )
                Text(
                    text = trend.latestAmount?.formatManwonAsEokMan()
                        ?: stringResource(R.string.complex_no_deals_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppTheme.semanticColors.ink,
                )
            }
            trend.change?.let { change -> ChangeLabel(change) }
        }

        // 점이 두 개 이하면 "추이"가 아니라 그냥 값이다. 통짜 막대가 화면만 잡아먹으므로
        // 아래 거래 목록으로 충분하다고 보고 차트를 접는다.
        if (trend.monthlyAverages.size >= MIN_TREND_POINTS) {
            SparkBars(values = trend.monthlyAverages.map(TrendPoint::averageAmount))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = trend.monthlyAverages.first().yearMonth.formatYearMonth(),
                    style = AppTextStyles.monoCaption,
                    color = AppTheme.semanticColors.ink45,
                )
                Text(
                    text = trend.monthlyAverages.last().yearMonth.formatYearMonth(),
                    style = AppTextStyles.monoCaption,
                    color = AppTheme.semanticColors.ink45,
                )
            }
        }

        SectionHeader(
            title = stringResource(R.string.complex_section_recent_deals),
            trailingText = pluralStringResource(
                R.plurals.complex_deal_count,
                trend.dealCount,
                trend.dealCount,
            ),
        )
        trend.deals.take(MAX_DEAL_ROWS).forEachIndexed { index, deal ->
            DealRow(deal)
            if (index != trend.deals.take(MAX_DEAL_ROWS).lastIndex) {
                HorizontalDivider(
                    thickness = AppSize.border,
                    color = AppTheme.semanticColors.line2,
                )
            }
        }
    }
}

@Composable
private fun ChangeLabel(change: Long) {
    val (arrow, color) = when {
        change > 0 -> "▲" to AppTheme.semanticColors.close
        change < 0 -> "▼" to MaterialTheme.colorScheme.primary
        else -> "―" to AppTheme.semanticColors.ink45
    }
    Text(
        text = "$arrow ${kotlin.math.abs(change).formatManwonAsEokMan()}",
        style = AppTextStyles.mono,
        color = color,
    )
}

@Composable
private fun DealRow(deal: ComplexDeal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = deal.date.orEmpty(),
                style = AppTextStyles.mono,
                color = AppTheme.semanticColors.ink,
            )
            deal.floor?.let { floor ->
                Text(
                    text = stringResource(R.string.complex_floor, floor),
                    style = AppTextStyles.monoCaption,
                    color = AppTheme.semanticColors.ink45,
                )
            }
        }
        Text(
            text = deal.amount.formatManwonAsEokMan(),
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.semanticColors.ink,
        )
    }
}

@Composable
private fun ComplexDetail.toSummaryEntries(): List<SummaryEntry> = listOfNotNull(
    useApprovalDate?.take(4)?.let {
        SummaryEntry(
            label = stringResource(R.string.complex_built),
            value = stringResource(R.string.complex_built_year, it),
        )
    },
    householdCount?.formatThousands()?.let {
        SummaryEntry(
            label = stringResource(R.string.complex_households),
            value = stringResource(R.string.notice_unit_household, it),
        )
    },
    dongCount?.let {
        SummaryEntry(
            label = stringResource(R.string.complex_dongs),
            value = stringResource(R.string.complex_dong_count, it),
        )
    },
    heatingName?.let {
        SummaryEntry(label = stringResource(R.string.complex_heating), value = it)
    },
)

/** `59.58` → `59.58㎡`, `85.0` → `85㎡` */
private fun Double.formatArea(): String {
    val text = if (this % 1.0 == 0.0) toInt().toString() else toString()
    return "$text㎡"
}

/** `202604` → `26.04` */
private fun String.formatYearMonth(): String =
    if (length == 6) "${substring(2, 4)}.${substring(4, 6)}" else this

private const val MAX_DEAL_ROWS = 5
private const val MIN_TREND_POINTS = 3

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ComplexDetailScreenPreview() {
    AppTheme {
        ComplexDetailScreen(
            uiState = ComplexDetailContract.State(
                kaptCode = "A15105302",
                name = "관악푸르지오",
                isLoading = false,
                isFavorite = true,
                selectedArea = 59.58,
                detail = ComplexDetail(
                    kaptCode = "A15105302",
                    name = "관악푸르지오",
                    address = "서울특별시 관악구 봉천동",
                    useApprovalDate = "20040830",
                    householdCount = 2104,
                    dongCount = "12",
                    heatingName = "개별난방",
                    constructorName = "대우건설",
                    areas = listOf(84.9, 59.58),
                    trendsByArea = mapOf(
                        59.58 to AreaTrend(
                            latestAmount = 121500,
                            latestDate = "2026-08-29",
                            latestFloor = 21,
                            change = 9960,
                            dealCount = 34,
                            monthlyAverages = listOf(
                                TrendPoint("202604", 111540),
                                TrendPoint("202605", 113900),
                                TrendPoint("202606", 114500),
                                TrendPoint("202607", 116781),
                                TrendPoint("202608", 121500),
                            ),
                            deals = listOf(
                                ComplexDeal("2026-08-29", 21, 121500),
                                ComplexDeal("2026-07-31", 1, 108000),
                            ),
                        ),
                    ),
                    dealsNote = null,
                ),
            ),
            onAction = {},
            onBackClick = {},
        )
    }
}
