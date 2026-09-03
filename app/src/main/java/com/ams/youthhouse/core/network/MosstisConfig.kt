package com.ams.youthhouse.core.network

/**
 * 자체 백엔드(data.mosstis.com) 접속 설정.
 *
 * data.go.kr용 [NetworkConfig]와 분리한다 — 인증 방식이 다르고(쿼리 serviceKey vs
 * x-api-key 헤더), 한쪽 설정 변경이 다른 쪽 클라이언트를 재구성하게 하고 싶지 않다.
 * core는 BuildConfig를 모르므로 값은 app/di가 채워 넣는다.
 */
data class MosstisConfig(
    val baseUrl: String,
    val apiKey: String,
)
