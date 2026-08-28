package com.ams.youthhouse.core.common

/**
 * 앱 빌드 정보.
 *
 * `BuildConfig`는 app 모듈의 산출물이라 core가 직접 참조하지 않는다.
 * 값 주입은 `app.di.AppInfoModule`이 담당한다 — `NetworkConfig`와 같은 방식이다.
 */
data class AppInfo(
    val versionName: String,
)
