package com.ams.myjeonse.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * 앱 테마.
 *
 * ## dynamicColor를 쓰지 않는다
 * 이전에는 `dynamicColor = true`가 기본이라 Android 12+에서 기기 배경화면 색이 우선 적용됐다.
 * 그러면 기획서에서 정한 코발트 팔레트가 실기기에서 보이지 않는다.
 * 이 앱은 색으로 상태(접수중/예정/마감 임박)를 구분하므로 팔레트를 고정한다.
 *
 * ## 다크 테마
 * 기획서에 다크 팔레트가 없다. 유추한 다크는 검증 근거가 없어 대비가 깨질 위험이 커서
 * 지금은 라이트로 고정한다. 정식 다크 팔레트가 정해지면 여기서 분기한다.
 */
@Composable
fun AppTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppSemanticColors provides LightSemanticColors) {
        MaterialTheme(
            colorScheme = AppLightColorScheme,
            typography = Typography,
            content = content,
        )
    }
}

/** `MaterialTheme.colorScheme`과 나란히 쓰는 시맨틱 색 접근자. */
object AppTheme {

    val semanticColors: AppSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppSemanticColors.current
}
