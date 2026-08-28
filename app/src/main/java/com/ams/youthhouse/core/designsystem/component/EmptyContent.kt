package com.ams.youthhouse.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 `.emp` — 보여줄 게 없을 때의 중앙 정렬 안내.
 *
 * "없습니다"로 끝내지 않도록 [actions] 슬롯에 다음 행동을 넣을 수 있다.
 */
@Composable
fun EmptyContent(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.semanticColors.ink,
            textAlign = TextAlign.Center,
        )
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.semanticColors.ink45,
                textAlign = TextAlign.Center,
            )
        }
        actions()
    }
}
