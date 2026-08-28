package com.ams.youthhouse.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/**
 * 기획서 `.eb` — 성격이 다른 블록 사이의 경계 라벨.
 *
 * 홈이 "사라지는 것(공고 마감)"과 "쌓이는 것(임장 기록)" 두 덩어리로 나뉘는데,
 * 섹션 헤더([SectionHeader])보다 한 단계 위에서 그 경계를 긋는다.
 */
@Composable
fun BlockLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = AppTextStyles.monoCaption,
        color = AppTheme.semanticColors.ink45,
        modifier = modifier,
    )
}
