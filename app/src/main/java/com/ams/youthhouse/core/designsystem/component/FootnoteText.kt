package com.ams.youthhouse.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme

/** 기획서 `.fn` — 화면 하단 면책 문구. */
@Composable
fun FootnoteText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = AppTextStyles.footnote,
        color = AppTheme.semanticColors.ink45,
        modifier = modifier,
    )
}
