package com.ams.youthhouse.core.network.serializer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class LenientIntSerializerTest {

    @Serializable
    private data class Holder(
        @Serializable(with = LenientIntSerializer::class)
        val value: Int = 0,
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private fun decode(raw: String): Int = json.decodeFromString<Holder>(raw).value

    @Test
    fun `숫자는 그대로 읽는다`() {
        assertEquals(120, decode("""{"value": 120}"""))
        assertEquals(0, decode("""{"value": 0}"""))
    }

    @Test
    fun `따옴표 붙은 숫자를 읽는다`() {
        assertEquals(120, decode("""{"value": "120"}"""))
    }

    /** 이 케이스가 목록 페이지 전체를 죽였던 원인이다. */
    @Test
    fun `빈 문자열은 0으로 읽는다`() {
        assertEquals(0, decode("""{"value": ""}"""))
    }

    @Test
    fun `공백이 섞인 숫자를 읽는다`() {
        assertEquals(37, decode("""{"value": " 37 "}"""))
    }

    @Test
    fun `숫자로 읽을 수 없는 문자열은 0이다`() {
        assertEquals(0, decode("""{"value": "미정"}"""))
    }

    @Test
    fun `null과 필드 누락은 0이다`() {
        assertEquals(0, decode("""{"value": null}"""))
        assertEquals(0, decode("""{}"""))
    }

    @Test
    fun `직렬화하면 숫자로 나간다`() {
        assertEquals("""{"value":120}""", json.encodeToString(Holder(120)))
    }
}
