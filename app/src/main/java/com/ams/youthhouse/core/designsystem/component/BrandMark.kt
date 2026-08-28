package com.ams.youthhouse.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.designsystem.theme.Palette

/**
 * 브랜드 심볼 — 공고 목록(게시판)과 우상단 알림 점.
 *
 * 기획서(`alimi-splash.html`)의 SVG 좌표를 그대로 옮겼다. 원본 뷰포트가 78×78이라
 * 모든 좌표를 그 기준으로 두고 [size]에 맞춰 배율만 바꾼다. 벡터 드로어블을 따로 두지 않고
 * 여기 한 곳에서만 그리므로 심볼 정의가 갈라지지 않는다.
 *
 * 인트로가 요소를 순차로 등장시키므로 부분별 등장 정도를 인자로 받는다.
 * 기본값은 전부 완성 상태라 정적으로 쓸 때는 [modifier]만 주면 된다.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = BrandMarkDefaults.size,
    boardColor: Color = Color.White,
    dotColor: Color = Palette.Coral,
    boardAlpha: Float = 1f,
    line1Alpha: Float = 1f,
    line2Alpha: Float = 1f,
    line3Alpha: Float = 1f,
    dotAlpha: Float = 1f,
    dotScale: Float = 1f,
) {
    Canvas(modifier = modifier.size(size)) {
        // 78 뷰포트 좌표 → 실제 픽셀
        val unit = this.size.minDimension / VIEWPORT
        fun v(value: Float) = value * unit

        drawRoundRect(
            color = boardColor,
            alpha = boardAlpha,
            topLeft = Offset(v(12f), v(14f)),
            size = Size(v(54f), v(52f)),
            cornerRadius = CornerRadius(v(9f)),
            style = Stroke(width = v(STROKE)),
        )

        // 목록이 한 줄씩 채워지는 동작. 나타나면서 왼쪽에서 제자리로 밀려 들어온다.
        fun drawRow(endX: Float, y: Float, alpha: Float) {
            val slide = v(LINE_SLIDE * (1f - alpha))
            drawLine(
                color = boardColor,
                alpha = alpha,
                start = Offset(v(24f) + slide, v(y)),
                end = Offset(v(endX) + slide, v(y)),
                strokeWidth = v(STROKE),
                cap = StrokeCap.Round,
            )
        }

        drawRow(endX = 50f, y = 31f, alpha = line1Alpha)
        drawRow(endX = 54f, y = 40f, alpha = line2Alpha)
        drawRow(endX = 42f, y = 49f, alpha = line3Alpha)

        drawCircle(
            color = dotColor,
            alpha = dotAlpha,
            radius = v(9f) * dotScale,
            center = Offset(v(60f), v(18f)),
        )
    }
}

object BrandMarkDefaults {
    /** 기획서 스플래시 기준 크기. 24dp까지 줄여도 판독된다. */
    val size = 78.dp
}

private const val VIEWPORT = 78f
private const val STROKE = 3.4f

/** 라인이 등장할 때 밀려 들어오는 거리(뷰포트 기준). */
private const val LINE_SLIDE = -7f

@Preview(showBackground = true, backgroundColor = 0xFF1E3FA0)
@Composable
private fun BrandMarkPreview() {
    AppTheme {
        BrandMark()
    }
}
