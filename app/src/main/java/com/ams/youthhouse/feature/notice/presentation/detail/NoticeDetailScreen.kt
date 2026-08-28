package com.ams.youthhouse.feature.notice.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.component.StatusLabel
import com.ams.youthhouse.core.designsystem.component.StatusTone
import com.ams.youthhouse.core.designsystem.component.SummaryEntry
import com.ams.youthhouse.core.designsystem.component.SummaryGrid
import com.ams.youthhouse.core.designsystem.component.Timeline
import com.ams.youthhouse.core.designsystem.component.TimelineItem
import com.ams.youthhouse.core.designsystem.component.TimelineState
import com.ams.youthhouse.core.designsystem.theme.AppRadius
import com.ams.youthhouse.core.designsystem.theme.AppSize
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.notice.presentation.model.NoticeScheduleStage
import com.ams.youthhouse.core.notice.presentation.model.NoticeStatus
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.notice.presentation.model.ScheduleStageKind
import com.ams.youthhouse.core.notice.presentation.model.ScheduleStageState
import com.ams.youthhouse.core.notice.presentation.model.labelRes
import com.ams.youthhouse.core.notice.presentation.model.previewNoticeUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeDetailScreen(
    uiState: NoticeDetailContract.State,
    onAction: (NoticeDetailContract.Action) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val notice = uiState.notice

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = stringResource(R.string.notice_detail_title))
                        // 기획서: 앱바 부제로 어느 기관·유형인지 먼저 알린다.
                        notice.institutionAndType()?.let { subtitle ->
                            Text(
                                text = subtitle,
                                style = AppTextStyles.monoCaption,
                                color = AppTheme.semanticColors.ink45,
                            )
                        }
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
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            DetailHeader(notice)

            SummaryGrid(entries = notice.toSummaryEntries())

            if (notice.scheduleStages.isNotEmpty()) {
                SectionHeader(
                    title = stringResource(R.string.notice_detail_schedule),
                    trailingText = stringResource(
                        R.string.notice_detail_schedule_count,
                        notice.scheduleStages.size,
                    ),
                )
                Timeline(items = notice.scheduleStages.map { it.toTimelineItem() })
                // 기획서는 서류 제출·계약 체결까지 4단계지만 이 API가 주지 않는다.
                // 모르는 것을 아는 척하지 않고, 어디서 확인하는지 알린다.
                FootnoteText(text = stringResource(R.string.notice_detail_schedule_footnote))
            }

            SectionHeader(title = stringResource(R.string.notice_detail_section_info))
            DetailRow(R.string.notice_field_complex, notice.complexName)
            DetailRow(R.string.notice_field_region, notice.regionName)
            AddressRow(
                address = notice.fullAddress,
                onMapClick = { onAction(NoticeDetailContract.Action.MapClicked) },
            )
            DetailRow(R.string.notice_field_house_type, notice.houseTypeName)
            DetailRow(R.string.notice_field_heating, notice.heatingMethodName)
            DetailRow(R.string.notice_field_contact, notice.contact)

            notice.toPaymentEntries().takeIf { it.isNotEmpty() }?.let { payments ->
                HorizontalDivider(color = AppTheme.semanticColors.line2)
                SectionHeader(title = stringResource(R.string.notice_detail_section_payment))
                payments.forEach { (labelRes, value) -> DetailRow(labelRes, value.won()) }
            }

            FootnoteText(text = stringResource(R.string.notice_detail_footnote))

            DetailActions(notice = notice, onAction = onAction)
        }
    }
}

@Composable
private fun DetailHeader(notice: NoticeUiModel) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = notice.statusName.orEmpty(),
                style = AppTextStyles.monoCaption,
                color = AppTheme.semanticColors.ink45,
            )
            notice.statusLabel?.let { label ->
                StatusLabel(text = label, tone = notice.status.toTone())
            }
        }
        Text(
            text = notice.title,
            style = MaterialTheme.typography.headlineSmall,
            color = AppTheme.semanticColors.ink,
        )
    }
}

/**
 * 외부로 나가는 두 경로.
 *
 * 화면 하단에 고정하지 않고 내용 끝에 둔다. 공고를 다 읽은 뒤에 누르는 버튼이고,
 * 고정해 두면 좁은 화면에서 본문 높이를 상시로 잡아먹는다.
 */
@Composable
private fun DetailActions(
    notice: NoticeUiModel,
    onAction: (NoticeDetailContract.Action) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Button(
            onClick = { onAction(NoticeDetailContract.Action.ApplyClicked) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.button),
        ) {
            Text(
                text = notice.supplyInstitutionName
                    ?.let { stringResource(R.string.notice_detail_apply_at, it) }
                    ?: stringResource(R.string.notice_detail_apply),
            )
        }
        OutlinedButton(
            onClick = { onAction(NoticeDetailContract.Action.OpenOriginalClicked) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.button),
        ) {
            Text(text = stringResource(R.string.notice_detail_open_original))
        }
    }
}

/**
 * 주소 행 — 값 아래에 "지도에서 보기" 링크가 붙는다.
 *
 * 임대 공고의 절반은 권역 단위 모집이라 주소가 없다(찍을 건물이 없는 공고다).
 * 그때는 [DetailRow]와 같은 규칙으로 행 자체를 그리지 않으므로 링크도 함께 사라진다.
 */
@Composable
private fun AddressRow(
    address: String?,
    onMapClick: () -> Unit,
) {
    if (address.isNullOrBlank()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = stringResource(R.string.notice_field_address),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.semanticColors.ink45,
            modifier = Modifier.weight(1f),
        )
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.semanticColors.ink,
            )
            Text(
                text = stringResource(R.string.notice_detail_open_map),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onMapClick)
                    // 텍스트 링크의 터치 영역 확보. 시각적 간격은 위쪽만 살짝 준다.
                    .padding(top = AppSpacing.sm, bottom = AppSpacing.xs, end = AppSpacing.xl),
            )
        }
    }
}

@Composable
private fun DetailRow(
    @StringRes labelRes: Int,
    value: String?,
) {
    if (value.isNullOrBlank()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.semanticColors.ink45,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.semanticColors.ink,
            modifier = Modifier.weight(2f),
        )
    }
}

/** 앱바 부제 — "LH · 매입임대". 분양은 공급유형이 없어 분야 라벨로 대신한다. */
@Composable
private fun NoticeUiModel.institutionAndType(): String? {
    val type = supplyTypeName ?: stringResource(category.labelRes)
    return listOfNotNull(supplyInstitutionName, type)
        .joinToString(separator = " · ")
        .takeIf { it.isNotBlank() }
}

/** 기획서 `.st4` — 돈과 규모처럼 먼저 보는 수치만. 없는 값은 칸을 만들지 않는다. */
@Composable
private fun NoticeUiModel.toSummaryEntries(): List<SummaryEntry> = listOfNotNull(
    deposit?.let { SummaryEntry(stringResource(R.string.notice_summary_deposit), it.won()) },
    monthlyRent?.let {
        SummaryEntry(stringResource(R.string.notice_summary_monthly_rent), it.won())
    },
    balance?.let { SummaryEntry(stringResource(R.string.notice_summary_balance), it.won()) },
    supplyCount?.let {
        SummaryEntry(stringResource(R.string.notice_summary_supply), it.households())
    },
    totalHouseholdCount?.let {
        SummaryEntry(stringResource(R.string.notice_summary_total), it.households())
    },
).take(MAX_SUMMARY_ENTRIES)

/** 요약 격자에 넣지 않은 나머지 금액. */
private fun NoticeUiModel.toPaymentEntries(): List<Pair<Int, String>> = listOfNotNull(
    downPayment?.let { R.string.notice_field_down_payment to it },
    interimPayment?.let { R.string.notice_field_interim_payment to it },
)

@Composable
private fun String.won(): String = stringResource(R.string.notice_unit_won, this)

@Composable
private fun String.households(): String = stringResource(R.string.notice_unit_household, this)

private fun NoticeStatus.toTone(): StatusTone = when (this) {
    NoticeStatus.OPEN -> StatusTone.LIVE
    NoticeStatus.URGENT -> StatusTone.URGENT
    NoticeStatus.UPCOMING -> StatusTone.SOON
    NoticeStatus.CLOSED, NoticeStatus.UNKNOWN -> StatusTone.CLOSED
}

@Composable
private fun NoticeScheduleStage.toTimelineItem(): TimelineItem = TimelineItem(
    date = dateText,
    title = stringResource(
        when (kind) {
            ScheduleStageKind.ANNOUNCED -> R.string.notice_schedule_announced
            ScheduleStageKind.APPLY -> R.string.notice_schedule_apply
            ScheduleStageKind.RESULT -> R.string.notice_schedule_result
        },
    ),
    state = when (state) {
        ScheduleStageState.DONE -> TimelineState.DONE
        ScheduleStageState.CURRENT -> TimelineState.CURRENT
        ScheduleStageState.UPCOMING -> TimelineState.UPCOMING
    },
)

private const val MAX_SUMMARY_ENTRIES = 4

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun NoticeDetailScreenPreview() {
    AppTheme {
        NoticeDetailScreen(
            uiState = NoticeDetailContract.State(notice = previewNoticeUiModel()),
            onAction = {},
            onBackClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
