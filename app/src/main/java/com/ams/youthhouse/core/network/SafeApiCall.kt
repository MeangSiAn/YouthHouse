package com.ams.youthhouse.core.network

import com.ams.youthhouse.core.common.error.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Retrofit/직렬화 예외를 [AppException]으로 정규화한다.
 *
 * suspend 함수가 `Response<T>`가 아닌 raw 타입을 반환할 때 Retrofit은
 * 비-2xx 응답에서 **컨버터를 태우지 않고** 곧바로 [HttpException]을 던진다.
 * 덕분에 인증 실패(401)의 응답 본문이 JSON 역직렬화에 도달하지 않고
 * [AppException.Unauthorized]로 흡수된다.
 *
 * [SerializationException] 분기는 방어선이다 — 게이트웨이가 HTTP 200으로
 * JSON이 아닌 본문을 돌려주는 경우가 있다.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): T = try {
    block()
} catch (exception: CancellationException) {
    throw exception
} catch (exception: HttpException) {
    throw when (exception.code()) {
        HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> AppException.Unauthorized(exception.code(), exception)
        else -> AppException.Server(
            code = exception.code().toString(),
            serverMessage = exception.message(),
            cause = exception,
        )
    }
} catch (exception: SerializationException) {
    throw AppException.Parse(exception)
} catch (exception: IOException) {
    throw AppException.Network(exception)
} catch (throwable: Throwable) {
    throw AppException.Unknown(throwable)
}

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
