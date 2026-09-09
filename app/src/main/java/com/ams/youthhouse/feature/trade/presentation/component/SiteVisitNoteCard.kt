package com.ams.youthhouse.feature.trade.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.common.format.formatManwonAsEokMan
import com.ams.youthhouse.core.common.format.formatYearMonthDay
import com.ams.youthhouse.core.complex.domain.model.ComplexSnapshot
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings
import com.ams.youthhouse.core.complex.presentation.component.CriterionDots
import com.ams.youthhouse.core.complex.presentation.component.labelRes
import com.ams.youthhouse.core.complex.presentation.component.shortLabelRes
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 v6 `.notecard` — 임장노트 요약 카드.
 *
 * 위에서부터 단지명·방문일 → 메모 → 구분선 → 항목별 점 → 부가 정보 칩.
 * 점수는 숫자가 아니라 점으로 그린다. 숫자 나열은 읽어야 알지만 찬 점은 훑으면 보이고,
 * 2점 이하는 붉어서 약점이 먼저 눈에 들어온다.
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
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
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

        val rated = VisitCriterion.entries.filter { note.ratings[it] != null }
        val chips = note.metaChips()
        if (rated.isNotEmpty() || chips.isNotEmpty()) {
            HorizontalDivider(thickness = AppSize.border, color = AppTheme.semanticColors.line2)
        }

        if (rated.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                rated.forEach { criterion ->
                    CriterionDots(
                        label = stringResource(criterion.shortLabelRes()),
                        score = note.ratings[criterion],
                    )
                }
            }
        }

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

/**
 * "역 도보 8분 · 84㎡ · 12층 · 방문 당시 12억 1,500" — 점수 아닌 것들.
 * 엘리베이터·누수는 불편할 때만 알린다. "여유"·"없음"은 칩 한 칸을 쓸 정보가 아니다.
 */
@Composable
private fun SiteVisitNote.metaChips(): List<String> = buildList {
    walkToStationMinutes?.let { minutes ->
        add(stringResource(R.string.note_walk_short, minutes))
    }
    if (viewedUnit.isNotBlank()) add(viewedUnit)
    snapshot.referenceAmount?.let { amount ->
        add(stringResource(R.string.note_reference_short, amount.formatManwonAsEokMan()))
    }
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
                        VisitCriterion.MANAGEMENT to 4,
                    ),
                ),
                walkToStationMinutes = 8,
                elevatorCondition = ElevatorCondition.COMFORTABLE,
                defectStatus = DefectStatus.NONE,
                memo = "남향 채광 좋음. 다만 8층 이하는 앞동에 가림. 단지 안 경사가 있어 유모차는 불편할 듯.",
                snapshot = ComplexSnapshot("2004", 2104, null, 84.9, 121_500),
                updatedAtMillis = 0L,
            ),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}
