package com.ams.youthhouse.feature.settings.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ams.youthhouse.core.ui.extension.CollectUiEffect

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    // 시스템 파일 선택기. 저장소 권한 없이 사용자가 고른 파일 하나에만 접근한다.
    val createBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE),
    ) { uri -> viewModel.onAction(SettingsContract.Action.BackupTargetPicked(uri?.toString())) }
    val openBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> viewModel.onAction(SettingsContract.Action.BackupSourcePicked(uri?.toString())) }

    CollectUiEffect(viewModel.uiEffect) { effect ->
        when (effect) {
            is SettingsContract.Effect.OpenUrl -> uriHandler.openUri(effect.url)
            is SettingsContract.Effect.PickBackupTarget -> createBackup.launch(effect.suggestedFileName)
            // 메신저·드라이브가 .json을 application/octet-stream으로 주는 일이 흔해 전부 허용한다.
            SettingsContract.Effect.PickBackupSource -> openBackup.launch(arrayOf("*/*"))
        }
    }

    SettingsScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

private const val BACKUP_MIME_TYPE = "application/json"
