package com.ams.youthhouse.core.network.interceptor

import com.ams.youthhouse.core.network.MosstisConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** 자체 백엔드의 인증 헤더(`x-api-key`)를 모든 요청에 붙인다. */
class ApiKeyHeaderInterceptor @Inject constructor(
    private val config: MosstisConfig,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (config.apiKey.isBlank()) return chain.proceed(chain.request())

        return chain.proceed(
            chain.request().newBuilder()
                .header(HEADER_NAME, config.apiKey)
                .build(),
        )
    }

    private companion object {
        const val HEADER_NAME = "x-api-key"
    }
}
