package com.ams.youthhouse.feature.trade.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.core.common.format.formatManwonAsEokMan
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.feature.trade.domain.model.TrendPoint

/**
 * 월별 실거래 추이 — 위는 평균가, 아래는 거래건수.
 *
 * 주식 차트의 가격/거래량 배치를 그대로 빌려온다. 가격만 보면 "12억에 한 건 팔린 달"과
 * "12억에 열 건 팔린 달"이 같아 보이는데, 거래가 적은 달의 평균은 신뢰도가 낮다.
 * 두 축을 나란히 두면 그 차이가 한눈에 읽힌다.
 *
 * 가격 막대는 0이 아니라 최솟값 기준으로 스케일한다 — 부동산은 변동폭이 작아
 * 0부터 그리면 모든 달이 같은 높이가 된다. 대신 아래에 실제 최저·최고를 적어
 * 막대 높이가 절대량이 아니라는 것을 드러낸다.
 */
@Composable
fun DealTrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return

    val prices = points.map(TrendPoint::averageAmount)
    val minPrice = prices.min()
    val maxPrice = prices.max()

    val barColor = AppTheme.semanticColors.ink25.copy(alpha = 0.4f)
    val highlightColor = MaterialTheme.colorScheme.primary
    val volumeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    val volumeHighlight = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(PRICE_HEIGHT),
        ) {
            val span = (maxPrice - minPrice).coerceAtLeast(1)
            val gap = BAR_GAP.toPx()
            val barWidth = (size.width - gap * (points.size - 1)) / points.size

            points.forEachIndexed { index, point ->
                val fraction = MIN_FRACTION + (1f - MIN_FRACTION) *
                    ((point.averageAmount - minPrice).toFloat() / span)
                val barHeight = size.height * fraction
                drawBar(
                    index = index,
                    barWidth = barWidth,
                    gap = gap,
                    top = size.height - barHeight,
                    height = barHeight,
                    color = if (index == points.lastIndex) highlightColor else barColor,
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(VOLUME_HEIGHT),
        ) {
            val maxVolume = points.maxOf(TrendPoint::dealCount).coerceAtLeast(1)
            val gap = BAR_GAP.toPx()
            val barWidth = (size.width - gap * (points.size - 1)) / points.size

            points.forEachIndexed { index, point ->
                // 거래 0건인 달도 흔적을 남긴다. 아예 비면 "데이터가 없는 달"과 구분이 안 된다.
                val barHeight = (size.height * point.dealCount / maxVolume)
                    .coerceAtLeast(MIN_VOLUME_HEIGHT.toPx())
                drawBar(
                    index = index,
                    barWidth = barWidth,
                    gap = gap,
                    top = size.height - barHeight,
                    height = barHeight,
                    color = if (index == points.lastIndex) volumeHighlight else volumeColor,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AxisLabel(points.first().yearMonth.formatYearMonth())
            AxisLabel(
                text = "최저 ${minPrice.formatManwonAsEokMan()} · " +
                    "최고 ${maxPrice.formatManwonAsEokMan()}",
            )
            AxisLabel(points.last().yearMonth.formatYearMonth())
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBar(
    index: Int,
    barWidth: Float,
    gap: Float,
    top: Float,
    height: Float,
    color: Color,
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x = index * (barWidth + gap), y = top),
        size = Size(barWidth, height),
        cornerRadius = CornerRadius(CORNER.toPx()),
    )
}

@Composable
private fun AxisLabel(text: String) {
    Text(
        text = text,
        style = AppTextStyles.monoCaption,
        color = AppTheme.semanticColors.ink45,
    )
}

/** `202604` → `26.04` */
private fun String.formatYearMonth(): String =
    if (length == 6) "${substring(2, 4)}.${substring(4, 6)}" else this

private val PRICE_HEIGHT = 84.dp
private val VOLUME_HEIGHT = 22.dp
private val BAR_GAP = 4.dp
private val CORNER = 3.dp
private val MIN_VOLUME_HEIGHT = 2.dp
private const val MIN_FRACTION = 0.22f

@Preview(showBackground = true)
@Composable
private fun DealTrendChartPreview() {
    AppTheme {
        DealTrendChart(
            points = listOf(
                TrendPoint("202510", 85_000, 2),
                TrendPoint("202511", 92_000, 5),
                TrendPoint("202512", 90_500, 3),
                TrendPoint("202601", 94_000, 4),
                TrendPoint("202602", 103_000, 11),
                TrendPoint("202603", 106_500, 9),
                TrendPoint("202604", 111_540, 10),
                TrendPoint("202605", 113_900, 13),
                TrendPoint("202606", 114_500, 2),
                TrendPoint("202607", 116_781, 8),
                TrendPoint("202608", 121_500, 1),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
