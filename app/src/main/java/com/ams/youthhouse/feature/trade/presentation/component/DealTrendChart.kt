package com.ams.youthhouse.feature.trade.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.core.common.format.formatManwonAsEokMan
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.feature.trade.domain.model.TrendPoint
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 월별 실거래 추이 차트.
 *
 * 시세 차트의 관례를 그대로 따른다 — 위는 가격 선과 영역, 아래는 거래량 막대.
 * 가격만 보면 "12억에 한 건 팔린 달"과 "12억에 열세 건 팔린 달"이 같아 보이는데,
 * 거래가 적은 달의 평균은 신뢰도가 낮다. 거래량을 나란히 두면 그 차이가 읽힌다.
 *
 * 세로 축은 0이 아니라 구간 최솟값부터 시작한다. 부동산은 변동폭이 작아 0부터 그리면
 * 모든 달이 평평해진다. 대신 축에 실제 금액 눈금을 찍어 높이가 절대량이 아님을 밝힌다.
 *
 * 누르거나 좌우로 끌면 그 달이 선택된다.
 *
 * 가격선·거래량·터치 판정이 [ChartGeometry] 하나를 공유한다. 예전에는 셋이 각자
 * 가로 좌표를 계산해서, 같은 달인데도 막대가 꼭짓점 아래에 오지 않고 누른 지점과
 * 선택된 달도 어긋났다.
 */
@Composable
fun DealTrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) return

    var selectedIndex by remember(points) { mutableIntStateOf(points.lastIndex) }
    val selected = points[selectedIndex.coerceIn(points.indices)]

    val prices = points.map(TrendPoint::averageAmount)
    val minPrice = prices.min()
    val maxPrice = prices.max()

    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = AppTheme.semanticColors.line
    val volumeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    val volumeSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    val dotFillColor = MaterialTheme.colorScheme.surface

    val textMeasurer = rememberTextMeasurer()
    val axisStyle = AppTextStyles.monoCaption.copy(color = AppTheme.semanticColors.ink45)

    // 축 라벨 폭은 그릴 때와 터치를 판정할 때 같은 값을 써야 하므로 한 번만 재 둔다.
    val axisLabelWidth = remember(maxPrice, axisStyle) {
        textMeasurer.measure(maxPrice.formatEokShort(), axisStyle).size.width.toFloat()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        SelectedPointHeader(point = selected)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT)
                .pointerInput(points, axisLabelWidth) {
                    val geometry = chartGeometry(size.width.toFloat(), axisLabelWidth, points.size)
                    detectTapGestures { offset ->
                        selectedIndex = geometry.indexAt(offset.x)
                    }
                }
                .pointerInput(points, axisLabelWidth) {
                    val geometry = chartGeometry(size.width.toFloat(), axisLabelWidth, points.size)
                    detectHorizontalDragGestures { change, _ ->
                        selectedIndex = geometry.indexAt(change.position.x)
                    }
                },
        ) {
            val geometry = chartGeometry(size.width, axisLabelWidth, points.size)

            val priceTop = VERTICAL_INSET.toPx()
            val priceBottom = size.height - VOLUME_HEIGHT.toPx() - SECTION_GAP.toPx()
            val volumeTop = priceBottom + SECTION_GAP.toPx()
            val volumeBottom = size.height
            val priceSpan = (maxPrice - minPrice).coerceAtLeast(1)

            fun yFor(amount: Long): Float =
                priceBottom - (priceBottom - priceTop) * ((amount - minPrice).toFloat() / priceSpan)

            // 눈금 세 줄 — 최고·중간·최저. 오른쪽에 실제 금액을 적는다.
            listOf(maxPrice, (maxPrice + minPrice) / 2, minPrice).forEach { value ->
                val y = yFor(value)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(geometry.chartWidth, y),
                    strokeWidth = GRID_STROKE.toPx(),
                )
                val layout = textMeasurer.measure(value.formatEokShort(), axisStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = size.width - layout.size.width,
                        y = y - layout.size.height / 2f,
                    ),
                )
            }

            val linePath = Path()
            points.forEachIndexed { index, point ->
                val x = geometry.x(index)
                val y = yFor(point.averageAmount)
                if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
            }

            // 선 아래를 옅게 채워 "얼마나 올라와 있는지"가 면적으로도 읽히게 한다.
            drawPath(
                path = Path().apply {
                    addPath(linePath)
                    lineTo(geometry.x(points.lastIndex), priceBottom)
                    lineTo(geometry.x(0), priceBottom)
                    close()
                },
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.20f), Color.Transparent),
                    startY = priceTop,
                    endY = priceBottom,
                ),
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = LINE_STROKE.toPx()),
            )

            // 십자선은 거래량 막대까지 내려가 "이 달의 가격과 건수"를 한 줄로 묶는다.
            val selectedX = geometry.x(selectedIndex)
            drawLine(
                color = lineColor.copy(alpha = 0.35f),
                start = Offset(selectedX, priceTop),
                end = Offset(selectedX, volumeBottom),
                strokeWidth = GRID_STROKE.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )

            points.forEachIndexed { index, point ->
                val center = Offset(geometry.x(index), yFor(point.averageAmount))
                if (index == selectedIndex) {
                    drawCircle(dotFillColor, radius = SELECTED_DOT.toPx(), center = center)
                    drawCircle(
                        color = lineColor,
                        radius = SELECTED_DOT.toPx(),
                        center = center,
                        style = Stroke(width = LINE_STROKE.toPx()),
                    )
                } else {
                    drawCircle(lineColor, radius = DOT.toPx(), center = center)
                }
            }

            val maxVolume = points.maxOf(TrendPoint::dealCount).coerceAtLeast(1)
            val volumeHeight = volumeBottom - volumeTop
            points.forEachIndexed { index, point ->
                // 0건인 달도 흔적을 남긴다. 아예 비면 "자료가 없는 달"과 구분되지 않는다.
                val barHeight = (volumeHeight * point.dealCount / maxVolume)
                    .coerceAtLeast(MIN_VOLUME_HEIGHT.toPx())
                drawRoundRect(
                    color = if (index == selectedIndex) volumeSelectedColor else volumeColor,
                    topLeft = Offset(
                        x = geometry.x(index) - geometry.barWidth / 2f,
                        y = volumeBottom - barHeight,
                    ),
                    size = Size(geometry.barWidth, barHeight),
                    cornerRadius = CornerRadius(VOLUME_CORNER.toPx()),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AxisLabel(points.first().yearMonth.formatYearMonth())
            AxisLabel(points.last().yearMonth.formatYearMonth())
        }
    }
}

/**
 * 차트의 가로 좌표 계산을 한 곳에 모은다.
 *
 * 양 끝 막대가 잘리지 않도록 막대 반 폭만큼 안쪽에서 시작·끝나고, 가격선의 꼭짓점도
 * 같은 위치를 쓴다. [indexAt]은 [x]의 역함수라 터치 지점과 선택된 달이 항상 일치한다.
 */
private class ChartGeometry(
    val chartWidth: Float,
    val barWidth: Float,
    private val count: Int,
) {
    private val halfBar = barWidth / 2f
    private val span = (chartWidth - barWidth).coerceAtLeast(1f)

    fun x(index: Int): Float =
        if (count <= 1) chartWidth / 2f else halfBar + span * index / (count - 1)

    fun indexAt(x: Float): Int =
        if (count <= 1) {
            0
        } else {
            (((x - halfBar) / span) * (count - 1)).roundToInt().coerceIn(0, count - 1)
        }
}

private fun Density.chartGeometry(
    totalWidth: Float,
    axisLabelWidth: Float,
    count: Int,
): ChartGeometry {
    val chartWidth = (totalWidth - axisLabelWidth - AXIS_GAP.toPx()).coerceAtLeast(1f)
    val slot = chartWidth / count.coerceAtLeast(1)
    val barWidth = min(slot - VOLUME_GAP.toPx(), MAX_BAR_WIDTH.toPx()).coerceAtLeast(1f)
    return ChartGeometry(chartWidth = chartWidth, barWidth = barWidth, count = count)
}

@Composable
private fun SelectedPointHeader(point: TrendPoint) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = point.yearMonth.formatYearMonth(),
            style = AppTextStyles.mono,
            color = AppTheme.semanticColors.ink45,
        )
        Text(
            text = point.averageAmount.formatManwonAsEokMan(),
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.semanticColors.ink,
        )
        Text(
            text = "${point.dealCount}건",
            style = AppTextStyles.mono,
            color = AppTheme.semanticColors.ink45,
        )
    }
}

@Composable
private fun AxisLabel(text: String) {
    Text(
        text = text,
        style = AppTextStyles.monoCaption,
        color = AppTheme.semanticColors.ink45,
    )
}

/** 축 눈금용 짧은 표기. `121500` → `12.2억` */
private fun Long.formatEokShort(): String {
    val eok = this / 10_000.0
    return if (eok >= 10) "%.1f억".format(eok) else "%.2f억".format(eok)
}

/** `202604` → `26.04` */
private fun String.formatYearMonth(): String =
    if (length == 6) "${substring(2, 4)}.${substring(4, 6)}" else this

private val CHART_HEIGHT = 176.dp
private val VOLUME_HEIGHT = 26.dp
private val SECTION_GAP = 10.dp
private val VERTICAL_INSET = 10.dp
private val AXIS_GAP = 8.dp
private val GRID_STROKE = 1.dp
private val LINE_STROKE = 2.dp
private val DOT = 2.5.dp
private val SELECTED_DOT = 5.dp
private val VOLUME_GAP = 5.dp
private val VOLUME_CORNER = 2.dp
private val MIN_VOLUME_HEIGHT = 2.dp
private val MAX_BAR_WIDTH = 18.dp

@Preview(showBackground = true)
@Composable
private fun DealTrendChartPreview() {
    AppTheme {
        DealTrendChart(
            points = listOf(
                TrendPoint("202510", 95_930, 2),
                TrendPoint("202511", 99_000, 5),
                TrendPoint("202512", 97_500, 3),
                TrendPoint("202601", 101_000, 4),
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
