package com.ams.youthhouse.core.network

/**
 * 모든 HTTP 클라이언트가 공유하는 설정.
 *
 * 공공데이터포털을 직접 부르던 시절에는 여기에 baseUrl과 serviceKey가 있었다.
 * 지금은 앱이 자체 백엔드 하나만 부르므로 접속 정보는 [MosstisConfig]가 갖고,
 * 여기에는 클라이언트를 어떻게 만들지에 대한 공통 스위치만 남는다.
 *
 * core는 BuildConfig를 모르므로 값은 app/di가 채워 넣는다.
 */
data class NetworkConfig(
    val isLoggingEnabled: Boolean,
)
