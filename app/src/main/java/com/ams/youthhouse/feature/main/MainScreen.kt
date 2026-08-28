package com.ams.youthhouse.feature.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ams.youthhouse.feature.main.navigation.MainTab

@Composable
fun MainScreen(
    uiState: MainContract.State,
    selectedTab: MainTab?,
    onTabSelected: (MainTab) -> Unit,
    onAction: (MainContract.Action) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // selectedTab이 null이면 탭에 속하지 않는 목적지(예: 공고 상세)이므로 바텀바를 숨긴다.
            if (uiState.isBottomBarVisible && selectedTab != null) {
                MainBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                )
            }
        },
        content = content,
    )
}

@Composable
private fun MainBottomBar(
    selectedTab: MainTab?,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        MainTab.entries.forEach { tab ->
            val label = stringResource(tab.labelRes)

            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = label,
                    )
                },
                label = { Text(text = label) },
            )
        }
    }
}

