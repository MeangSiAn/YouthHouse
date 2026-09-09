package com.ams.youthhouse.core.common.error

/**
 * 앱 전역에서 다루는 실패 유형.
 *
 * 사용자에게 보여줄 문구는 담지 않는다. 지역화는 presentation의 책임이다.
 */
sealed class AppException(cause: Throwable? = null) : Exception(cause) {

    /** 오프라인, 타임아웃 등 I/O 실패. */
    class Network(cause: Throwable) : AppException(cause)

    /** HTTP 401/403 — 백엔드 API 키가 없거나 유효하지 않음. */
    class Unauthorized(val httpCode: Int, cause: Throwable? = null) : AppException(cause)

    /** HTTP 4xx/5xx. */
    class Server(
        val code: String?,
        val serverMessage: String?,
        cause: Throwable? = null,
    ) : AppException(cause)

    /** 응답 본문이 기대한 JSON 스키마가 아님 (HTTP 200 + XML 오류 응답 등). */
    class Parse(cause: Throwable) : AppException(cause)

    class Unknown(cause: Throwable) : AppException(cause)
}
