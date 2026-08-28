package com.ams.youthhouse.feature.main.intro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.component.BrandMark
import com.ams.youthhouse.core.designsystem.theme.AppTextStyles
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import kotlinx.coroutines.delay

/**
 * 기획서 `alimi-splash.html`의 브랜드 인트로.
 *
 * 홈 위에 덮어 두고 [onFinished]가 호출되면 걷힌다. 아래에서 홈이 먼저 데이터를 받기 시작하므로
 * 인트로가 끝날 때쯤에는 대부분 목록이 준비돼 있다 — 이 2초는 로딩을 기다리는 시간이 아니라
 * 로딩과 겹쳐 쓰는 시간이다.
 *
 * 기획서의 DURATION POLICY는 "최소 1.2초 + 데이터 준비 시 즉시 전환"을 권한다.
 * 다만 준비 신호가 feature/home 안에 있어 여기까지 끌어오면 feature 간 직접 의존이 생기므로,
 * 애니메이션이 실제로 다 채우는 2.02초 고정으로 둔다. 빈 대기 구간이 없어 손해가 크지 않다.
 */
@Composable
fun IntroScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 목표 상태로 바뀌어야 전이가 재생되므로 첫 컴포지션 직후에 켠다.
    var isStarted by remember { mutableStateOf(false) }
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        isStarted = true
        delay(IntroTiming.TOTAL.toLong())
        currentOnFinished()
    }

    ForceLightSystemBars()

    val transition = updateTransition(targetState = isStarted, label = "intro")

    // 마지막 0.28초는 화면 전체가 살짝 확대되며 사라지는 구간이다.
    val rootAlpha by transition.animateOnce(from = 1f, to = 0f, label = "rootAlpha") {
        keyframes {
            durationMillis = IntroTiming.TOTAL
            1f at IntroTiming.EXIT_DELAY
        }
    }
    val rootScale by transition.animateOnce(from = 1f, to = EXIT_SCALE, label = "rootScale") {
        keyframes {
            durationMillis = IntroTiming.TOTAL
            1f at IntroTiming.EXIT_DELAY
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(rootAlpha)
            .scale(rootScale)
            .background(MaterialTheme.colorScheme.primary)
            .blockTouches(),
    ) {
        TopHighlight()
        BrandBlock(transition)
        LoadingBar(transition, modifier = Modifier.align(Alignment.BottomCenter))
        Footer(transition, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 기획서 `.splash::after` — 위쪽에서 은은하게 번지는 빛. */
@Composable
private fun BoxScope.TopHighlight() {
    Box(
        modifier = Modifier
            .matchParentSize()
            .drawWithCache {
                val brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width / 2f, size.height * 0.18f),
                    radius = size.width * 1.2f,
                )
                onDrawBehind { drawRect(brush) }
            },
    )
}

@Composable
private fun BoxScope.BrandBlock(transition: Transition<Boolean>) {
    val markAlpha by transition.animateOnce(label = "markAlpha") {
        tween(IntroTiming.MARK_DURATION, IntroTiming.MARK_DELAY, EmphasizedEasing)
    }
    val markScale by transition.animateOnce(from = 0.82f, to = 1f, label = "markScale") {
        tween(IntroTiming.MARK_DURATION, IntroTiming.MARK_DELAY, EmphasizedEasing)
    }
    val boardAlpha by transition.animateOnce(label = "boardAlpha") {
        tween(IntroTiming.BOARD_DURATION, IntroTiming.BOARD_DELAY, StandardEasing)
    }
    val line1Alpha by transition.animateOnce(label = "line1Alpha") {
        tween(IntroTiming.LINE_DURATION, IntroTiming.LINE1_DELAY, StandardEasing)
    }
    val line2Alpha by transition.animateOnce(label = "line2Alpha") {
        tween(IntroTiming.LINE_DURATION, IntroTiming.LINE2_DELAY, StandardEasing)
    }
    val line3Alpha by transition.animateOnce(label = "line3Alpha") {
        tween(IntroTiming.LINE_DURATION, IntroTiming.LINE3_DELAY, StandardEasing)
    }
    val dotAlpha by transition.animateOnce(label = "dotAlpha") {
        tween(IntroTiming.DOT_PEAK, IntroTiming.DOT_DELAY, StandardEasing)
    }
    // 알림 점만 살짝 튀어 오른다. 브랜드의 핵심 동작이라 마지막에 강조한다.
    val dotScale by transition.animateOnce(from = 0.2f, to = 1f, label = "dotScale") {
        keyframes {
            durationMillis = IntroTiming.DOT_DURATION
            delayMillis = IntroTiming.DOT_DELAY
            DOT_OVERSHOOT at IntroTiming.DOT_PEAK
        }
    }

    val wordProgress by transition.animateOnce(label = "wordProgress") {
        tween(IntroTiming.TEXT_DURATION, IntroTiming.WORD_DELAY, EmphasizedEasing)
    }
    val tagProgress by transition.animateOnce(label = "tagProgress") {
        tween(IntroTiming.TEXT_DURATION, IntroTiming.TAG_DELAY, EmphasizedEasing)
    }

    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandMark(
            modifier = Modifier
                .alpha(markAlpha)
                .scale(markScale),
            boardAlpha = boardAlpha,
            line1Alpha = line1Alpha,
            line2Alpha = line2Alpha,
            line3Alpha = line3Alpha,
            dotAlpha = dotAlpha,
            dotScale = dotScale,
        )

        Text(
            text = stringResource(R.string.app_name),
            style = AppTextStyles.wordmark,
            color = Color.White,
            modifier = Modifier
                .padding(top = 20.dp)
                .riseIn(wordProgress),
        )

        Text(
            text = stringResource(R.string.intro_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 7.dp)
                .riseIn(tagProgress),
        )
    }
}

/**
 * 진행률 숫자는 쓰지 않는다 — 실제 진행과 어긋나면 신뢰를 잃는다.
 * 얇은 바로 "받아오는 중"만 전한다.
 */
@Composable
private fun LoadingBar(
    transition: Transition<Boolean>,
    modifier: Modifier = Modifier,
) {
    val barAlpha by transition.animateOnce(label = "barAlpha") {
        tween(IntroTiming.BAR_FADE_DURATION, IntroTiming.BAR_DELAY, StandardEasing)
    }
    val progress by transition.animateOnce(label = "barProgress") {
        keyframes {
            durationMillis = IntroTiming.BAR_DURATION
            delayMillis = IntroTiming.BAR_DELAY
            0f at 0 using DeceleratingEasing
            BAR_MID_PROGRESS at IntroTiming.BAR_MID using DeceleratingEasing
        }
    }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 72.dp)
            .alpha(barAlpha)
            .width(112.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(alpha = 0.22f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Color.White),
        )
    }
}

@Composable
private fun Footer(
    transition: Transition<Boolean>,
    modifier: Modifier = Modifier,
) {
    val footAlpha by transition.animateOnce(label = "footAlpha") {
        tween(IntroTiming.FOOT_DURATION, IntroTiming.FOOT_DELAY, StandardEasing)
    }

    Text(
        text = stringResource(R.string.intro_footer),
        style = AppTextStyles.monoCaption,
        color = Color.White.copy(alpha = 0.45f),
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 32.dp)
            .alpha(footAlpha),
    )
}

/**
 * 인트로 배경이 코발트라 시스템 바 아이콘을 밝게 바꾼다.
 * 앱 테마는 라이트 고정(어두운 아이콘)이라 그대로 두면 코발트 위에서 아이콘이 보이지 않는다.
 */
@Composable
private fun ForceLightSystemBars() {
    val view = LocalView.current

    DisposableEffect(view) {
        val window = view.context.findWindow()
            ?: return@DisposableEffect onDispose { }
        val controller = WindowCompat.getInsetsController(window, view)

        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        onDispose {
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }
}

private tailrec fun Context.findWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.findWindow()
    else -> null
}

/** 워드마크·태그라인 공통 등장 — 아래에서 [TEXT_RISE]만큼 올라오며 나타난다. */
private fun Modifier.riseIn(progress: Float): Modifier =
    alpha(progress).offset(y = TEXT_RISE * (1f - progress))

/** 인트로가 떠 있는 동안 아래 홈으로 터치가 새지 않도록 삼킨다. */
private fun Modifier.blockTouches(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

/** `false → true` 한 방향으로만 재생되는 값. 되돌아올 일이 없어 구간을 나누지 않는다. */
@Composable
private fun Transition<Boolean>.animateOnce(
    from: Float = 0f,
    to: Float = 1f,
    label: String,
    transitionSpec: @Composable Transition.Segment<Boolean>.() -> FiniteAnimationSpec<Float>,
) = animateFloat(transitionSpec = transitionSpec, label = label) { isStarted ->
    if (isStarted) to else from
}

/** 기획서 CSS의 타이밍(ms)을 그대로 옮긴 값. */
private object IntroTiming {
    const val MARK_DELAY = 50
    const val MARK_DURATION = 420

    const val BOARD_DELAY = 100
    const val BOARD_DURATION = 300

    const val LINE_DURATION = 260
    const val LINE1_DELAY = 300
    const val LINE2_DELAY = 400
    const val LINE3_DELAY = 500

    const val DOT_DELAY = 700
    const val DOT_DURATION = 340

    /** 튀어 오른 정점(전체의 60%). 여기서 불투명도도 다 찬다. */
    const val DOT_PEAK = 204

    const val TEXT_DURATION = 420
    const val WORD_DELAY = 550
    const val TAG_DELAY = 920

    const val BAR_DELAY = 350
    const val BAR_FADE_DURATION = 300
    const val BAR_DURATION = 1500
    const val BAR_MID = 930

    const val FOOT_DELAY = 1150
    const val FOOT_DURATION = 500

    /** 인트로가 걷히기 시작하는 시점 */
    const val EXIT_DELAY = 1740

    /** 전환까지 포함한 인트로 전체 길이 */
    const val TOTAL = 2020
}

private const val EXIT_SCALE = 1.045f
private const val DOT_OVERSHOOT = 1.25f
private const val BAR_MID_PROGRESS = 0.74f
private val TEXT_RISE = 9.dp

/** CSS `cubic-bezier(.2,.9,.3,1)` — 빠르게 나와 부드럽게 멎는다. */
private val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f)

/** CSS `ease` */
private val StandardEasing: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

/** CSS `cubic-bezier(.35,.1,.2,1)` — 로딩 바가 초반에 빨리 차오른다. */
private val DeceleratingEasing: Easing = CubicBezierEasing(0.35f, 0.1f, 0.2f, 1f)

@Preview(showBackground = true)
@Composable
private fun IntroScreenPreview() {
    AppTheme {
        IntroScreen(onFinished = {})
    }
}
