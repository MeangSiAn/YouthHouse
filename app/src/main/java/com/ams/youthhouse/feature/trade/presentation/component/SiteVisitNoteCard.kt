package com.ams.youthhouse.feature.trade.presentation.component

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.R
import com.ams.youthhouse.core.common.format.formatYearMonthDay
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.complex.domain.model.ComplexSnapshot
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings

/**
 * 기획서 `.notecard` — 임장노트 요약 카드.
 *
 * 메모가 맨 위다. 기획서 노트대로 자유 메모가 실제로 가장 많이 쓰이는 필드고,
 * 점수는 그 아래에 칩으로 짧게 붙는다.
 *
 * @param showComplexName 단지 상세 안에서는 이미 제목이 단지명이라 끈다.
 */
@Composable
fun SiteVisitNoteCard(
    note: SiteVisitNote,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showComplexName: Boolean = true,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(AppSize.border, AppTheme.semanticColors.line), shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                Text(
                    text = if (showComplexName) {
                        note.complexName
                    } else {
                        note.viewedUnit.ifBlank { stringResource(R.string.note_title) }
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = AppTheme.semanticColors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (showComplexName && note.regionLabel.isNotBlank()) {
                    Text(
                        text = note.regionLabel,
                        style = AppTextStyles.monoCaption,
                        color = AppTheme.semanticColors.ink45,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = note.visitedOn.formatYearMonthDay().orEmpty(),
                style = AppTextStyles.monoCaption,
                color = AppTheme.semanticColors.ink45,
            )
        }

        if (note.memo.isNotBlank()) {
            Text(
                text = note.memo,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.semanticColors.ink70,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val chips = note.summaryChips()
        if (chips.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                chips.forEach { chip -> FacilityChip(text = chip) }
            }
        }
    }
}

/** "채광 4 · 소음 3 · 역 도보 8분 · 누수·곰팡이 있음" — 남긴 것만 짧게. */
@Composable
private fun SiteVisitNote.summaryChips(): List<String> = buildList {
    VisitCriterion.entries.forEach { criterion ->
        ratings[criterion]?.let { score ->
            add(stringResource(R.string.note_score_short, stringResource(criterion.labelRes()), score))
        }
    }
    walkToStationMinutes?.let { minutes ->
        add(stringResource(R.string.note_walk_short, minutes))
    }
    // 엘리베이터는 불편할 때만 알린다. "여유"는 굳이 칩 한 칸을 쓸 정보가 아니다.
    if (elevatorCondition == ElevatorCondition.CROWDED ||
        elevatorCondition == ElevatorCondition.NONE
    ) {
        add(
            "${stringResource(R.string.note_elevator)} " +
                stringResource(elevatorCondition.labelRes()),
        )
    }
    if (defectStatus == DefectStatus.FOUND) {
        add("${stringResource(R.string.note_defect)} ${stringResource(R.string.note_defect_found)}")
    }
}

/** 1~5점을 점 다섯 개로. 숫자보다 눈으로 빨리 비교된다. */
@Composable
fun RatingDots(
    score: Int?,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        (VisitRatings.MIN_SCORE..VisitRatings.MAX_SCORE).forEach { step ->
            Box(
                modifier = Modifier
                    .size(RATING_DOT)
                    .clip(CircleShape)
                    .background(
                        if (score != null && step <= score) {
                            activeColor
                        } else {
                            AppTheme.semanticColors.line
                        },
                    ),
            )
        }
    }
}

@StringRes
fun VisitCriterion.labelRes(): Int = when (this) {
    VisitCriterion.LIGHT -> R.string.note_criterion_light
    VisitCriterion.NOISE -> R.string.note_criterion_noise
    VisitCriterion.PARKING -> R.string.note_criterion_parking
    VisitCriterion.MANAGEMENT -> R.string.note_criterion_management
}

@StringRes
fun ElevatorCondition.labelRes(): Int = when (this) {
    ElevatorCondition.UNCHECKED -> R.string.note_elevator_unchecked
    ElevatorCondition.COMFORTABLE -> R.string.note_elevator_comfortable
    ElevatorCondition.CROWDED -> R.string.note_elevator_crowded
    ElevatorCondition.NONE -> R.string.note_elevator_none
}

@StringRes
fun DefectStatus.labelRes(): Int = when (this) {
    DefectStatus.UNCHECKED -> R.string.note_defect_unchecked
    DefectStatus.NONE -> R.string.note_defect_none
    DefectStatus.FOUND -> R.string.note_defect_found
}

private val RATING_DOT = 7.dp

@Preview(showBackground = true)
@Composable
private fun SiteVisitNoteCardPreview() {
    AppTheme {
        SiteVisitNoteCard(
            note = SiteVisitNote(
                kaptCode = "A15105302",
                complexName = "관악푸르지오아파트",
                regionLabel = "서울특별시 관악구 봉천동",
                visitedOn = "20260720",
                viewedUnit = "84㎡ · 12층 · 남향",
                ratings = VisitRatings(
                    mapOf(
                        VisitCriterion.LIGHT to 4,
                        VisitCriterion.NOISE to 3,
                        VisitCriterion.PARKING to 2,
                    ),
                ),
                walkToStationMinutes = 8,
                elevatorCondition = ElevatorCondition.CROWDED,
                defectStatus = DefectStatus.NONE,
                memo = "남향 채광 좋음. 다만 8층 이하는 앞동에 가림. 단지 안 경사가 있어 유모차는 불편할 듯.",
                snapshot = ComplexSnapshot.EMPTY,
                updatedAtMillis = 0L,
            ),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}
