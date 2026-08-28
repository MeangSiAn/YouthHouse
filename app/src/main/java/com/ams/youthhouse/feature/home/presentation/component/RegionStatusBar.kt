package com.ams.youthhouse.feature.home.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 `.regionbar` — 선택한 지역의 공고 현황.
 *
 * 기획서는 `접수중 / 예정 / 마감` 3분할이지만, 이 API는 이미 마감된 공고를 주지 않아
 * "마감"이 항상 0이 된다. 그 자리를 **오늘 마감**으로 바꿔 그리드를 살렸다.
 */
@Composable
fun RegionStatusBar(
    openCount: Int,
    upcomingCount: Int,
    closingTodayCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.semanticColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(AppSize.border, colors.line),
                shape = RoundedCornerShape(AppRadius.card),
            )
            .padding(vertical = AppSpacing.lg),
    ) {
        StatCell(
            count = openCount,
            label = stringResource(R.string.home_stat_open),
            color = colors.live,
        )
        StatCell(
            count = upcomingCount,
            label = stringResource(R.string.home_stat_upcoming),
            color = colors.signal,
        )
        StatCell(
            count = closingTodayCount,
            label = stringResource(R.string.home_stat_closing_today),
            color = colors.close,
        )
    }
}

@Composable
private fun RowScope.StatCell(
    count: Int,
    label: String,
    color: Color,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
    ) {
        Text(
            text = count.toString(),
            style = AppTextStyles.statNumber,
            color = color,
        )
        Text(
            text = label,
            style = AppTextStyles.footnote,
            color = AppTheme.semanticColors.ink45,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegionStatusBarPreview() {
    AppTheme {
        RegionStatusBar(
            openCount = 6,
            upcomingCount = 4,
            closingTodayCount = 2,
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}
