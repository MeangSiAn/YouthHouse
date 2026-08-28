package com.ams.youthhouse.core.common.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * D-day 숫자가 마감 임박 카드의 핵심이므로 경계값을 고정한다.
 */
class DateArithmeticTest {

    @Test
    fun `같은 날은 0이다`() {
        assertEquals(0, daysBetween("20260803", "20260803"))
    }

    @Test
    fun `하루 차이는 1이다`() {
        assertEquals(1, daysBetween("20260803", "20260804"))
    }

    @Test
    fun `월말을 넘어가도 정확하다`() {
        assertEquals(1, daysBetween("20260228", "20260301")) // 2026년은 평년
        assertEquals(1, daysBetween("20260731", "20260801"))
        assertEquals(31, daysBetween("20260801", "20260901"))
    }

    @Test
    fun `윤년 2월 29일을 센다`() {
        assertEquals(2, daysBetween("20240228", "20240301")) // 2024년은 윤년
    }

    @Test
    fun `연말을 넘어가도 정확하다`() {
        assertEquals(1, daysBetween("20261231", "20270101"))
    }

    @Test
    fun `과거로 가면 음수다`() {
        assertEquals(-5, daysBetween("20260808", "20260803"))
    }

    @Test
    fun `형식이 어긋나면 null이다`() {
        assertNull(daysBetween("2026-08-03", "20260804"))
        assertNull(daysBetween("2026080", "20260804"))
        assertNull(daysBetween("", "20260804"))
        assertNull(daysBetween(null, "20260804"))
        assertNull(daysBetween("20260803", null))
    }

    @Test
    fun `존재하지 않는 날짜는 null이다`() {
        assertNull(daysBetween("20261301", "20260804")) // 13월
        assertNull(daysBetween("20260230", "20260804")) // 2월 30일
    }
}
