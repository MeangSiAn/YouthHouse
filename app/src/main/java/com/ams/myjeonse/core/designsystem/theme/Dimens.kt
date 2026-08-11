package com.ams.myjeonse.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * 간격 토큰. 테마에 따라 바뀌지 않으므로 CompositionLocal 없이 top-level object로 둔다.
 */
object AppSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 6.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 20.dp
    val xxxl = 28.dp
}

object AppRadius {
    val card = 11.dp
    val panel = 13.dp
    val badge = 4.dp
    val button = 9.dp
}

object AppSize {
    val border = 1.dp
    val borderStrong = 1.5.dp
    val statusDot = 5.dp
}
