package com.ams.youthhouse.feature.home.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.R
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.presentation.component.CriterionDots
import com.ams.youthhouse.core.complex.presentation.component.shortLabelRes
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.feature.home.presentation.model.HomeVisitUiModel

/**
 * 기획서 v6 홈 "최근 임장" — 가로 슬라이드 카드.
 *
 * 목록 행이던 것을 카드로 바꿨다. 바로 아래 "알아두면 좋은 것"이 가로 슬라이드라
 * 한 화면에 두 형식이 섞여 있었다. 카드에 메모는 넣지 않는다 — 홈은 떠올리는 자리고,
 * 읽는 것은 매매 탭에서.
 */
@Composable
fun VisitRail(
    visits: List<HomeVisitUiModel>,
    showMore: Boolean,
    onVisitClick: (HomeVisitUiModel) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        items(visits, key = { it.kaptCode }) { visit ->
            VisitMiniCard(visit = visit, onClick = { onVisitClick(visit) })
        }
        if (showMore) {
            item { MoreMiniCard(onClick = onMoreClick) }
        }
    }
}

@Composable
private fun VisitMiniCard(visit: HomeVisitUiModel, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        modifier = Modifier
            .width(CARD_WIDTH)
            .border(BorderStroke(AppSize.border, AppTheme.semanticColors.line), shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = visit.complexName,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.semanticColors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        VisitWhenText(visit)
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            CriterionDots(
                label = stringResource(VisitCriterion.LIGHT.shortLabelRes()),
                score = visit.lightScore,
            )
            CriterionDots(
                label = stringResource(VisitCriterion.PARKING.shortLabelRes()),
                score = visit.parkingScore,
            )
        }
    }
}

/**
 * 날짜 자리 한 줄. 미완은 날짜 대신 강조색으로 재촉한다 — 임장은 바로 안 적으면
 * 기억이 흐려진다(기획서 NOTE-03). 예정은 코발트로 "아직 갈 곳"임을 알린다.
 */
@Composable
private fun VisitWhenText(visit: HomeVisitUiModel) {
    val (text, color) = when {
        visit.isPlanned ->
            stringResource(R.string.home_visit_planned_date, visit.visitedOnLabel) to
                MaterialTheme.colorScheme.primary

        visit.isIncomplete ->
            stringResource(R.string.home_visit_incomplete) to AppTheme.semanticColors.signalDeep

        else ->
            stringResource(
                R.string.home_visit_done_summary,
                visit.visitedOnLabel,
                visit.ratedCount,
                visit.totalCriteria,
            ) to AppTheme.semanticColors.ink45
    }
    Text(
        text = text,
        style = AppTextStyles.monoCaption,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** 상한을 넘는 기록이 있을 때만 붙는 마지막 카드. 누르면 매매 탭. */
@Composable
private fun MoreMiniCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.card)
    Box(
        modifier = Modifier
            .width(MORE_WIDTH)
            .height(CARD_MIN_HEIGHT)
            .border(BorderStroke(AppSize.border, AppTheme.semanticColors.line), shape)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.home_visit_more),
                style = AppTextStyles.monoCaption,
                color = AppTheme.semanticColors.ink45,
            )
        }
    }
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

private val CARD_WIDTH = 156.dp
private val MORE_WIDTH = 88.dp
private val CARD_MIN_HEIGHT = 108.dp

@Preview(showBackground = true)
@Composable
private fun VisitRailPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            VisitRail(
                visits = listOf(
                    previewVisit("A1", "관악우방", incomplete = true, light = 3, parking = 2),
                    previewVisit("A2", "동탄금호어울림", light = 4, parking = 5),
                    previewVisit("A3", "관악푸르지오아파트", planned = true, light = null, parking = null),
                ),
                showMore = true,
                onVisitClick = {},
                onMoreClick = {},
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
    light: Int?,
    parking: Int?,
) = HomeVisitUiModel(
    kaptCode = code,
    complexName = name,
    regionLabel = "서울특별시 관악구 봉천동",
    visitedOnLabel = "09.04",
    isPlanned = planned,
    isIncomplete = incomplete,
    lightScore = light,
    parkingScore = parking,
    ratedCount = if (incomplete) 2 else 4,
    totalCriteria = 4,
    viewedUnit = "84㎡ · 12층",
)
