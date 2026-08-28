package com.ams.youthhouse.feature.note.presentation

import com.ams.youthhouse.core.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor() :
    BaseViewModel<
        NoteContract.State,
        NoteContract.Action,
        NoteContract.Effect,
        >(
        initialState = NoteContract.State(),
    ) {

    override fun handleAction(action: NoteContract.Action) {
        when (action) {
            NoteContract.Action.Refreshed -> Unit
        }
    }
}
