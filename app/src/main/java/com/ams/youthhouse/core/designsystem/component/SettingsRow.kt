package com.ams.youthhouse.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 `.rows` — 테두리로 묶인 설정 행 그룹.
 *
 * 행 사이에 구분선을 자동으로 넣는다(마지막 행 뒤에는 넣지 않는다).
 */
@Composable
fun SettingsRowGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .border(
                border = BorderStroke(AppSize.border, AppTheme.semanticColors.line),
                shape = RoundedCornerShape(AppRadius.card),
            ),
        content = content,
    )
}

/**
 * 기획서 `.rw` — 좌측 제목(+ 부제), 우측 값.
 *
 * [onClick]이 있으면 눌러서 바꿀 수 있는 항목이고, 없으면 정보 표시다.
 */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xl),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.semanticColors.ink,
                )
                description?.let {
                    Text(
                        text = it,
                        style = AppTextStyles.footnote,
                        color = AppTheme.semanticColors.ink45,
                    )
                }
            }

            value?.let {
                Text(
                    text = if (onClick == null) it else "$it ›",
                    style = AppTextStyles.mono,
                    color = if (onClick == null) {
                        AppTheme.semanticColors.ink45
                    } else {
                        AppTheme.semanticColors.ink70
                    },
                    textAlign = TextAlign.End,
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                thickness = AppSize.border,
                color = AppTheme.semanticColors.line2,
            )
        }
    }
}
