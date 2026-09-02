package com.ams.youthhouse.feature.settings.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 dev2.0 `.privacy` — 마이 탭 최상단의 개인정보 미수집 안내.
 *
 * 설정 항목보다 위에 두는 이유: 계정이 없는 앱은 "왜 로그인이 없지?"라는
 * 의문을 만드는데, 그 답("수집하지 않아서")을 먼저 보여주는 것이 이 배너다.
 */
@Composable
fun PrivacyBanner(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppRadius.panel)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(AppSize.borderStrong, AppTheme.semanticColors.live), shape)
            .background(AppTheme.semanticColors.liveTint, shape)
            .padding(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.my_privacy_title),
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.semanticColors.ink,
        )
        Text(
            text = stringResource(R.string.my_privacy_body),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.semanticColors.ink70,
        )
        Text(
            text = stringResource(R.string.my_privacy_points),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.semanticColors.ink70,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyBannerPreview() {
    AppTheme {
        PrivacyBanner(modifier = Modifier.padding(AppSpacing.xl))
    }
}
