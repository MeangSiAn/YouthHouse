package com.ams.myjeonse.feature.notice.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.ams.myjeonse.R
import com.ams.myjeonse.core.designsystem.theme.AppTheme
import com.ams.myjeonse.core.notice.presentation.model.previewNoticeUiModel

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
                title = { Text(text = stringResource(R.string.notice_detail_title)) },
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                notice.supplyTypeName?.let { supplyType ->
                    Text(
                        text = supplyType,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                notice.statusName?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = notice.title,
                style = MaterialTheme.typography.headlineSmall,
            )

            HorizontalDivider()

            DetailRow(R.string.notice_field_institution, notice.supplyInstitutionName)
            DetailRow(R.string.notice_field_house_type, notice.houseTypeName)
            DetailRow(R.string.notice_field_complex, notice.complexName)
            DetailRow(R.string.notice_field_region, notice.regionName)
            DetailRow(R.string.notice_field_address, notice.fullAddress)
            DetailRow(R.string.notice_field_heating, notice.heatingMethodName)

            HorizontalDivider()

            DetailRow(R.string.notice_field_notice_date, notice.noticeDate)
            DetailRow(R.string.notice_field_apply_period, notice.applyPeriod)
            DetailRow(R.string.notice_field_winner_date, notice.winnerAnnounceDate)

            HorizontalDivider()

            DetailRow(
                labelRes = R.string.notice_field_total_household,
                value = notice.totalHouseholdCount?.let {
                    stringResource(R.string.notice_unit_household, it)
                },
            )
            DetailRow(
                labelRes = R.string.notice_field_supply_count,
                value = notice.supplyCount?.let {
                    stringResource(R.string.notice_unit_household, it)
                },
            )

            HorizontalDivider()

            DetailRow(R.string.notice_field_deposit, notice.deposit?.toWon())
            DetailRow(R.string.notice_field_monthly_rent, notice.monthlyRent?.toWon())
            DetailRow(R.string.notice_field_down_payment, notice.downPayment?.toWon())
            DetailRow(R.string.notice_field_interim_payment, notice.interimPayment?.toWon())
            DetailRow(R.string.notice_field_balance, notice.balance?.toWon())

            HorizontalDivider()

            DetailRow(R.string.notice_field_contact, notice.contact)

            Button(
                onClick = { onAction(NoticeDetailContract.Action.OpenOriginalClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(text = stringResource(R.string.notice_detail_open_original))
            }
        }
    }
}

@Composable
private fun String.toWon(): String = stringResource(R.string.notice_unit_won, this)

@Composable
private fun DetailRow(
    @StringRes labelRes: Int,
    value: String?,
) {
    if (value.isNullOrBlank()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeDetailScreenPreview() {
    AppTheme {
        NoticeDetailScreen(
            uiState = NoticeDetailContract.State(
                notice = previewNoticeUiModel(),
            ),
            onAction = {},
            onBackClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
