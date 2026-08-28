package com.ams.youthhouse.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

data class SummaryEntry(
    val label: String,
    val value: String,
)

/**
 * 기획서 `.st4` — 핵심 수치 2×2 격자.
 *
 * 값이 없는 항목은 호출부에서 걸러 넘긴다. 홀수 개면 마지막 칸은 비워 둔다.
 */
@Composable
fun SummaryGrid(
    entries: List<SummaryEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(AppSize.border, AppTheme.semanticColors.line),
                shape = RoundedCornerShape(AppRadius.card),
            ),
    ) {
        entries.chunked(2).forEachIndexed { index, rowEntries ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowEntries.forEach { entry -> SummaryCell(entry) }
                // 홀수로 끝나면 남은 칸을 비워 격자를 유지한다.
                if (rowEntries.size == 1) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
            if (index != entries.chunked(2).lastIndex) {
                HorizontalDivider(
                    thickness = AppSize.border,
                    color = AppTheme.semanticColors.line2,
                )
            }
        }
    }
}

@Composable
private fun RowScope.SummaryCell(entry: SummaryEntry) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
    ) {
        Text(
            text = entry.label,
            style = AppTextStyles.monoCaption,
            color = AppTheme.semanticColors.ink45,
        )
        Text(
            text = entry.value,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.semanticColors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryGridPreview() {
    AppTheme {
        SummaryGrid(
            entries = listOf(
                SummaryEntry("보증금", "16,612,000원"),
                SummaryEntry("월 임대료", "83,060원"),
                SummaryEntry("공급 호수", "270세대"),
                SummaryEntry("총 세대수", "562세대"),
            ),
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}
