package com.ams.youthhouse.feature.home.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 SCREEN 02의 진단 유도 배너에 해당한다.
 *
 * 기획서는 "조건 미등록"이 전제지만 아직 내 조건 기능이 없으므로,
 * 지금 실제로 물어볼 수 있는 것(지역)만 정직하게 요청한다.
 */
@Composable
fun RegionUnsetBanner(
    onSelectRegionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.semanticColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.panel))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                border = BorderStroke(AppSize.borderStrong, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(AppRadius.panel),
            )
            .padding(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.home_region_unset_eyebrow),
            style = AppTextStyles.monoCaption,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.home_region_unset_title),
            style = MaterialTheme.typography.titleLarge,
            color = colors.ink,
        )
        Text(
            text = stringResource(R.string.home_region_unset_description),
            style = MaterialTheme.typography.bodySmall,
            color = colors.ink70,
        )
        Button(
            onClick = onSelectRegionClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.button),
        ) {
            Text(text = stringResource(R.string.home_region_unset_action))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegionUnsetBannerPreview() {
    AppTheme {
        RegionUnsetBanner(
            onSelectRegionClick = {},
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}
