package com.ams.youthhouse.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Material3 슬롯에 대응이 없는 기획서 색.
 *
 * 예를 들어 `ink45`(보조 텍스트 3단계)나 `board`(마감 임박 카드 배경)는
 * Material3에 대응 슬롯이 없다. 억지로 `colorScheme`에 밀어 넣으면
 * 기본 컴포넌트가 엉뚱한 곳에 그 색을 쓰게 되므로 분리해서 제공한다.
 */
@Immutable
data class AppSemanticColors(
    val live: Color,
    val liveTint: Color,
    val signal: Color,
    val signalTint: Color,
    val signalDeep: Color,
    val close: Color,
    val closeTint: Color,
    /** 마감 임박 카드 배경(검정)과 그 위 텍스트 */
    val board: Color,
    val onBoard: Color,
    val onBoardMuted: Color,
    /** 검은 카드 위 D-day 숫자 */
    val dday: Color,
    val ink: Color,
    val ink70: Color,
    val ink45: Color,
    val ink25: Color,
    val line: Color,
    val line2: Color,
)

internal val LightSemanticColors = AppSemanticColors(
    live = Palette.Live,
    liveTint = Palette.LiveTint,
    signal = Palette.Signal,
    signalTint = Palette.SignalTint,
    signalDeep = Palette.SignalDeep,
    close = Palette.Close,
    closeTint = Palette.CloseTint,
    board = Palette.Board,
    onBoard = Palette.White,
    onBoardMuted = Palette.Ink45,
    dday = Palette.Coral,
    ink = Palette.Ink,
    ink70 = Palette.Ink70,
    ink45 = Palette.Ink45,
    ink25 = Palette.Ink25,
    line = Palette.Line,
    line2 = Palette.Line2,
)

/**
 * 값이 런타임에 바뀌지 않으므로 [staticCompositionLocalOf]를 쓴다.
 * 재구성 추적 비용이 없고, Preview에서 테마를 갈아끼울 여지는 남는다.
 */
val LocalAppSemanticColors = staticCompositionLocalOf { LightSemanticColors }
