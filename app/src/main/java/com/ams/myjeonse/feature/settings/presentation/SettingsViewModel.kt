package com.ams.myjeonse.feature.settings.presentation

import com.ams.myjeonse.core.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() :
    BaseViewModel<
        SettingsContract.State,
        SettingsContract.Action,
        SettingsContract.Effect,
        >(
        initialState = SettingsContract.State(),
    ) {

    override fun handleAction(action: SettingsContract.Action) {
        when (action) {
            SettingsContract.Action.Refreshed -> Unit
        }
    }
}
