package com.ams.myjeonse.core.network.interceptor

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * data.go.kr 인증키를 쿼리에 붙일 때 어떤 형태(encoding/decoding)를 써야 하는지 고정한다.
 *
 * 포털은 같은 키를 두 형태로 제공한다.
 * - encoding: `...7J%2B5sHrmtg%3D%3D`
 * - decoding: `...7J+5sHrmtg==`
 *
 * 서버가 최종적으로 받아야 하는 것은 **인코딩된 형태**이며,
 * 어느 쪽을 넣느냐보다 "넣는 방식과 짝이 맞느냐"가 관건이다.
 */
class ServiceKeyEncodingTest {

    @Test
    fun `encoding 키 + addEncodedQueryParameter 는 올바른 형태를 만든다`() {
        val actual = encodedServiceKey { addEncodedQueryParameter(SERVICE_KEY, ENCODED_KEY) }

        assertEquals(ENCODED_KEY, actual)
    }

    /** OkHttp 5는 `+`도 `%2B`로 이스케이프하므로 decoding 키 + 일반 파라미터도 동일 결과가 된다. */
    @Test
    fun `decoding 키 + addQueryParameter 도 같은 형태를 만든다`() {
        val actual = encodedServiceKey { addQueryParameter(SERVICE_KEY, DECODED_KEY) }

        assertEquals(ENCODED_KEY, actual)
    }

    /**
     * 짝이 어긋나는 조합.
     * `=`는 `%3D`로 이스케이프되지만 `+`는 원문으로 남아 서버가 공백으로 해석 → 인증 실패.
     */
    @Test
    fun `decoding 키 + addEncodedQueryParameter 는 plus 가 인코딩되지 않아 깨진다`() {
        val actual = encodedServiceKey { addEncodedQueryParameter(SERVICE_KEY, DECODED_KEY) }

        assertNotEquals(ENCODED_KEY, actual)
        assertTrue("`+`가 원문으로 남아야 재현됨: $actual", actual!!.contains('+'))
    }

    /** ServiceKeyInterceptor가 쓰는 경로: 두 형태 모두 올바른 형태로 정규화된다. */
    @Test
    fun `appendServiceKey 는 encoding 과 decoding 키를 같은 형태로 정규화한다`() {
        assertEquals(ENCODED_KEY, encodedServiceKey { appendServiceKey(ENCODED_KEY) })
        assertEquals(ENCODED_KEY, encodedServiceKey { appendServiceKey(DECODED_KEY) })
    }

    private fun encodedServiceKey(
        build: HttpUrl.Builder.() -> HttpUrl.Builder,
    ): String? = BASE_URL.toHttpUrl()
        .newBuilder()
        .run(build)
        .build()
        .encodedQuery
        ?.substringAfter("$SERVICE_KEY=")

    private companion object {
        const val BASE_URL = "https://apis.data.go.kr/sample"

        const val ENCODED_KEY =
            "lHjQO7Po4AVKEkZF0sim8m6fiwqlRaV6njj3jZUUV4gCovZVOYjNNMwsPRhzsTUtGSMHVzvWsuuH7J%2B5sHrmtg%3D%3D"
        const val DECODED_KEY =
            "lHjQO7Po4AVKEkZF0sim8m6fiwqlRaV6njj3jZUUV4gCovZVOYjNNMwsPRhzsTUtGSMHVzvWsuuH7J+5sHrmtg=="
    }
}
