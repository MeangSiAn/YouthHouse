package com.ams.youthhouse.core.network

/**
 * 자체 백엔드(data.mosstis.com) 접속 설정.
 *
 * 접속 정보(주소·인증키)를 클라이언트 공통 스위치([NetworkConfig])와 분리해 둔다.
 * core는 BuildConfig를 모르므로 값은 app/di가 채워 넣는다.
 */
data class MosstisConfig(
    val baseUrl: String,
    val apiKey: String,
)
