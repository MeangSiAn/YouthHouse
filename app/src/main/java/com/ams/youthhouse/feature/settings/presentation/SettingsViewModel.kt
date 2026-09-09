package com.ams.youthhouse.feature.settings.presentation

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ams.youthhouse.core.backup.domain.repository.BackupFormatException
import com.ams.youthhouse.core.backup.domain.repository.BackupRepository
import com.ams.youthhouse.core.common.AppInfo
import com.ams.youthhouse.core.common.time.TodayProvider
import com.ams.youthhouse.core.notice.domain.repository.NoticeFilterRepository
import com.ams.youthhouse.core.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val noticeFilterRepository: NoticeFilterRepository,
    private val backupRepository: BackupRepository,
    private val todayProvider: TodayProvider,
    appInfo: AppInfo,
) : BaseViewModel<
    SettingsContract.State,
    SettingsContract.Action,
    SettingsContract.Effect,
    >(
    initialState = SettingsContract.State(),
) {

    init {
        updateState { copy(versionName = appInfo.versionName) }

        // 홈·임대공고 탭과 같은 저장소를 본다. 여기서 바꾸면 그쪽도 함께 바뀐다.
        combine(
            noticeFilterRepository.selectedRegion,
            noticeFilterRepository.selectedCategory,
        ) { region, category -> region to category }
            .distinctUntilChanged()
            .onEach { (region, category) ->
                updateState {
                    copy(isLoading = false, selectedRegion = region, selectedCategory = category)
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SettingsContract.Action) {
        when (action) {
            SettingsContract.Action.RegionRowClicked -> {
                updateState { copy(openDialog = SettingsDialog.REGION) }
            }

            SettingsContract.Action.CategoryRowClicked -> {
                updateState { copy(openDialog = SettingsDialog.CATEGORY) }
            }

            SettingsContract.Action.DialogDismissed -> {
                updateState { copy(openDialog = SettingsDialog.NONE) }
            }

            // 상태를 직접 바꾸지 않는다. 저장소에 쓰면 Flow가 되돌려준다(단방향).
            is SettingsContract.Action.RegionSelected -> {
                updateState { copy(openDialog = SettingsDialog.NONE) }
                viewModelScope.launch {
                    noticeFilterRepository.setSelectedRegion(action.region)
                }
            }

            is SettingsContract.Action.CategorySelected -> {
                updateState { copy(openDialog = SettingsDialog.NONE) }
                viewModelScope.launch {
                    noticeFilterRepository.setSelectedCategory(action.category)
                }
            }

            SettingsContract.Action.PrivacyPolicyRowClicked -> {
                sendEffect(SettingsContract.Effect.OpenUrl(PRIVACY_POLICY_URL))
            }

            SettingsContract.Action.ExportBackupClicked -> {
                if (currentState.isBackupBusy) return
                sendEffect(
                    SettingsContract.Effect.PickBackupTarget(
                        suggestedFileName = "$BACKUP_FILE_PREFIX${todayProvider.today()}.json",
                    ),
                )
            }

            SettingsContract.Action.ImportBackupClicked -> {
                if (currentState.isBackupBusy) return
                sendEffect(SettingsContract.Effect.PickBackupSource)
            }

            is SettingsContract.Action.BackupTargetPicked -> {
                val uri = action.uri ?: return
                runBackup { BackupMessage.Exported(backupRepository.export(uri)) }
            }

            is SettingsContract.Action.BackupSourcePicked -> {
                val uri = action.uri ?: return
                runBackup { BackupMessage.Imported(backupRepository.import(uri)) }
            }

            SettingsContract.Action.BackupMessageShown -> {
                updateState { copy(backupMessage = null) }
            }
        }
    }

    private fun runBackup(block: suspend () -> BackupMessage) {
        updateState { copy(isBackupBusy = true) }
        viewModelScope.launch {
            val message = try {
                block()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: BackupFormatException) {
                Log.w(TAG, "백업 파일 형식이 맞지 않습니다", exception)
                BackupMessage.InvalidFile
            } catch (throwable: Throwable) {
                Log.w(TAG, "백업 처리에 실패했습니다", throwable)
                BackupMessage.Failed
            }
            updateState { copy(isBackupBusy = false, backupMessage = message) }
        }
    }

    private companion object {
        const val TAG = "SettingsViewModel"
        const val BACKUP_FILE_PREFIX = "youthhouse-backup-"

        /**
         * 개인정보처리방침 랜딩. Play Console 앱 콘텐츠에 등록하는 주소와 **같은 값이어야 한다.**
         * 둘이 어긋나면 심사에서 앱 안의 링크와 스토어 표기가 다르다고 잡힌다.
         */
        const val PRIVACY_POLICY_URL = "https://myhome.mosstis.com/"
    }
}
