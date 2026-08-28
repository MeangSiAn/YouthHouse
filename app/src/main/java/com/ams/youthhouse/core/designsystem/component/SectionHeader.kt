package com.ams.youthhouse.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 `.st` — 좌측 제목 + 우측 보조 텍스트.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.semanticColors.ink,
        )
        trailingText?.let {
            Text(
                text = it,
                style = AppTextStyles.monoCaption,
                // 눌러서 갈 수 있는 곳이면 색으로 그 사실을 알린다.
                color = if (onTrailingClick == null) {
                    AppTheme.semanticColors.ink45
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = if (onTrailingClick == null) {
                    Modifier
                } else {
                    Modifier.clickable(onClick = onTrailingClick)
                },
            )
        }
    }
}
