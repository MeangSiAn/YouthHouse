package com.ams.youthhouse.feature.trade.presentation.compare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.R
import com.ams.youthhouse.core.common.format.formatManwonAsEokMan
import com.ams.youthhouse.core.common.format.formatThousands
import com.ams.youthhouse.core.common.format.formatYearMonthDay
import com.ams.youthhouse.core.complex.domain.VisitComparison
import com.ams.youthhouse.core.complex.domain.model.ComplexSnapshot
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings
import com.ams.youthhouse.core.complex.presentation.component.RatingDots
import com.ams.youthhouse.core.complex.presentation.component.labelRes
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.feature.trade.presentation.component.formatArea
import java.util.Locale

/**
 * 임장 단지 비교.
 *
 * 선택과 표가 한 화면이다 — 2~3곳 고르는 일에 화면 전환은 과하다. 표는 열이 단지,
 * 행이 항목이고, 어느 열이 어느 단지인지 스크롤해도 잃지 않도록 머리글이 고정된다.
 * 우세는 배경 틴트로만 알린다. 승자를 선언하지 않는다 — 판단은 도메인([VisitComparison]),
 * 화면은 색만.
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompareContent(
    uiState: VisitCompareContract.State,
    onAction: (VisitCompareContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = uiState.selectedNotes
    val comparison = uiState.comparison

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            SectionHeader(
                title = stringResource(R.string.compare_pick),
                trailingText = stringResource(
                    R.string.compare_pick_limit,
                    VisitCompareContract.MAX_SELECTION,
                ),
            )
        }
        item {
            PickList(
                notes = uiState.notes,
                selectedCodes = uiState.selectedCodes,
                onToggle = { onAction(VisitCompareContract.Action.SelectionToggled(it)) },
            )
        }

        if (!uiState.hasEnoughSelection) {
            item {
                Text(
                    text = stringResource(R.string.compare_pick_hint, VisitCompareContract.MIN_NOTES),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.semanticColors.ink45,
                )
            }
        } else {
            stickyHeader {
                HeaderRow(
                    notes = selected,
                    onNoteClick = { note ->
                        onAction(VisitCompareContract.Action.NoteClicked(note.kaptCode, note.complexName))
                    },
                )
            }
            item { CompareRows(notes = selected, comparison = comparison) }

            // 메모는 표 아래 세로로. 칸에 욱여넣으면 세 줄 넘어가는 순간 못 읽는다.
            val withMemo = selected.filter { it.memo.isNotBlank() }
            if (withMemo.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.note_section_memo),
                        modifier = Modifier.padding(top = AppSpacing.md),
                    )
                }
                items(withMemo, key = { "memo-${it.kaptCode}" }) { note -> MemoBlock(note) }
            }
        }

        item { FootnoteText(text = stringResource(R.string.compare_footnote)) }
    }
}

// ── 선택 ─────────────────────────────────────────────────

@Composable
private fun PickList(
    notes: List<SiteVisitNote>,
    selectedCodes: List<String>,
    onToggle: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(AppSize.border, AppTheme.semanticColors.line),
                RoundedCornerShape(AppRadius.card),
            ),
    ) {
        notes.forEachIndexed { index, note ->
            PickRow(
                note = note,
                checked = note.kaptCode in selectedCodes,
                onClick = { onToggle(note.kaptCode) },
            )
            if (index != notes.lastIndex) RowDivider()
        }
    }
}

@Composable
private fun PickRow(note: SiteVisitNote, checked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = AppSpacing.xs, end = AppSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = { onClick() })
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
        ) {
            Text(
                text = note.complexName,
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.semanticColors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(
                    note.visitedOn.formatYearMonthDay(),
                    note.viewedUnit.takeIf { it.isNotBlank() },
                ).joinToString(separator = " · "),
                style = AppTextStyles.monoCaption,
                color = AppTheme.semanticColors.ink45,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ── 표 ───────────────────────────────────────────────────

/** 고정 머리글. 아래 표와 같은 열 폭이어야 스크롤 중에도 열이 맞는다. */
@Composable
private fun HeaderRow(notes: List<SiteVisitNote>, onNoteClick: (SiteVisitNote) -> Unit) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
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
                    if (note.viewedUnit.isNotBlank()) {
                        Text(
                            text = note.viewedUnit,
                            style = AppTextStyles.monoCaption,
                            color = AppTheme.semanticColors.ink45,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        HorizontalDivider(thickness = AppSize.border, color = AppTheme.semanticColors.line)
    }
}

@Composable
private fun CompareRows(notes: List<SiteVisitNote>, comparison: VisitComparison) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    ElevatorCondition.NONE, ElevatorCondition.CROWDED -> AppTheme.semanticColors.close
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

        CompareRow(
            label = stringResource(R.string.complex_households),
            notes = notes,
            showDivider = false,
        ) { note ->
            CellText(
                text = note.snapshot.householdCount?.formatThousands()
                    ?.let { stringResource(R.string.notice_unit_household, it) }
                    ?: stringResource(R.string.compare_not_rated),
            )
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
                .padding(end = AppSpacing.md, top = AppSpacing.md, bottom = AppSpacing.md),
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
    style: TextStyle = MaterialTheme.typography.bodyMedium,
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

/** 점만 둔다. 숫자를 곁들이면 세 열이 빽빽해져 오히려 안 읽힌다. 2점 이하는 점 자체가 붉다. */
@Composable
private fun RatingCell(score: Int?, highlighted: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .highlight(highlighted)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.md),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (score == null) {
            Text(
                text = stringResource(R.string.compare_not_rated),
                style = AppTextStyles.mono,
                color = AppTheme.semanticColors.ink45,
            )
        } else {
            RatingDots(score = score)
        }
    }
}

@Composable
private fun Modifier.highlight(enabled: Boolean): Modifier =
    if (enabled) {
        background(MaterialTheme.colorScheme.primary.copy(alpha = HIGHLIGHT_ALPHA))
    } else {
        this
    }

// ── 메모 ─────────────────────────────────────────────────

@Composable
private fun MemoBlock(note: SiteVisitNote) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(AppSize.border, AppTheme.semanticColors.line),
                RoundedCornerShape(AppRadius.card),
            )
            .clip(RoundedCornerShape(AppRadius.card))
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = note.complexName,
            style = AppTextStyles.monoCaption,
            color = AppTheme.semanticColors.ink45,
        )
        Text(
            text = note.memo,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.semanticColors.ink,
        )
    }
}

/** "누수·곰팡이"가 한 줄에 들어가는 폭. 더 좁히면 단어 중간에서 꺾인다. */
private val LABEL_WIDTH = 88.dp
private const val HIGHLIGHT_ALPHA = 0.08f

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun VisitCompareScreenPreview() {
    val notes = listOf(
        previewNote("A1", "관악푸르지오", light = 4, noise = 3, parking = 2, walk = 8, amount = 121_500),
        previewNote("A2", "부천중동 리첸시아", light = 3, noise = 2, parking = 5, walk = 5, amount = 71_000),
        previewNote("A3", "신림현대", light = 5, noise = 3, parking = null, walk = 5, amount = 87_000),
    )
    AppTheme {
        VisitCompareScreen(
            uiState = VisitCompareContract.State(
                notes = notes,
                isLoaded = true,
                selectedCodes = notes.take(2).map { it.kaptCode },
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
