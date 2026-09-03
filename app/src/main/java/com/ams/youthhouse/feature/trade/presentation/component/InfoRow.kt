package com.ams.youthhouse.feature.trade.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 라벨-값 한 줄. 값이 없으면 행 자체를 그리지 않는다.
 *
 * K-apt는 단지마다 채워 주는 필드가 달라서, 빈 값을 "-"로 채우면
 * 화면이 대시로 뒤덮인다. 아는 것만 보여주는 편이 정직하고 읽기도 쉽다.
 */
@Composable
fun InfoRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    if (value.isNullOrBlank()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.semanticColors.ink45,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.semanticColors.ink,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(2f),
        )
    }
}

/** 주변 시설 한 항목. 개수가 단지마다 들쭉날쭉해 목록보다 칩이 자리를 덜 먹는다. */
@Composable
fun FacilityChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = AppTextStyles.footnote,
        color = AppTheme.semanticColors.ink70,
        modifier = modifier
            .background(
                color = AppTheme.semanticColors.line2,
                shape = RoundedCornerShape(AppRadius.badge),
            )
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    )
}

@Preview(showBackground = true)
@Composable
private fun InfoRowPreview() {
    AppTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            InfoRow(label = "건설사", value = "대우건설")
            InfoRow(label = "주차", value = "2,110대 (지상 120 · 지하 1,990)")
            InfoRow(label = "빈 값", value = null)
            FacilityChip(text = "초등학교(봉천초등학교)")
        }
    }
}
