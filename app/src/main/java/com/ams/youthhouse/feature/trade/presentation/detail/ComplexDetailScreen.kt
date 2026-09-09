package com.ams.youthhouse.feature.trade.presentation.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.common.format.formatManwonAsEokMan
import com.ams.youthhouse.core.common.format.formatThousands
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.component.SummaryEntry
import com.ams.youthhouse.core.designsystem.component.SummaryGrid
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.complex.domain.model.AreaBucket
import com.ams.youthhouse.core.complex.domain.model.AreaBucketKind
import com.ams.youthhouse.core.complex.domain.model.AreaTrend
import com.ams.youthhouse.core.complex.domain.model.BuildingInfo
import com.ams.youthhouse.core.complex.domain.model.ComplexDeal
import com.ams.youthhouse.core.complex.domain.model.ComplexDetail
import com.ams.youthhouse.core.complex.domain.model.FacilityGroup
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.Surroundings
import com.ams.youthhouse.core.complex.domain.model.TransitInfo
import com.ams.youthhouse.core.complex.domain.model.TrendPoint
import com.ams.youthhouse.feature.trade.presentation.component.DealTrendChart
import com.ams.youthhouse.feature.trade.presentation.component.FacilityChip
import com.ams.youthhouse.feature.trade.presentation.component.InfoRow
import com.ams.youthhouse.feature.trade.presentation.component.SiteVisitNoteCard
import com.ams.youthhouse.feature.trade.presentation.component.formatArea

/**
 * 기획서 dev2.0 SCREEN 08 — 단지 상세.
 *
 * 구성 순서는 "고를 때 보는 순서"를 따른다.
 * 얼마에 팔렸나(실거래) → 어떤 집인가(평형·건물) → 어떻게 다니나(교통) → 주변에 뭐가 있나.
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
                        Text(
                            text = uiState.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        uiState.detail?.address?.let { address ->
                            Text(
                                text = address,
                                style = AppTextStyles.monoCaption,
                                color = AppTheme.semanticColors.ink45,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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
                        FavoriteAction(
                            isFavorite = uiState.isFavorite,
                            onClick = { onAction(ComplexDetailContract.Action.FavoriteClicked) },
                        )
                    }
                },
            )
        },
        bottomBar = {
            // 기획서의 고정 버튼. 긴 상세를 다 내리지 않아도 현장에서 바로 쓸 수 있어야 한다.
            if (uiState.detail != null) {
                NoteActionBar(
                    hasNote = uiState.note != null,
                    onClick = { onAction(ComplexDetailContract.Action.NoteClicked) },
                )
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> CenterBox(Modifier.padding(innerPadding)) {
                CircularProgressIndicator()
            }

            uiState.errorRes != null -> CenterBox(Modifier.padding(innerPadding)) {
                EmptyContent(title = stringResource(uiState.errorRes)) {
                    TextButton(onClick = { onAction(ComplexDetailContract.Action.RetryClicked) }) {
                        Text(text = stringResource(R.string.retry))
                    }
                }
            }

            uiState.detail != null -> DetailContent(
                detail = uiState.detail,
                selectedArea = uiState.selectedArea,
                note = uiState.note,
                onAction = onAction,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun NoteActionBar(hasNote: Boolean, onClick: () -> Unit) {
    Column {
        HorizontalDivider(thickness = AppSize.border, color = AppTheme.semanticColors.line)
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.md),
            shape = RoundedCornerShape(AppRadius.button),
        ) {
            Text(
                text = stringResource(if (hasNote) R.string.note_edit else R.string.note_write),
            )
        }
    }
}

@Composable
private fun FavoriteAction(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.notice_favorite_remove else R.string.notice_favorite_add,
            ),
            tint = if (isFavorite) {
                AppTheme.semanticColors.close
            } else {
                AppTheme.semanticColors.ink45
            },
        )
    }
}

@Composable
private fun CenterBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun DetailContent(
    detail: ComplexDetail,
    selectedArea: Double?,
    note: SiteVisitNote?,
    onAction: (ComplexDetailContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        SummaryGrid(entries = detail.toSummaryEntries())

        DealSection(detail = detail, selectedArea = selectedArea, onAction = onAction)

        if (detail.areaComposition.isNotEmpty()) {
            AreaCompositionSection(detail.areaComposition, detail.householdCount)
        }

        BuildingSection(detail.building)

        if (detail.transit.hasAny) {
            TransitSection(detail.transit)
        }

        if (detail.surroundings.hasAny) {
            SurroundingSection(detail.surroundings)
        }

        NoteSection(note = note, onAction = onAction)

        FootnoteText(text = stringResource(R.string.trade_footnote))
    }
}

// ── 임장노트 ─────────────────────────────────────────────

@Composable
private fun NoteSection(note: SiteVisitNote?, onAction: (ComplexDetailContract.Action) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        SectionHeader(title = stringResource(R.string.note_section_mine))
        if (note == null) {
            Text(
                text = stringResource(R.string.note_empty_detail),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.semanticColors.ink45,
            )
        } else {
            SiteVisitNoteCard(
                note = note,
                onClick = { onAction(ComplexDetailContract.Action.NoteClicked) },
                showComplexName = false,
            )
        }
    }
}

// ── 실거래 ──────────────────────────────────────────────

@Composable
private fun DealSection(
    detail: ComplexDetail,
    selectedArea: Double?,
    onAction: (ComplexDetailContract.Action) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        SectionHeader(
            title = stringResource(R.string.complex_section_trend),
            trailingText = stringResource(R.string.complex_trend_window),
        )

        if (detail.areas.isEmpty()) {
            EmptyContent(
                title = stringResource(R.string.complex_no_deals_title),
                description = detail.dealsNote,
            )
            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            detail.areas.forEach { area ->
                FilterChip(
                    selected = area == selectedArea,
                    onClick = { onAction(ComplexDetailContract.Action.AreaSelected(area)) },
                    label = { Text(text = area.formatArea()) },
                )
            }
        }

        selectedArea?.let { area ->
            detail.trendsByArea[area]?.let { trend -> AreaTrendBlock(trend) }
        }
    }
}

@Composable
private fun AreaTrendBlock(trend: AreaTrend) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
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
                trend.latestDate?.let { date ->
                    Text(
                        text = listOfNotNull(
                            date,
                            trend.latestFloor?.let { stringResource(R.string.complex_floor, it) },
                        ).joinToString(separator = " · "),
                        style = AppTextStyles.monoCaption,
                        color = AppTheme.semanticColors.ink45,
                    )
                }
            }
            trend.change?.let { change -> ChangeLabel(change) }
        }

        // 점이 두 개 이하면 "추이"가 아니라 그냥 값이다. 아래 거래 목록으로 충분하다.
        if (trend.monthlyAverages.size >= MIN_TREND_POINTS) {
            DealTrendChart(points = trend.monthlyAverages)
        }

        SectionHeader(
            title = stringResource(R.string.complex_section_recent_deals),
            trailingText = pluralStringResource(
                R.plurals.complex_deal_count,
                trend.dealCount,
                trend.dealCount,
            ),
        )
        val shown = trend.deals.take(MAX_DEAL_ROWS)
        shown.forEachIndexed { index, deal ->
            DealRow(deal)
            if (index != shown.lastIndex) {
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
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
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

// ── 평형 구성 ────────────────────────────────────────────

@Composable
private fun AreaCompositionSection(buckets: List<AreaBucket>, totalHouseholds: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        SectionHeader(
            title = stringResource(R.string.complex_section_area_mix),
            trailingText = totalHouseholds?.formatThousands()
                ?.let { stringResource(R.string.notice_unit_household, it) },
        )
        buckets.forEach { bucket ->
            InfoRow(
                label = stringResource(bucket.kind.labelRes()),
                value = bucket.householdCount.formatThousands()
                    ?.let { stringResource(R.string.notice_unit_household, it) }
                    .orEmpty(),
            )
        }
    }
}

private fun AreaBucketKind.labelRes(): Int = when (this) {
    AreaBucketKind.UNDER_60 -> R.string.complex_area_under_60
    AreaBucketKind.FROM_60_TO_85 -> R.string.complex_area_60_85
    AreaBucketKind.FROM_85_TO_135 -> R.string.complex_area_85_135
    AreaBucketKind.OVER_135 -> R.string.complex_area_over_135
}

// ── 단지 정보 ────────────────────────────────────────────

@Composable
private fun BuildingSection(building: BuildingInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        SectionHeader(
            title = stringResource(R.string.complex_section_building),
            trailingText = stringResource(R.string.complex_source_kapt),
        )
        InfoRow(stringResource(R.string.complex_house_type), building.houseTypeName)
        InfoRow(stringResource(R.string.complex_hall_type), building.hallTypeName)
        InfoRow(stringResource(R.string.complex_structure), building.structureName)
        InfoRow(stringResource(R.string.complex_builder), building.builderName)
        InfoRow(stringResource(R.string.complex_developer), building.developerName)
        InfoRow(stringResource(R.string.complex_management), building.managementName)
        InfoRow(stringResource(R.string.complex_security), building.securityCompany)
        InfoRow(
            label = stringResource(R.string.complex_elevator),
            value = building.elevatorCount?.formatThousands()
                ?.let { stringResource(R.string.complex_unit_count, it) },
        )
        InfoRow(
            label = stringResource(R.string.complex_parking),
            value = building.parkingLabel(),
        )
        InfoRow(
            label = stringResource(R.string.complex_ev_charger),
            value = building.evChargerLabel(),
        )
        InfoRow(
            label = stringResource(R.string.complex_cctv),
            value = building.cctvCount?.formatThousands()
                ?.let { stringResource(R.string.complex_unit_count, it) },
        )
    }
}

/** "2,110대 (지상 120 · 지하 1,990)" — 총계만으로는 지하주차장 유무를 알 수 없다. */
@Composable
private fun BuildingInfo.parkingLabel(): String? {
    val total = totalParking?.formatThousands() ?: return null
    val breakdown = listOfNotNull(
        parkingGround?.formatThousands()
            ?.let { stringResource(R.string.complex_parking_ground, it) },
        parkingUnderground?.formatThousands()
            ?.let { stringResource(R.string.complex_parking_underground, it) },
    )
    val totalText = stringResource(R.string.complex_unit_count, total)
    return if (breakdown.isEmpty()) {
        totalText
    } else {
        "$totalText (${breakdown.joinToString(separator = " · ")})"
    }
}

@Composable
private fun BuildingInfo.evChargerLabel(): String? {
    val total = totalEvCharger?.takeIf { it > 0 }?.formatThousands() ?: return null
    return stringResource(R.string.complex_unit_count, total)
}

// ── 교통 ─────────────────────────────────────────────────

@Composable
private fun TransitSection(transit: TransitInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        SectionHeader(title = stringResource(R.string.complex_section_transit))
        InfoRow(
            label = stringResource(R.string.complex_subway),
            value = listOfNotNull(
                transit.subwayLine,
                transit.subwayStation,
                transit.subwayWalkTime?.let {
                    stringResource(R.string.complex_walk_time, it)
                },
            ).joinToString(separator = " · ").takeIf { it.isNotBlank() },
        )
        InfoRow(
            label = stringResource(R.string.complex_bus),
            value = transit.busWalkTime?.let { stringResource(R.string.complex_walk_time, it) },
        )
    }
}

// ── 주변 시설 ────────────────────────────────────────────

@Composable
private fun SurroundingSection(surroundings: Surroundings) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        SectionHeader(title = stringResource(R.string.complex_section_surroundings))
        FacilityRows(stringResource(R.string.complex_facility_education), surroundings.education)
        FacilityRows(stringResource(R.string.complex_facility_convenient), surroundings.convenient)
        FacilityChips(stringResource(R.string.complex_facility_welfare), surroundings.welfare)
    }
}

/**
 * 분류가 있는 시설은 "초등학교 | 구암, 신봉, 은천초등학교"처럼 행으로.
 * 칩으로 늘어놓으면 분류와 이름이 섞여 무엇이 무엇인지 읽히지 않는다.
 */
@Composable
private fun FacilityRows(label: String, groups: List<FacilityGroup>) {
    if (groups.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(
            text = label,
            style = AppTextStyles.monoCaption,
            color = AppTheme.semanticColors.ink45,
        )
        groups.forEach { group ->
            InfoRow(label = group.category, value = group.names.joinToString(separator = ", "))
        }
    }
}

/** 단지 내 시설은 분류 없는 짧은 이름들이라 칩이 맞다. */
@Composable
private fun FacilityChips(label: String, items: List<String>) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = label,
            style = AppTextStyles.monoCaption,
            color = AppTheme.semanticColors.ink45,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items.forEach { item -> FacilityChip(text = item) }
        }
    }
}

// ── 공통 ─────────────────────────────────────────────────

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
    topFloor?.let {
        SummaryEntry(
            label = stringResource(R.string.complex_top_floor),
            value = stringResource(R.string.complex_floor, it),
        )
    },
    heatingName?.let {
        SummaryEntry(label = stringResource(R.string.complex_heating), value = it)
    },
)

private const val MAX_DEAL_ROWS = 5
private const val MIN_TREND_POINTS = 3

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun ComplexDetailScreenPreview() {
    AppTheme {
        ComplexDetailScreen(
            uiState = ComplexDetailContract.State(
                kaptCode = "A15105302",
                name = "관악푸르지오아파트",
                isLoading = false,
                isFavorite = true,
                selectedArea = 59.58,
                detail = previewDetail(),
            ),
            onAction = {},
            onBackClick = {},
        )
    }
}

private fun previewDetail() = ComplexDetail(
    kaptCode = "A15105302",
    name = "관악푸르지오아파트",
    address = "서울특별시 관악구 관악로30길 27",
    useApprovalDate = "20040826",
    householdCount = 2104,
    dongCount = "23",
    topFloor = 24,
    heatingName = "개별난방",
    building = BuildingInfo(
        houseTypeName = "아파트",
        hallTypeName = "혼합식",
        structureName = "철근콘크리트구조",
        builderName = "대우건설",
        developerName = "재건축조합",
        managementName = "위탁관리",
        securityCompany = "(주)예주산업",
        elevatorCount = 36,
        parkingGround = 120,
        parkingUnderground = 1990,
        cctvCount = 230,
        evChargerGround = 29,
        evChargerUnderground = 15,
    ),
    areaComposition = listOf(
        AreaBucket(AreaBucketKind.UNDER_60, 1158),
        AreaBucket(AreaBucketKind.FROM_60_TO_85, 828),
        AreaBucket(AreaBucketKind.FROM_85_TO_135, 118),
    ),
    transit = TransitInfo(
        subwayLine = "2호선",
        subwayStation = "서울대입구역",
        subwayWalkTime = "15~20분이내",
        busWalkTime = "10~15분이내",
    ),
    surroundings = Surroundings(
        convenient = listOf(
            FacilityGroup("관공서", listOf("청림동")),
            FacilityGroup("병원", listOf("고려병원")),
            FacilityGroup("대형상가", listOf("관악프라자")),
        ),
        education = listOf(
            FacilityGroup("초등학교", listOf("봉천초등학교")),
            FacilityGroup("중학교", listOf("상도중학교")),
            FacilityGroup("대학교", listOf("서울대학교", "숭실대학교")),
        ),
        welfare = listOf("관리사무소", "노인정", "보육시설", "어린이놀이터"),
    ),
    areas = listOf(84.9, 59.58),
    trendsByArea = mapOf(
        59.58 to AreaTrend(
            latestAmount = 121500,
            latestDate = "2026-08-29",
            latestFloor = 21,
            change = 30907,
            dealCount = 72,
            monthlyAverages = listOf(
                TrendPoint("202603", 106_500, 9),
                TrendPoint("202604", 111_540, 10),
                TrendPoint("202605", 113_900, 13),
                TrendPoint("202606", 114_500, 2),
                TrendPoint("202607", 116_781, 8),
                TrendPoint("202608", 121_500, 1),
            ),
            deals = listOf(
                ComplexDeal("2026-08-29", 21, 121500),
                ComplexDeal("2026-07-31", 1, 109000),
            ),
        ),
    ),
    dealsNote = null,
)
