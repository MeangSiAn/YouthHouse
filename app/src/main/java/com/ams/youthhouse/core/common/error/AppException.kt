package com.ams.youthhouse.core.common.error

/**
 * 앱 전역에서 다루는 실패 유형.
 *
 * 사용자에게 보여줄 문구는 담지 않는다. 지역화는 presentation의 책임이다.
 */
sealed class AppException(cause: Throwable? = null) : Exception(cause) {

    /** 오프라인, 타임아웃 등 I/O 실패. */
    class Network(cause: Throwable) : AppException(cause)

    /**
     * HTTP 401/403 — data.go.kr 인증키가 등록되지 않았거나 승인되지 않음.
     *
     * 이 경우 서버는 JSON이 아니라 XML 본문을 돌려주지만,
     * Retrofit이 비-2xx 응답에서 컨버터를 태우지 않고 곧바로 예외를 던지므로
     * XML이 역직렬화에 도달하지 않는다.
     */
    class Unauthorized(val httpCode: Int, cause: Throwable? = null) : AppException(cause)

    /** HTTP 4xx/5xx 또는 응답 header의 resultCode가 실패(`02`, `99` 등)인 경우. */
    class Server(
        val code: String?,
        val serverMessage: String?,
        cause: Throwable? = null,
    ) : AppException(cause)

    /** 응답 본문이 기대한 JSON 스키마가 아님 (HTTP 200 + XML 오류 응답 등). */
    class Parse(cause: Throwable) : AppException(cause)

    class Unknown(cause: Throwable) : AppException(cause)
}
