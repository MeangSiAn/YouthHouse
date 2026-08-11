package com.ams.myjeonse.feature.favorite.presentation

import com.ams.myjeonse.core.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor() :
    BaseViewModel<
        FavoriteContract.State,
        FavoriteContract.Action,
        FavoriteContract.Effect,
        >(
        initialState = FavoriteContract.State(),
    ) {

    override fun handleAction(action: FavoriteContract.Action) {
        when (action) {
            FavoriteContract.Action.Refreshed -> Unit
        }
    }
}
