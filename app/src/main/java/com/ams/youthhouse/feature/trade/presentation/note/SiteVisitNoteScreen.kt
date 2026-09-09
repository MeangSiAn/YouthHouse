package com.ams.youthhouse.feature.trade.presentation.note

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.R
import com.ams.youthhouse.core.common.format.formatYearMonthDay
import com.ams.youthhouse.core.common.time.utcMidnightMillisToYyyyMmDd
import com.ams.youthhouse.core.common.time.yyyyMmDdToUtcMidnightMillis
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.complex.domain.model.ComplexSnapshot
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings
import com.ams.youthhouse.feature.trade.presentation.component.labelRes

/**
 * 기획서 dev2.0 SCREEN 09 — 임장노트.
 *
 * "현장에서 한 손으로 입력할 수 있어야 한다." 그래서 점수는 타이핑이 아니라 탭이고,
 * 텍스트 입력은 본 매물·도보 분·메모 셋뿐이다. 사진은 이번 판에 없다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteVisitNoteScreen(
    uiState: SiteVisitNoteContract.State,
    onAction: (SiteVisitNoteContract.Action) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        // 키보드가 올라오면 저장 버튼까지 같이 올라온다.
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = stringResource(R.string.note_title))
                        Text(
                            text = uiState.complexName,
                            style = AppTextStyles.monoCaption,
                            color = AppTheme.semanticColors.ink45,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.notice_detail_back),
                        )
                    }
                },
                actions = {
                    if (uiState.isExisting) {
                        IconButton(
                            onClick = { onAction(SiteVisitNoteContract.Action.DeleteClicked) },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.note_delete),
                                tint = AppTheme.semanticColors.ink45,
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            SaveBar(
                enabled = uiState.canSave,
                onClick = { onAction(SiteVisitNoteContract.Action.SaveClicked) },
            )
        },
    ) { innerPadding ->
        if (!uiState.isLoaded) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            NoteForm(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (uiState.isDatePickerShown) {
        VisitDatePickerDialog(
            initialYyyyMmDd = uiState.visitedOn,
            onPicked = { onAction(SiteVisitNoteContract.Action.VisitedOnPicked(it)) },
            onDismiss = { onAction(SiteVisitNoteContract.Action.DatePickerDismissed) },
        )
    }

    if (uiState.isDeleteDialogShown) {
        DeleteDialog(
            onConfirm = { onAction(SiteVisitNoteContract.Action.DeleteConfirmed) },
            onDismiss = { onAction(SiteVisitNoteContract.Action.DeleteDismissed) },
        )
    }
}

@Composable
private fun NoteForm(
    uiState: SiteVisitNoteContract.State,
    onAction: (SiteVisitNoteContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fieldShape = RoundedCornerShape(AppRadius.card)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        VisitedOnField(
            visitedOn = uiState.visitedOn,
            onClick = { onAction(SiteVisitNoteContract.Action.DatePickerOpened) },
        )

        OutlinedTextField(
            value = uiState.viewedUnit,
            onValueChange = { onAction(SiteVisitNoteContract.Action.ViewedUnitChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.note_viewed_unit)) },
            placeholder = { Text(text = stringResource(R.string.note_viewed_unit_hint)) },
            singleLine = true,
            shape = fieldShape,
        )

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            SectionHeader(
                title = stringResource(R.string.note_section_checks),
                trailingText = stringResource(R.string.note_scale),
            )
            VisitCriterion.entries.forEach { criterion ->
                RatingRow(
                    label = stringResource(criterion.labelRes()),
                    score = uiState.ratings[criterion],
                    onScoreChanged = { score ->
                        onAction(SiteVisitNoteContract.Action.RatingChanged(criterion, score))
                    },
                )
            }
        }

        OutlinedTextField(
            value = uiState.walkMinutesText,
            onValueChange = { onAction(SiteVisitNoteContract.Action.WalkMinutesChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.note_walk_to_station)) },
            suffix = { Text(text = stringResource(R.string.note_walk_unit)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = fieldShape,
        )

        ElevatorField(
            condition = uiState.elevatorCondition,
            onChanged = { onAction(SiteVisitNoteContract.Action.ElevatorChanged(it)) },
        )

        DefectField(
            status = uiState.defectStatus,
            onChanged = { onAction(SiteVisitNoteContract.Action.DefectChanged(it)) },
        )

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            SectionHeader(
                title = stringResource(R.string.note_section_memo),
                trailingText = stringResource(R.string.note_memo_free),
            )
            OutlinedTextField(
                value = uiState.memo,
                onValueChange = { onAction(SiteVisitNoteContract.Action.MemoChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(R.string.note_memo_hint)) },
                minLines = MEMO_MIN_LINES,
                shape = fieldShape,
            )
        }

        FootnoteText(text = stringResource(R.string.note_footnote))
    }
}

@Composable
private fun VisitedOnField(visitedOn: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.card)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(AppSize.border, AppTheme.semanticColors.line), shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
            Text(
                text = stringResource(R.string.note_visited_on),
                style = AppTextStyles.monoCaption,
                color = AppTheme.semanticColors.ink45,
            )
            Text(
                text = visitedOn.formatYearMonthDay().orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = AppTheme.semanticColors.ink,
            )
        }
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = null,
            tint = AppTheme.semanticColors.ink45,
        )
    }
}

/** 기획서 `.rate` — 1~5 중 하나를 누른다. 같은 걸 다시 누르면 지운다. */
@Composable
private fun RatingRow(
    label: String,
    score: Int?,
    onScoreChanged: (Int?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.semanticColors.ink,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            (VisitRatings.MIN_SCORE..VisitRatings.MAX_SCORE).forEach { value ->
                val selected = value == score
                Box(
                    modifier = Modifier
                        .size(SCORE_BUTTON)
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                AppTheme.semanticColors.line2
                            },
                        )
                        .clickable { onScoreChanged(if (selected) null else value) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = value.toString(),
                        style = AppTextStyles.mono,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            AppTheme.semanticColors.ink70
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ElevatorField(
    condition: ElevatorCondition,
    onChanged: (ElevatorCondition) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = stringResource(R.string.note_elevator),
            style = AppTextStyles.monoCaption,
            color = AppTheme.semanticColors.ink45,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ElevatorCondition.entries.forEach { candidate ->
                FilterChip(
                    selected = candidate == condition,
                    onClick = { onChanged(candidate) },
                    label = { Text(text = stringResource(candidate.labelRes())) },
                )
            }
        }
    }
}

@Composable
private fun DefectField(status: DefectStatus, onChanged: (DefectStatus) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = stringResource(R.string.note_defect),
            style = AppTextStyles.monoCaption,
            color = AppTheme.semanticColors.ink45,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            DefectStatus.entries.forEach { candidate ->
                FilterChip(
                    selected = candidate == status,
                    onClick = { onChanged(candidate) },
                    label = { Text(text = stringResource(candidate.labelRes())) },
                )
            }
        }
    }
}

@Composable
private fun SaveBar(enabled: Boolean, onClick: () -> Unit) {
    Column {
        HorizontalDivider(thickness = AppSize.border, color = AppTheme.semanticColors.line)
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.md),
            shape = RoundedCornerShape(AppRadius.button),
        ) {
            Text(text = stringResource(R.string.note_save))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisitDatePickerDialog(
    initialYyyyMmDd: String,
    onPicked: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialYyyyMmDd.yyyyMmDdToUtcMidnightMillis(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onPicked(it.utcMidnightMillisToYyyyMmDd()) }
                        ?: onDismiss()
                },
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun DeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.note_delete_confirm_title)) },
        text = { Text(text = stringResource(R.string.note_delete_confirm_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.note_delete),
                    color = AppTheme.semanticColors.close,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

private val SCORE_BUTTON = 36.dp
private const val MEMO_MIN_LINES = 4

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun SiteVisitNoteScreenPreview() {
    AppTheme {
        SiteVisitNoteScreen(
            uiState = SiteVisitNoteContract.State(
                kaptCode = "A15105302",
                complexName = "관악푸르지오아파트",
                regionLabel = "서울특별시 관악구 봉천동",
                snapshot = ComplexSnapshot.EMPTY,
                isLoaded = true,
                isExisting = true,
                visitedOn = "20260720",
                viewedUnit = "84㎡ · 12층 · 남향",
                ratings = VisitRatings(
                    mapOf(
                        VisitCriterion.LIGHT to 4,
                        VisitCriterion.NOISE to 3,
                        VisitCriterion.PARKING to 2,
                        VisitCriterion.MANAGEMENT to 4,
                    ),
                ),
                walkMinutesText = "8",
                elevatorCondition = ElevatorCondition.COMFORTABLE,
                defectStatus = DefectStatus.NONE,
                memo = "남향 채광 좋음. 다만 8층 이하는 앞동에 가림.",
            ),
            onAction = {},
            onBackClick = {},
        )
    }
}
