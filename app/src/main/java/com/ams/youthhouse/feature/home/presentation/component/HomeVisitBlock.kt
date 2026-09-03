package com.ams.youthhouse.feature.home.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.component.StatusLabel
import com.ams.youthhouse.core.designsystem.component.StatusTone
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.feature.home.presentation.model.HomeVisitUiModel

/**
 * 기획서 H-06 — 임장 예정은 카드로, 다녀온 곳은 행으로.
 *
 * 예정 카드는 코발트 테두리로 "아직 갈 곳"임을 알리고, 행의 별점은 시그널 색으로
 * 공고 카드의 상태 색과 구분한다. 홈에서만 쓰는 조합이라 designsystem으로 올리지 않는다.
 */
@Composable
fun PlannedVisitCard(
    visit: HomeVisitUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(AppSize.borderStrong, MaterialTheme.colorScheme.primary), shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = listOf(
                    stringResource(R.string.home_visit_planned_date, visit.visitedOnLabel),
                    visit.regionLabel,
                ).filter { it.isNotBlank() }.joinToString(separator = " · "),
                style = AppTextStyles.monoCaption,
                color = AppTheme.semanticColors.ink45,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            StatusLabel(
                text = stringResource(R.string.home_visit_planned),
                tone = StatusTone.SOON,
            )
        }
        Text(
            text = visit.complexName,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.semanticColors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (visit.viewedUnit.isNotBlank()) {
            Text(
                text = visit.viewedUnit,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.semanticColors.ink70,
            )
        }
    }
}

/** 다녀온 곳 목록. 기획서 `.rows` — 한 줄에 단지명·방문일·점검 진행, 오른쪽에 별점. */
@Composable
fun VisitRowGroup(
    visits: List<HomeVisitUiModel>,
    onVisitClick: (HomeVisitUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (visits.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(AppSize.border, AppTheme.semanticColors.line),
                shape = RoundedCornerShape(AppRadius.card),
            ),
    ) {
        visits.forEachIndexed { index, visit ->
            VisitRow(visit = visit, onClick = { onVisitClick(visit) })
            if (index != visits.lastIndex) {
                HorizontalDivider(
                    thickness = AppSize.border,
                    color = AppTheme.semanticColors.line2,
                )
            }
        }
    }
}

@Composable
private fun VisitRow(visit: HomeVisitUiModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md + AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
        ) {
            Text(
                text = visit.complexName,
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.semanticColors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (visit.isIncomplete) {
                    // 기획서 NOTE-03: 방문 후 며칠 지나면 기억이 흐려진다. 사과 대신 재촉한다.
                    stringResource(R.string.home_visit_incomplete)
                } else {
                    stringResource(
                        R.string.home_visit_done_summary,
                        visit.visitedOnLabel,
                        visit.ratedCount,
                        visit.totalCriteria,
                    )
                },
                style = AppTextStyles.monoCaption,
                color = if (visit.isIncomplete) {
                    AppTheme.semanticColors.signalDeep
                } else {
                    AppTheme.semanticColors.ink45
                },
            )
        }
        visit.stars?.let { stars -> StarText(stars = stars) }
    }
}

/** `★★★★☆` — 텍스트로 그린다. 홈에서 한 줄 요약으로 충분하고 아이콘 다섯 개보다 가볍다. */
@Composable
private fun StarText(stars: Int) {
    val filled = stars.coerceIn(0, HomeVisitUiModel.MAX_STARS)
    Text(
        text = "★".repeat(filled) + "☆".repeat(HomeVisitUiModel.MAX_STARS - filled),
        style = AppTextStyles.mono,
        color = AppTheme.semanticColors.signal,
    )
}

/** 기획서 SCREEN 02 임장기록 빈 상태. 노트는 단지 상세에서 쓰므로 매매 탭으로 보낸다. */
@Composable
fun VisitEmptyBlock(
    onFindComplexClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyContent(
        title = stringResource(R.string.home_visit_empty_title),
        description = stringResource(R.string.home_visit_empty_description),
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(AppSize.border, AppTheme.semanticColors.line),
                shape = RoundedCornerShape(AppRadius.card),
            ),
    ) {
        OutlinedButton(
            onClick = onFindComplexClick,
            shape = RoundedCornerShape(AppRadius.button),
        ) {
            Text(text = stringResource(R.string.home_visit_empty_action))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeVisitBlockPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            PlannedVisitCard(
                visit = previewVisit("A1", "관악푸르지오아파트", planned = true, stars = null),
                onClick = {},
            )
            VisitRowGroup(
                visits = listOf(
                    previewVisit("A2", "봉천두산", stars = 4),
                    previewVisit("A3", "신림현대", stars = 2, incomplete = true),
                ),
                onVisitClick = {},
            )
            VisitEmptyBlock(onFindComplexClick = {})
        }
    }
}

private fun previewVisit(
    code: String,
    name: String,
    planned: Boolean = false,
    incomplete: Boolean = false,
    stars: Int?,
) = HomeVisitUiModel(
    kaptCode = code,
    complexName = name,
    regionLabel = "서울특별시 관악구 봉천동",
    visitedOnLabel = "07.27",
    isPlanned = planned,
    isIncomplete = incomplete,
    stars = stars,
    ratedCount = if (incomplete) 1 else 4,
    totalCriteria = 4,
    viewedUnit = "84㎡ · 12층 · 남향",
)
