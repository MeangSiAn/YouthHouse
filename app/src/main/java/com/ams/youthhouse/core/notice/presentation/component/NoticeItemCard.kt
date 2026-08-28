package com.ams.youthhouse.core.notice.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.core.designsystem.component.StatusLabel
import com.ams.youthhouse.core.designsystem.component.StatusTone
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.notice.presentation.model.NoticeStatus
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.notice.presentation.model.labelRes
import com.ams.youthhouse.core.notice.presentation.model.previewNoticeUiModel

/**
 * 기획서 `.card` — 홈과 공고 목록이 함께 쓴다.
 *
 * 상단에 기관·공급유형과 D-day 상태, 가운데 제목, 아래 보조 정보를 둔다.
 * 날짜를 나열하는 대신 D-day를 앞세워 급한 정도가 먼저 읽히게 한다.
 */
@Composable
fun NoticeItemCard(
    notice: NoticeUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(AppSize.border, AppTheme.semanticColors.line),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 분양은 공급유형(supplyTypeName)이 응답에 없어 분야 라벨로 대신한다.
                // 그래야 "LH"만 덩그러니 남지 않고 "LH · 공공분양"이 된다.
                val typeLabel = notice.supplyTypeName ?: stringResource(notice.category.labelRes)
                Text(
                    text = listOfNotNull(notice.supplyInstitutionName, typeLabel)
                        .joinToString(separator = " · "),
                    style = AppTextStyles.monoCaption,
                    color = AppTheme.semanticColors.ink45,
                )
                notice.statusLabel?.let { label ->
                    StatusLabel(text = label, tone = notice.status.toTone())
                }
            }

            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.semanticColors.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            notice.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = AppTextStyles.mono,
                    color = AppTheme.semanticColors.ink70,
                )
            }

            notice.applyPeriod?.let { period ->
                Text(
                    text = period,
                    style = AppTextStyles.monoCaption,
                    color = AppTheme.semanticColors.ink45,
                )
            }
        }
    }
}

private fun NoticeStatus.toTone(): StatusTone = when (this) {
    NoticeStatus.OPEN -> StatusTone.LIVE
    NoticeStatus.URGENT -> StatusTone.URGENT
    NoticeStatus.UPCOMING -> StatusTone.SOON
    NoticeStatus.CLOSED, NoticeStatus.UNKNOWN -> StatusTone.CLOSED
}

@Preview(showBackground = true)
@Composable
private fun NoticeItemCardPreview() {
    AppTheme {
        NoticeItemCard(
            notice = previewNoticeUiModel(),
            onClick = {},
        )
    }
}
