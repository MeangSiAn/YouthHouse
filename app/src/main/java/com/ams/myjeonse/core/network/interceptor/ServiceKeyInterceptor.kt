package com.ams.myjeonse.core.network.interceptor

import com.ams.myjeonse.core.network.NetworkConfig
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

internal const val SERVICE_KEY = "serviceKey"

/**
 * data.go.kr 오픈API는 모든 요청에 `serviceKey` 쿼리 파라미터를 요구하며,
 * 서버가 받아야 하는 것은 **URL 인코딩된 형태**다.
 *
 * 포털은 같은 키를 encoding/decoding 두 형태로 보여주므로 어느 쪽을 붙여넣어도
 * 동작하도록 정규화한다. Base64 키의 `+`와 `=`는 인코딩된 형태에는 절대 원문으로
 * 남아 있지 않으므로, 이 두 문자의 유무로 두 형태를 구분할 수 있다.
 */
internal fun HttpUrl.Builder.appendServiceKey(serviceKey: String): HttpUrl.Builder =
    if (serviceKey.any { it == '+' || it == '=' }) {
        // decoding 형태 → OkHttp가 인코딩하도록 맡긴다.
        addQueryParameter(SERVICE_KEY, serviceKey)
    } else {
        // encoding 형태 → 이미 인코딩되어 있으므로 이중 인코딩을 막는다.
        addEncodedQueryParameter(SERVICE_KEY, serviceKey)
    }

class ServiceKeyInterceptor @Inject constructor(
    private val networkConfig: NetworkConfig,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val alreadyHasKey = request.url.queryParameterNames.contains(SERVICE_KEY)
        if (networkConfig.serviceKey.isBlank() || alreadyHasKey) {
            return chain.proceed(request)
        }

        val url = request.url
            .newBuilder()
            .appendServiceKey(networkConfig.serviceKey)
            .build()

        return chain.proceed(
            request.newBuilder()
                .url(url)
                .build(),
        )
    }
}
