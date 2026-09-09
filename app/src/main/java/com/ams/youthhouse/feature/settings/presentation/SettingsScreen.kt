package com.ams.youthhouse.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.backup.domain.model.BackupSummary
import com.ams.youthhouse.core.designsystem.component.FootnoteText
import com.ams.youthhouse.core.designsystem.component.SectionHeader
import com.ams.youthhouse.core.designsystem.component.SettingsRow
import com.ams.youthhouse.core.designsystem.component.SettingsRowGroup
import com.ams.youthhouse.core.designsystem.theme.AppSpacing
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.presentation.model.labelRes
import com.ams.youthhouse.feature.settings.presentation.component.PrivacyBanner
import com.ams.youthhouse.feature.settings.presentation.component.SettingsChoiceDialog

@Composable
fun SettingsScreen(
    uiState: SettingsContract.State,
    onAction: (SettingsContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val backupMessageText = uiState.backupMessage?.let { backupMessageText(it) }
    LaunchedEffect(uiState.backupMessage) {
        if (backupMessageText != null) {
            snackbarHostState.showSnackbar(backupMessageText)
            onAction(SettingsContract.Action.BackupMessageShown)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            item {
                Text(
                    text = stringResource(R.string.main_tab_my),
                    style = MaterialTheme.typography.titleLarge,
                    color = AppTheme.semanticColors.ink,
                )
            }
            item { PrivacyBanner() }

            item { SectionHeader(title = stringResource(R.string.settings_section_display)) }
            item {
                SettingsRowGroup {
                    SettingsRow(
                        title = stringResource(R.string.settings_region),
                        description = stringResource(R.string.settings_region_description),
                        value = uiState.selectedRegion?.regionName
                            ?: stringResource(R.string.notice_region_all),
                        onClick = { onAction(SettingsContract.Action.RegionRowClicked) },
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_category),
                        description = stringResource(R.string.settings_category_description),
                        value = stringResource(uiState.selectedCategory.labelRes),
                        onClick = { onAction(SettingsContract.Action.CategoryRowClicked) },
                        showDivider = false,
                    )
                }
            }

            // 임장노트는 서버에 없다. 폰을 바꾸면 이 파일만이 노트를 옮기는 길이다.
            item {
                SectionHeader(
                    title = stringResource(R.string.settings_section_data),
                    trailingText = stringResource(R.string.settings_section_data_scope),
                )
            }
            item {
                SettingsRowGroup {
                    SettingsRow(
                        title = stringResource(R.string.settings_backup_export),
                        description = stringResource(R.string.settings_backup_export_description),
                        value = if (uiState.isBackupBusy) stringResource(R.string.settings_backup_busy) else null,
                        onClick = { onAction(SettingsContract.Action.ExportBackupClicked) },
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_backup_import),
                        description = stringResource(R.string.settings_backup_import_description),
                        onClick = { onAction(SettingsContract.Action.ImportBackupClicked) },
                        showDivider = false,
                    )
                }
            }

            item { SectionHeader(title = stringResource(R.string.settings_section_info)) }
            item {
                SettingsRowGroup {
                    // 심사자와 사용자가 가장 먼저 찾는 항목이라 정보 섹션 맨 위에 둔다.
                    SettingsRow(
                        title = stringResource(R.string.settings_privacy_policy),
                        value = stringResource(R.string.settings_privacy_policy_value),
                        onClick = { onAction(SettingsContract.Action.PrivacyPolicyRowClicked) },
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_data_source),
                        value = stringResource(R.string.settings_data_source_value),
                        showDivider = true,
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_version),
                        value = uiState.versionName,
                        showDivider = false,
                    )
                }
            }

            item { FootnoteText(text = stringResource(R.string.settings_footnote)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    when (uiState.openDialog) {
        SettingsDialog.NONE -> Unit

        SettingsDialog.REGION -> SettingsChoiceDialog(
            title = stringResource(R.string.settings_region),
            // 첫 항목이 "전체"라 null을 함께 담는다.
            options = listOf<NoticeRegion?>(null) + NoticeRegion.entries,
            selectedOption = uiState.selectedRegion,
            optionLabel = { region ->
                region?.regionName ?: stringResource(R.string.notice_region_all)
            },
            onOptionSelected = { onAction(SettingsContract.Action.RegionSelected(it)) },
            onDismiss = { onAction(SettingsContract.Action.DialogDismissed) },
        )

        SettingsDialog.CATEGORY -> SettingsChoiceDialog(
            title = stringResource(R.string.settings_category),
            options = NoticeCategory.entries,
            selectedOption = uiState.selectedCategory,
            optionLabel = { stringResource(it.labelRes) },
            onOptionSelected = { onAction(SettingsContract.Action.CategorySelected(it)) },
            onDismiss = { onAction(SettingsContract.Action.DialogDismissed) },
        )
    }
}

@Composable
private fun backupMessageText(message: BackupMessage): String = when (message) {
    is BackupMessage.Exported ->
        stringResource(R.string.backup_exported, message.summary.summaryText())
    is BackupMessage.Imported ->
        stringResource(R.string.backup_imported, message.summary.summaryText())
    BackupMessage.InvalidFile -> stringResource(R.string.backup_invalid_file)
    BackupMessage.Failed -> stringResource(R.string.backup_failed)
}

@Composable
private fun BackupSummary.summaryText(): String = stringResource(
    R.string.backup_summary,
    noteCount,
    favoriteNoticeCount,
    favoriteComplexCount,
)

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            uiState = SettingsContract.State(
                isLoading = false,
                selectedRegion = NoticeRegion.SEOUL,
                selectedCategory = NoticeCategory.RENTAL,
                versionName = "1.0",
            ),
            onAction = {},
        )
    }
}
