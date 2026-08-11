package com.ams.myjeonse.feature.settings.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ams.myjeonse.feature.settings.presentation.SettingsRoute
import kotlinx.serialization.Serializable

/** 설정 탭의 navigation contract. 다른 feature는 이 타입까지만 참조한다. */
@Serializable
data object SettingsDestination

fun NavGraphBuilder.settingsScreen() {
    composable<SettingsDestination> {
        SettingsRoute()
    }
}
