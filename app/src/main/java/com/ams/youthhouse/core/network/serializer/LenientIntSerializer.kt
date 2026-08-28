package com.ams.youthhouse.core.network.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 숫자 필드가 숫자로도, 문자열로도, 빈 문자열로도 오는 API를 위한 관용 역직렬화기.
 *
 * data.go.kr은 같은 필드를 레코드마다 다른 타입으로 준다.
 * 예: `rsdtRcritNtcList`의 `totHshldCo`는 383건 중 301건이 `0`(숫자),
 * 82건이 `""`(빈 문자열)로 내려온다. `isLenient`는 따옴표 붙은 숫자까지는 받아주지만
 * 빈 문자열은 `Int`로 만들 수 없어 역직렬화가 통째로 실패한다.
 *
 * 값이 없거나 숫자로 읽을 수 없으면 [FALLBACK]을 쓴다.
 * "값 없음"과 "0"의 구분은 매퍼가 담당한다.
 */
object LenientIntSerializer : KSerializer<Int> {

    private const val FALLBACK = 0

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LenientInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeInt()
        val primitive = jsonDecoder.decodeJsonElement() as? JsonPrimitive ?: return FALLBACK
        return primitive.contentOrNull?.trim()?.toIntOrNull() ?: FALLBACK
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }
}
