package com.ams.myjeonse.core.designsystem.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 전세알리미 기획서(`전세알리미_기획.html`)의 CSS 커스텀 프로퍼티를 그대로 옮긴 원시 토큰.
 *
 * 화면 코드는 이 값을 직접 쓰지 않는다.
 * Material3 슬롯([AppLightColorScheme])이나 시맨틱 토큰([AppSemanticColors])을 거쳐 쓴다.
 */
internal object Palette {
    val Cobalt = Color(0xFF1E3FA0)
    val CobaltTint = Color(0xFFEDF1FB)

    val Live = Color(0xFF0F9D63)
    val LiveTint = Color(0xFFE7F5EE)

    val Signal = Color(0xFFE39400)
    val SignalTint = Color(0xFFFCF3E3)
    val SignalDeep = Color(0xFF96650B)

    val Close = Color(0xFFC0392B)
    val CloseTint = Color(0xFFFBEDEB)

    /** 마감 임박 카드의 검은 배경 */
    val Board = Color(0xFF16191C)

    /** 검은 배경 위 D-day 숫자 */
    val Coral = Color(0xFFFF7A6E)

    val Ink = Color(0xFF15181B)
    val Ink70 = Color(0xFF4C555C)
    val Ink45 = Color(0xFF8B949B)
    val Ink25 = Color(0xFFC2C8CC)

    val Line = Color(0xFFE5E8E6)
    val Line2 = Color(0xFFF1F3F1)

    val White = Color(0xFFFFFFFF)
}

/**
 * Material3 기본 컴포넌트(Card, NavigationBar, OutlinedTextField 등)가 읽는 슬롯.
 *
 * 여기를 채우지 않으면 팔레트를 정의해도 기본 컴포넌트는 계속 보라색으로 나온다.
 */
internal val AppLightColorScheme = lightColorScheme(
    primary = Palette.Cobalt,
    onPrimary = Palette.White,
    primaryContainer = Palette.CobaltTint,
    onPrimaryContainer = Palette.Cobalt,

    secondary = Palette.Live,
    onSecondary = Palette.White,
    secondaryContainer = Palette.LiveTint,
    onSecondaryContainer = Palette.Live,

    tertiary = Palette.Signal,
    onTertiary = Palette.White,
    tertiaryContainer = Palette.SignalTint,
    onTertiaryContainer = Palette.SignalDeep,

    error = Palette.Close,
    onError = Palette.White,
    errorContainer = Palette.CloseTint,
    onErrorContainer = Palette.Close,

    background = Palette.White,
    onBackground = Palette.Ink,
    surface = Palette.White,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.Line2,
    onSurfaceVariant = Palette.Ink70,

    // 기획서 카드는 흰 배경 + 1dp 테두리다. Material3의 기본 tonal elevation이
    // 카드 배경을 회색조로 물들이지 않도록 컨테이너 단계를 흰색 쪽으로 고정한다.
    surfaceContainerLowest = Palette.White,
    surfaceContainerLow = Palette.White,
    surfaceContainer = Palette.White,
    surfaceContainerHigh = Palette.Line2,
    surfaceContainerHighest = Palette.Line2,

    outline = Palette.Line,
    outlineVariant = Palette.Line2,
)
