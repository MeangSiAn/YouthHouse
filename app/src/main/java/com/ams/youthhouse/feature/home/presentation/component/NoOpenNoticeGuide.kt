package com.ams.youthhouse.feature.home.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.component.EmptyContent
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 SCREEN 10 — 지금 접수중인 공고가 없을 때.
 *
 * "없습니다"로 끝내면 앱을 지운다는 게 기획 의도라, 다음 시기를 날짜로 알려주고
 * 지금 할 수 있는 행동 두 가지를 남긴다.
 */
@Composable
fun NoOpenNoticeGuide(
    regionName: String,
    nextOpenDate: String?,
    onChangeRegionClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyContent(
        title = stringResource(R.string.home_empty_title, regionName),
        description = nextOpenDate
            ?.let { stringResource(R.string.home_empty_next_open, it) }
            ?: stringResource(R.string.home_empty_no_next_open),
        modifier = modifier,
    ) {
        Button(
            onClick = onChangeRegionClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.button),
        ) {
            Text(text = stringResource(R.string.home_empty_change_region))
        }
        OutlinedButton(
            onClick = onSeeAllClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.button),
        ) {
            Text(text = stringResource(R.string.home_empty_see_all))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoOpenNoticeGuidePreview() {
    AppTheme {
        NoOpenNoticeGuide(
            regionName = "서울특별시",
            nextOpenDate = "2026.08.16",
            onChangeRegionClick = {},
            onSeeAllClick = {},
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}
