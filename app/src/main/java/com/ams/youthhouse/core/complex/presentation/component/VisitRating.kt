package com.ams.youthhouse.core.complex.presentation.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.R
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 1~5점을 점 다섯 개로. "채광 4 · 소음 3"은 읽어야 알지만, 찬 점의 개수는 훑으면 보인다.
 *
 * 2점 이하는 붉게 칠한다 — 약점이 먼저 눈에 들어와야 한다. 홈·매매 탭·단지 상세·비교표가
 * 전부 이 하나를 쓰므로 어디서 봐도 같은 점이 같은 뜻이다.
 */
@Composable
fun RatingDots(
    score: Int?,
    modifier: Modifier = Modifier,
    activeColor: Color = score.defaultDotColor(),
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
                        if (score != null && step <= score) activeColor else AppTheme.semanticColors.line,
                    ),
            )
        }
    }
}

/** 항목 이름 + 점. 카드 안에서 여러 항목을 나란히 놓을 때 쓴다. */
@Composable
fun CriterionDots(
    label: String,
    score: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AppTextStyles.monoCaption,
            color = AppTheme.semanticColors.ink45,
        )
        RatingDots(score = score)
    }
}

@Composable
private fun Int?.defaultDotColor(): Color =
    if (this != null && this <= LOW_SCORE) {
        AppTheme.semanticColors.close
    } else {
        MaterialTheme.colorScheme.primary
    }

@StringRes
fun VisitCriterion.labelRes(): Int = when (this) {
    VisitCriterion.LIGHT -> R.string.note_criterion_light
    VisitCriterion.NOISE -> R.string.note_criterion_noise
    VisitCriterion.PARKING -> R.string.note_criterion_parking
    VisitCriterion.MANAGEMENT -> R.string.note_criterion_management
}

/** 카드처럼 좁은 자리용. "단지 관리"만 두 글자로 줄어든다. */
@StringRes
fun VisitCriterion.shortLabelRes(): Int = when (this) {
    VisitCriterion.MANAGEMENT -> R.string.note_criterion_management_short
    else -> labelRes()
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

/** 이 점수 이하는 약점으로 본다. */
const val LOW_SCORE = 2
private val RATING_DOT = 7.dp
