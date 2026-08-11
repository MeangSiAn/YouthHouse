package com.ams.myjeonse.feature.main

import androidx.compose.runtime.Composable
import com.ams.myjeonse.core.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Composable
    override fun ActivityContent() {
        MainRoute()
    }
}
