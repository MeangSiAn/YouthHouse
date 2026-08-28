package com.ams.youthhouse.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ams.youthhouse.feature.main.navigation.MainNavHost
import com.ams.youthhouse.feature.main.navigation.navigateToTab
import com.ams.youthhouse.feature.main.navigation.toMainTab

@Composable
fun MainRoute(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = backStackEntry?.destination.toMainTab()

    MainScreen(
        uiState = uiState,
        selectedTab = selectedTab,
        onTabSelected = navController::navigateToTab,
        onAction = viewModel::onAction,
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
