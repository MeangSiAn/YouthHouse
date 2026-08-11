package com.ams.myjeonse.feature.home.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.ams.myjeonse.R
import com.ams.myjeonse.core.designsystem.theme.AppRadius
import com.ams.myjeonse.core.designsystem.theme.AppSpacing
import com.ams.myjeonse.core.designsystem.theme.AppTextStyles
import com.ams.myjeonse.core.designsystem.theme.AppTheme

/**
 * 기획서 `.urgent` — 가장 급한 마감 한 건.
 *
 * 검은 배경으로 주변 카드와 완전히 분리해서, 스크롤하기 전에 눈에 들어오게 한다.
 * 홈에서만 쓰는 조합이라 designsystem으로 올리지 않는다.
 */
@Composable
fun UrgentDeadlineCard(
    daysLeft: Int,
    title: String,
    deadlineText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.semanticColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.panel))
            .background(colors.board)
            .clickable(onClick = onClick)
            .padding(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = stringResource(R.string.home_urgent_label),
            style = AppTextStyles.monoCaption,
            color = colors.onBoardMuted,
        )

        Text(
            text = stringResource(R.string.home_dday_format, daysLeft),
            style = AppTextStyles.dday,
            color = colors.dday,
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBoard,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = deadlineText,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onBoardMuted,
        )

        Text(
            text = stringResource(R.string.home_urgent_action),
            style = MaterialTheme.typography.titleSmall,
            color = colors.board,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.md)
                .clip(RoundedCornerShape(AppRadius.button))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UrgentDeadlineCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UrgentDeadlineCard(
                daysLeft = 2,
                title = "[울산권] 2026년 기존주택등 매입임대주택 입주자 모집 공고",
                deadlineText = "2026.08.05에 접수가 끝납니다.",
                onClick = {},
            )
        }
    }
}
