package com.ams.youthhouse.feature.main.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.ams.youthhouse.R
import com.ams.youthhouse.feature.home.presentation.navigation.HomeDestination
import com.ams.youthhouse.feature.schedule.presentation.navigation.ScheduleDestination
import com.ams.youthhouse.feature.trade.presentation.navigation.TradeDestination
import com.ams.youthhouse.feature.notice.presentation.navigation.NoticeDestination
import com.ams.youthhouse.feature.settings.presentation.navigation.SettingsDestination

/**
 * 바텀 네비게이션 탭 정의.
 *
 * 각 탭은 다른 feature의 **navigation contract(Destination)** 만 참조한다.
 * Activity/ViewModel 등 구현체는 참조하지 않는다.
 *
 * 임장노트는 화면이 자리만 잡힌 상태라 탭에서 빼 두었다. 동작하지 않는 탭을 노출하면
 * 스토어 심사에서 미완성 기능으로 잡히고, 무엇보다 눌러서 빈 화면에 도착한다.
 * 기능이 생기면 `NoteDestination`으로 항목을 되살리면 된다.
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
    SCHEDULE(
        destination = ScheduleDestination,
        labelRes = R.string.main_tab_schedule,
        icon = Icons.Filled.DateRange,
    ),
    TRADE(
        destination = TradeDestination,
        labelRes = R.string.main_tab_trade,
        icon = Icons.Filled.ShoppingCart,
    ),
    MY(
        destination = SettingsDestination,
        labelRes = R.string.main_tab_my,
        icon = Icons.Filled.Person,
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
