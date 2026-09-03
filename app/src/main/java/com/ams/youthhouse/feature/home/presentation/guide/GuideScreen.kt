package com.ams.youthhouse.feature.home.presentation.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import java.util.Locale

/**
 * 가이드 글 한 편. 상태가 없어 ViewModel을 두지 않는다 — 리소스를 읽어 그리는 게 전부다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    guide: HomeGuide,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.home_section_guides)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.notice_detail_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
        ) {
            GuideHeader(guide)

            Text(
                text = stringResource(guide.leadRes),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.semanticColors.ink70,
            )

            HorizontalDivider(thickness = AppSize.border, color = AppTheme.semanticColors.line)

            val titles = stringArrayResource(guide.pointTitlesRes)
            val bodies = stringArrayResource(guide.pointBodiesRes)
            // 두 배열 길이가 어긋나면 짧은 쪽까지만 — 리소스 실수로 화면이 죽으면 안 된다.
            titles.zip(bodies).forEachIndexed { index, (title, body) ->
                GuidePoint(number = index + 1, title = title, body = body)
            }

            FootnoteText(text = stringResource(R.string.guide_footnote))
        }
    }
}

@Composable
private fun GuideHeader(guide: HomeGuide) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Box(
            modifier = Modifier
                .size(HERO_SIZE)
                .clip(RoundedCornerShape(AppRadius.panel))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = HERO_TINT_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = guide.emoji, style = MaterialTheme.typography.headlineMedium)
        }
        Text(
            text = stringResource(guide.tagRes),
            style = AppTextStyles.monoCaption,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(guide.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.semanticColors.ink,
        )
    }
}

@Composable
private fun GuidePoint(number: Int, title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = String.format(Locale.US, "%02d", number),
            style = AppTextStyles.mono,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(NUMBER_WIDTH),
        )
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.semanticColors.ink,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.semanticColors.ink70,
            )
        }
    }
}

private val HERO_SIZE = 56.dp
private val NUMBER_WIDTH = 28.dp
private const val HERO_TINT_ALPHA = 0.10f

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GuideScreenPreview() {
    AppTheme {
        GuideScreen(guide = HomeGuide.VISIT_CHECKLIST, onBackClick = {})
    }
}
