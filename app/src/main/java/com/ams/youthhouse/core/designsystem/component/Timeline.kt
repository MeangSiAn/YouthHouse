package com.ams.youthhouse.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/** 타임라인 한 단계의 진행 상태. 도메인이 아니라 시각 단계다. */
enum class TimelineState { DONE, CURRENT, UPCOMING }

data class TimelineItem(
    val date: String,
    val title: String,
    val description: String? = null,
    val state: TimelineState,
)

/**
 * 기획서 `.tl` — 세로선으로 이어진 단계 목록.
 *
 * 지난 단계는 채운 점, 현재 단계는 강조색 점 + 굵은 제목, 남은 단계는 빈 점.
 */
@Composable
fun Timeline(
    items: List<TimelineItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            TimelineRow(item = item, isLast = index == items.lastIndex)
        }
    }
}

@Composable
private fun TimelineRow(
    item: TimelineItem,
    isLast: Boolean,
) {
    val colors = AppTheme.semanticColors
    val accent = when (item.state) {
        TimelineState.DONE -> colors.live
        TimelineState.CURRENT -> colors.close
        TimelineState.UPCOMING -> colors.ink25
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // 점과 연결선
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(DOT_COLUMN_WIDTH),
        ) {
            Box(
                modifier = Modifier
                    .padding(top = AppSpacing.xs)
                    .size(DOT_SIZE)
                    .background(
                        color = if (item.state == TimelineState.UPCOMING) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            accent
                        },
                        shape = CircleShape,
                    )
                    .border(width = DOT_BORDER, color = accent, shape = CircleShape),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = AppSpacing.xs)
                        .width(LINE_WIDTH)
                        .height(LINE_HEIGHT)
                        .background(colors.line),
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(start = AppSpacing.md, bottom = if (isLast) 0.dp else AppSpacing.lg)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
        ) {
            Text(
                text = item.date,
                style = AppTextStyles.monoCaption,
                color = colors.ink45,
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (item.state == TimelineState.CURRENT) accent else colors.ink,
            )
            item.description?.let {
                Text(
                    text = it,
                    style = AppTextStyles.footnote,
                    color = colors.ink45,
                )
            }
        }
    }
}

private val DOT_COLUMN_WIDTH = 16.dp
private val DOT_SIZE = 11.dp
private val DOT_BORDER = 2.5.dp
private val LINE_WIDTH = 2.dp
private val LINE_HEIGHT = 34.dp

@Preview(showBackground = true)
@Composable
private fun TimelinePreview() {
    AppTheme {
        Timeline(
            items = listOf(
                TimelineItem("2026.08.13", "모집공고", state = TimelineState.DONE),
                TimelineItem(
                    date = "2026.08.25 ~ 2026.08.27",
                    title = "접수",
                    description = "오늘 기준 2일 남음",
                    state = TimelineState.CURRENT,
                ),
                TimelineItem("2026.12.08", "당첨자 발표", state = TimelineState.UPCOMING),
            ),
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}
