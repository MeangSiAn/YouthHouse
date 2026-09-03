package com.ams.youthhouse.feature.home.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.feature.home.presentation.guide.HomeGuide

/** 기획서 `.hscroll` + `.ccard` — 가이드 카드 가로 줄. */
@Composable
fun GuideRail(
    guides: List<HomeGuide>,
    onGuideClick: (HomeGuide) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        items(guides.size, key = { guides[it].name }) { index ->
            val guide = guides[index]
            GuideCard(guide = guide, onClick = { onGuideClick(guide) })
        }
    }
}

@Composable
private fun GuideCard(guide: HomeGuide, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        modifier = Modifier
            .width(CARD_WIDTH)
            .border(BorderStroke(AppSize.border, AppTheme.semanticColors.line), shape)
            .clip(shape)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IMAGE_HEIGHT)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = IMAGE_TINT_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = guide.emoji, style = MaterialTheme.typography.headlineMedium)
        }
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = stringResource(guide.tagRes),
                style = AppTextStyles.monoCaption,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(guide.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.semanticColors.ink,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val CARD_WIDTH = 168.dp
private val IMAGE_HEIGHT = 72.dp
private const val IMAGE_TINT_ALPHA = 0.10f

@Preview(showBackground = true)
@Composable
private fun GuideRailPreview() {
    AppTheme {
        GuideRail(
            guides = HomeGuide.entries,
            onGuideClick = {},
            modifier = Modifier.padding(AppSpacing.xl),
        )
    }
}
