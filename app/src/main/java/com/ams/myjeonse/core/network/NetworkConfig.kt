package com.ams.myjeonse.core.network

/**
 * 네트워크 계층이 필요로 하는 설정값.
 *
 * core는 BuildConfig(앱 빌드 산출물)를 직접 참조하지 않는다.
 * 실제 값 주입은 app.di.NetworkConfigModule이 담당한다.
 *
 * @param serviceKey data.go.kr 인증키. **URL 인코딩된 값**을 그대로 담는다.
 */
data class NetworkConfig(
    val baseUrl: String,
    val serviceKey: String,
    val isLoggingEnabled: Boolean,
)
