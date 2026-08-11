package com.ams.myjeonse.feature.favorite.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ams.myjeonse.feature.favorite.presentation.FavoriteRoute
import kotlinx.serialization.Serializable

/** 찜 탭의 navigation contract. 다른 feature는 이 타입까지만 참조한다. */
@Serializable
data object FavoriteDestination

fun NavGraphBuilder.favoriteScreen() {
    composable<FavoriteDestination> {
        FavoriteRoute()
    }
}
