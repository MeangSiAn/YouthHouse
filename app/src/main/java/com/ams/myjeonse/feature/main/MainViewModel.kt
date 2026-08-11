package com.ams.myjeonse.feature.main

import com.ams.myjeonse.core.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() :
    BaseViewModel<
        MainContract.State,
        MainContract.Action,
        MainContract.Effect,
        >(
        initialState = MainContract.State(),
    ) {

    override fun handleAction(action: MainContract.Action) {
        when (action) {
            is MainContract.Action.BottomBarVisibilityChanged -> {
                updateState { copy(isBottomBarVisible = action.isVisible) }
            }
        }
    }
}
