package com.ams.youthhouse.core.ui.navigation

import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8

/**
 * `@Serializable` 객체를 Navigation 인자 하나로 실어 나르는 [NavType].
 *
 * 값은 JSON으로 직렬화한 뒤 **Base64URL**로 인코딩한다.
 * - 출력 문자셋이 `A-Za-z0-9-_`뿐이라 라우트 문자열을 URI로 조립·분해하는 과정에서
 *   percent-encoding이 이중 디코딩되는 문제가 원천 차단된다.
 * - 한글 percent-encoding(글자당 9자) 대비 크기가 절반 이하다.
 * - okio는 OkHttp를 통해 이미 클래스패스에 있고 순수 JVM이라 유닛 테스트에서 그대로 돈다.
 *   (`android.util.Base64`는 유닛 테스트에서 스텁이고, `java.util.Base64`는 API 26부터라 minSdk 24에서 못 쓴다.)
 *
 * 여기의 [Json]은 Hilt가 제공하는 네트워크용 인스턴스와 별개다.
 * [NavType]은 DI 그래프 밖에서 생성되므로 주입받을 수 없다.
 */
class JsonNavType<T : Any>(
    private val serializer: KSerializer<T>,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : NavType<T>(isNullableAllowed = false) {

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, encode(value))
    }

    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let(::decode)

    override fun parseValue(value: String): T = decode(value)

    override fun serializeAsValue(value: T): String = encode(value)

    private fun encode(value: T): String =
        json.encodeToString(serializer, value)
            .encodeUtf8()
            .base64Url()
            .trimEnd('=')

    private fun decode(value: String): T {
        val decoded = requireNotNull(value.decodeBase64()) {
            "Navigation 인자가 Base64URL이 아닙니다."
        }
        return json.decodeFromString(serializer, decoded.utf8())
    }
}
