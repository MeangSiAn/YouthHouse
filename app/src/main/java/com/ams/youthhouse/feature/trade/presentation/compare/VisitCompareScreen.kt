package com.ams.youthhouse.feature.trade.presentation.compare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.R
import com.ams.youthhouse.core.common.format.formatManwonAsEokMan
import com.ams.youthhouse.core.common.format.formatThousands
import com.ams.youthhouse.core.common.format.formatYearMonthDay
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.feature.trade.domain.VisitComparison
import com.ams.youthhouse.feature.trade.domain.model.ComplexSnapshot
import com.ams.youthhouse.feature.trade.domain.model.DefectStatus
import com.ams.youthhouse.feature.trade.domain.model.ElevatorCondition
import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.domain.model.VisitCriterion
import com.ams.youthhouse.feature.trade.domain.model.VisitRatings
import com.ams.youthhouse.feature.trade.presentation.component.RatingDots
import com.ams.youthhouse.feature.trade.presentation.component.formatArea
import com.ams.youthhouse.feature.trade.presentation.component.labelRes
import java.util.Locale

/**
 * 임장 단지 비교.
 *
 * 열이 단지, 행이 항목인 표다. 각 행에서 가장 나은 단지를 강조하되, 그 판단은
 * 도메인([VisitComparison])이 하고 화면은 색만 입힌다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitCompareScreen(
    uiState: VisitCompareContract.State,
    onAction: (VisitCompareContract.Action) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.compare_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.notice_detail_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            !uiState.isLoaded -> Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            !uiState.hasEnoughNotes -> EmptyContent(
                title = stringResource(R.string.compare_empty_title),
                description = stringResource(R.string.compare_empty_description),
                modifier = Modifier.padding(innerPadding),
            )

            else -> CompareContent(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun CompareContent(
    uiState: VisitCompareContract.State,
    onAction: (VisitCompareContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            SectionHeader(
                title = stringResource(R.string.compare_pick),
                trailingText = stringResource(
                    R.string.compare_pick_limit,
                    VisitCompareContract.MAX_SELECTION,
                ),
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                uiState.notes.forEach { note ->
                    FilterChip(
                        selected = note.kaptCode in uiState.selectedCodes,
                        onClick = {
                            onAction(VisitCompareContract.Action.SelectionToggled(note.kaptCode))
                        },
                        label = {
                            Text(
                                text = note.complexName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
        }

        if (uiState.hasEnoughSelection) {
            CompareTable(
                notes = uiState.selectedNotes,
                comparison = uiState.comparison,
                onNoteClick = { note ->
                    onAction(VisitCompareContract.Action.NoteClicked(note.kaptCode, note.complexName))
                },
            )
        } else {
            Text(
                text = stringResource(R.string.compare_pick_hint, VisitCompareContract.MIN_NOTES),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.semanticColors.ink45,
            )
        }

        FootnoteText(text = stringResource(R.string.compare_footnote))
    }
}

@Composable
private fun CompareTable(
    notes: List<SiteVisitNote>,
    comparison: VisitComparison,
    onNoteClick: (SiteVisitNote) -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(AppSize.border, AppTheme.semanticColors.line), shape)
            .clip(shape),
    ) {
        HeaderRow(notes = notes, onNoteClick = onNoteClick)
        RowDivider()

        CompareRow(label = stringResource(R.string.note_visited_on), notes = notes) { note ->
            CellText(text = note.visitedOn.formatYearMonthDay().orEmpty())
        }

        VisitCriterion.entries.forEach { criterion ->
            CompareRow(label = stringResource(criterion.labelRes()), notes = notes) { note ->
                RatingCell(
                    score = note.ratings[criterion],
                    highlighted = comparison.isBest(criterion, note.kaptCode),
                )
            }
        }

        CompareRow(label = stringResource(R.string.note_overall), notes = notes) { note ->
            CellText(
                text = note.ratings.average?.let { String.format(Locale.US, "%.1f", it) }
                    ?: stringResource(R.string.compare_not_rated),
                highlighted = note.kaptCode in comparison.bestOverall,
                style = MaterialTheme.typography.titleSmall,
            )
        }

        CompareRow(label = stringResource(R.string.note_walk_to_station), notes = notes) { note ->
            CellText(
                text = note.walkToStationMinutes?.let { stringResource(R.string.note_walk_minutes, it) }
                    ?: stringResource(R.string.compare_not_rated),
                highlighted = note.kaptCode in comparison.shortestWalk,
            )
        }

        CompareRow(label = stringResource(R.string.note_elevator), notes = notes) { note ->
            CellText(
                text = stringResource(note.elevatorCondition.labelRes()),
                color = when (note.elevatorCondition) {
                    ElevatorCondition.NONE, ElevatorCondition.CROWDED ->
                        AppTheme.semanticColors.close
                    ElevatorCondition.COMFORTABLE -> AppTheme.semanticColors.ink
                    ElevatorCondition.UNCHECKED -> AppTheme.semanticColors.ink45
                },
            )
        }

        CompareRow(label = stringResource(R.string.note_defect), notes = notes) { note ->
            CellText(
                text = stringResource(note.defectStatus.labelRes()),
                color = when (note.defectStatus) {
                    DefectStatus.FOUND -> AppTheme.semanticColors.close
                    DefectStatus.NONE -> AppTheme.semanticColors.ink
                    DefectStatus.UNCHECKED -> AppTheme.semanticColors.ink45
                },
            )
        }

        CompareRow(label = stringResource(R.string.note_viewed_unit), notes = notes) { note ->
            CellText(text = note.viewedUnit.ifBlank { stringResource(R.string.compare_not_rated) })
        }

        CompareRow(label = stringResource(R.string.note_reference_deal), notes = notes) { note ->
            CellText(
                text = listOfNotNull(
                    note.snapshot.referenceArea?.formatArea(),
                    note.snapshot.referenceAmount?.formatManwonAsEokMan(),
                ).joinToString(separator = "\n").ifBlank { stringResource(R.string.compare_not_rated) },
            )
        }

        CompareRow(label = stringResource(R.string.complex_built), notes = notes) { note ->
            CellText(
                text = note.snapshot.builtYear?.let { stringResource(R.string.complex_built_year, it) }
                    ?: stringResource(R.string.compare_not_rated),
            )
        }

        CompareRow(label = stringResource(R.string.complex_households), notes = notes) { note ->
            CellText(
                text = note.snapshot.householdCount?.formatThousands()
                    ?.let { stringResource(R.string.notice_unit_household, it) }
                    ?: stringResource(R.string.compare_not_rated),
            )
        }

        CompareRow(
            label = stringResource(R.string.note_section_memo),
            notes = notes,
            showDivider = false,
        ) { note ->
            CellText(
                text = note.memo.ifBlank { stringResource(R.string.compare_not_rated) },
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.semanticColors.ink70,
                maxLines = MEMO_MAX_LINES,
            )
        }
    }
}

@Composable
private fun HeaderRow(notes: List<SiteVisitNote>, onNoteClick: (SiteVisitNote) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Spacer(modifier = Modifier.width(LABEL_WIDTH))
        notes.forEach { note ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNoteClick(note) }
                    .padding(horizontal = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                Text(
                    text = note.complexName,
                    style = MaterialTheme.typography.titleSmall,
                    color = AppTheme.semanticColors.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (note.regionLabel.isNotBlank()) {
                    Text(
                        text = note.regionLabel,
                        style = AppTextStyles.monoCaption,
                        color = AppTheme.semanticColors.ink45,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** 행 하나 — 왼쪽 라벨 + 단지 수만큼의 셀. 셀 배경이 행 높이를 채우도록 높이를 맞춘다. */
@Composable
private fun CompareRow(
    label: String,
    notes: List<SiteVisitNote>,
    showDivider: Boolean = true,
    cell: @Composable (SiteVisitNote) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = AppTextStyles.monoCaption,
            color = AppTheme.semanticColors.ink45,
            modifier = Modifier
                .width(LABEL_WIDTH)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
        )
        notes.forEach { note ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                cell(note)
            }
        }
    }
    if (showDivider) RowDivider()
}

@Composable
private fun RowDivider() {
    HorizontalDivider(thickness = AppSize.border, color = AppTheme.semanticColors.line2)
}

@Composable
private fun CellText(
    text: String,
    highlighted: Boolean = false,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = AppTheme.semanticColors.ink,
    maxLines: Int = 2,
) {
    Text(
        text = text,
        style = style,
        color = if (highlighted) MaterialTheme.colorScheme.primary else color,
        fontWeight = if (highlighted) FontWeight.Bold else null,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxSize()
            .highlight(highlighted)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.md),
    )
}

@Composable
private fun RatingCell(score: Int?, highlighted: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .highlight(highlighted)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        RatingDots(score = score)
        Text(
            text = score?.toString() ?: stringResource(R.string.compare_not_rated),
            style = AppTextStyles.mono,
            color = when {
                highlighted -> MaterialTheme.colorScheme.primary
                score == null -> AppTheme.semanticColors.ink45
                else -> AppTheme.semanticColors.ink
            },
            fontWeight = if (highlighted) FontWeight.Bold else null,
        )
    }
}

@Composable
private fun Modifier.highlight(enabled: Boolean): Modifier =
    if (enabled) {
        background(MaterialTheme.colorScheme.primary.copy(alpha = HIGHLIGHT_ALPHA))
    } else {
        this
    }

/** "누수·곰팡이"가 한 줄에 들어가는 폭. 더 좁히면 단어 중간에서 꺾인다. */
private val LABEL_WIDTH = 88.dp
private const val HIGHLIGHT_ALPHA = 0.08f
private const val MEMO_MAX_LINES = 4

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun VisitCompareScreenPreview() {
    val notes = listOf(
        previewNote("A1", "관악푸르지오", light = 4, noise = 3, parking = 2, walk = 8, amount = 121_500),
        previewNote("A2", "봉천두산", light = 3, noise = 4, parking = 4, walk = 12, amount = 98_000),
        previewNote("A3", "신림현대", light = 5, noise = 3, parking = null, walk = 5, amount = 87_000),
    )
    AppTheme {
        VisitCompareScreen(
            uiState = VisitCompareContract.State(
                notes = notes,
                isLoaded = true,
                selectedCodes = notes.map { it.kaptCode },
            ),
            onAction = {},
            onBackClick = {},
        )
    }
}

private fun previewNote(
    code: String,
    name: String,
    light: Int?,
    noise: Int?,
    parking: Int?,
    walk: Int?,
    amount: Long?,
) = SiteVisitNote(
    kaptCode = code,
    complexName = name,
    regionLabel = "서울특별시 관악구",
    visitedOn = "20260720",
    viewedUnit = "84㎡ · 12층",
    ratings = VisitRatings()
        .with(VisitCriterion.LIGHT, light)
        .with(VisitCriterion.NOISE, noise)
        .with(VisitCriterion.PARKING, parking),
    walkToStationMinutes = walk,
    elevatorCondition = ElevatorCondition.COMFORTABLE,
    defectStatus = DefectStatus.NONE,
    memo = "남향 채광 좋음. 8층 이하는 앞동에 가림.",
    snapshot = ComplexSnapshot(
        builtYear = "2004",
        householdCount = 2104,
        subwayLabel = "2호선 · 서울대입구역",
        referenceArea = 84.9,
        referenceAmount = amount,
    ),
    updatedAtMillis = 0L,
)
