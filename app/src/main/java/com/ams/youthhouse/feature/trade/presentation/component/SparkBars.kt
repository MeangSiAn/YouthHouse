package com.ams.youthhouse.feature.trade.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 `.spark` — 월별 평균가 막대. 마지막(최신) 막대만 진한 색으로 강조한다.
 *
 * 값 축을 그리지 않는 대신 최솟값도 바닥에서 [MIN_FRACTION]만큼 띄운다.
 * 0부터 그리면 부동산 가격처럼 변동폭이 작은 시계열은 전부 같은 높이로 보인다.
 */
@Composable
fun SparkBars(
    values: List<Long>,
    modifier: Modifier = Modifier,
) {
    if (values.isEmpty()) return

    val barColor = AppTheme.semanticColors.ink25.copy(alpha = 0.45f)
    val highlightColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(BAR_AREA_HEIGHT),
    ) {
        val min = values.min()
        val max = values.max()
        val span = (max - min).coerceAtLeast(1)

        val gap = BAR_GAP.toPx()
        val barWidth = (size.width - gap * (values.size - 1)) / values.size

        values.forEachIndexed { index, value ->
            val fraction = MIN_FRACTION +
                (1f - MIN_FRACTION) * ((value - min).toFloat() / span)
            val barHeight = size.height * fraction
            drawRoundRect(
                color = if (index == values.lastIndex) highlightColor else barColor,
                topLeft = Offset(x = index * (barWidth + gap), y = size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(CORNER.toPx()),
            )
        }
    }
}

private val BAR_AREA_HEIGHT = 56.dp
private val BAR_GAP = 5.dp
private val CORNER = 3.dp
private const val MIN_FRACTION = 0.25f

@Preview(showBackground = true)
@Composable
private fun SparkBarsPreview() {
    AppTheme {
        SparkBars(values = listOf(111540, 113900, 114500, 116781, 121500))
    }
}
