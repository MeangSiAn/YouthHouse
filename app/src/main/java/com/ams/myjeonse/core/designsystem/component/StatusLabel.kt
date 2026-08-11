package com.ams.myjeonse.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ams.myjeonse.core.designsystem.theme.AppSize
import com.ams.myjeonse.core.designsystem.theme.AppSpacing
import com.ams.myjeonse.core.designsystem.theme.AppTextStyles
import com.ams.myjeonse.core.designsystem.theme.AppTheme
import androidx.compose.foundation.layout.Box

/**
 * 기획서 `.stt` — 색 점 + 상태 문구.
 *
 * 톤은 도메인 개념이 아니라 시각 단계다. 무엇을 어느 톤에 매핑할지는 호출부가 정한다.
 */
enum class StatusTone { LIVE, SOON, URGENT, CLOSED }

@Composable
fun StatusLabel(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
) {
    val color = tone.toColor()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AppSize.statusDot)
                .background(color = color, shape = CircleShape),
        )
        Text(
            text = text,
            style = AppTextStyles.monoCaption,
            color = color,
        )
    }
}

@Composable
private fun StatusTone.toColor(): Color = when (this) {
    StatusTone.LIVE -> AppTheme.semanticColors.live
    StatusTone.SOON -> AppTheme.semanticColors.signal
    StatusTone.URGENT -> AppTheme.semanticColors.close
    StatusTone.CLOSED -> AppTheme.semanticColors.ink45
}
