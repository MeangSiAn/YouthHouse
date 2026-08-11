package com.ams.myjeonse.feature.main.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.ams.myjeonse.R
import com.ams.myjeonse.feature.favorite.presentation.navigation.FavoriteDestination
import com.ams.myjeonse.feature.home.presentation.navigation.HomeDestination
import com.ams.myjeonse.feature.notice.presentation.navigation.NoticeDestination
import com.ams.myjeonse.feature.settings.presentation.navigation.SettingsDestination

/**
 * 바텀 네비게이션 탭 정의.
 *
 * 각 탭은 다른 feature의 **navigation contract(Destination)** 만 참조한다.
 * Activity/ViewModel 등 구현체는 참조하지 않는다.
 */
enum class MainTab(
    val destination: Any,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME(
        destination = HomeDestination,
        labelRes = R.string.main_tab_home,
        icon = Icons.Filled.Home,
    ),
    NOTICE(
        destination = NoticeDestination,
        labelRes = R.string.main_tab_notice,
        icon = Icons.AutoMirrored.Filled.List,
    ),
    FAVORITE(
        destination = FavoriteDestination,
        labelRes = R.string.main_tab_favorite,
        icon = Icons.Filled.Favorite,
    ),
    SETTINGS(
        destination = SettingsDestination,
        labelRes = R.string.main_tab_settings,
        icon = Icons.Filled.Settings,
    ),
}

/** 현재 백스택 목적지가 어느 탭에 속하는지 역으로 찾는다. */
fun NavDestination?.toMainTab(): MainTab? = this
    ?.hierarchy
    ?.firstNotNullOfOrNull { destination ->
        MainTab.entries.firstOrNull { tab ->
            destination.hasRoute(tab.destination::class)
        }
    }

/** 탭 전환: 시작 목적지까지 pop하고 각 탭의 상태는 저장/복원한다. */
fun NavHostController.navigateToTab(tab: MainTab) {
    navigate(tab.destination) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
