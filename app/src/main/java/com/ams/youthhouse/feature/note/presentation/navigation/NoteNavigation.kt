package com.ams.youthhouse.feature.note.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ams.youthhouse.feature.note.presentation.NoteRoute
import kotlinx.serialization.Serializable

/** 찜 탭의 navigation contract. 다른 feature는 이 타입까지만 참조한다. */
@Serializable
data object NoteDestination

fun NavGraphBuilder.noteScreen() {
    composable<NoteDestination> {
        NoteRoute()
    }
}
