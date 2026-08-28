package com.ams.youthhouse.core.ui.base

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.ams.youthhouse.core.designsystem.theme.AppTheme

abstract class BaseActivity : ComponentActivity() {

    // Hilt(@AndroidEntryPoint)가 SavedStateHandle 주입을 위해 onCreate를 오버라이드하므로
    // final로 막을 수 없다. 대신 하위 Activity는 아래 훅(onCreateBeforeContent /
    // ActivityContent / onCreateAfterContent)만 재정의하도록 한다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureWindow()
        onCreateBeforeContent(savedInstanceState)

        setContent {
            AppTheme {
                ActivityContent()
            }
        }

        onCreateAfterContent(savedInstanceState)
    }

    protected open fun configureWindow() {
        // 앱 테마가 라이트 고정이므로 시스템 다크 모드에서도 시스템 바를 라이트로 강제한다.
        // 기본값을 쓰면 다크 모드에서 흰 배경 위에 흰 아이콘이 그려져 보이지 않는다.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
    }

    protected open fun onCreateBeforeContent(
        savedInstanceState: Bundle?,
    ) = Unit

    @Composable
    protected abstract fun ActivityContent()

    protected open fun onCreateAfterContent(
        savedInstanceState: Bundle?,
    ) = Unit
}
