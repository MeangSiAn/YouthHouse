package com.ams.youthhouse.core.common.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormatTest {

    @Test
    fun `YYYYMMDD를 점 구분 형식으로 변환한다`() {
        assertEquals("2026.08.03", "20260803".formatYearMonthDay())
    }

    @Test
    fun `형식이 다른 문자열은 원문을 그대로 돌려준다`() {
        assertEquals("2026-08", "2026-08".formatYearMonthDay())
        assertEquals("2026080", "2026080".formatYearMonthDay())
        assertEquals("2026080X", "2026080X".formatYearMonthDay())
    }

    @Test
    fun `날짜가 null이면 null이다`() {
        assertNull(null.formatYearMonthDay())
    }

    @Test
    fun `시작일과 종료일을 물결로 잇는다`() {
        assertEquals("2026.08.10 ~ 2026.08.11", formatDateRange("20260810", "20260811"))
    }

    @Test
    fun `한쪽 날짜만 있으면 그쪽만 표시한다`() {
        assertEquals("2026.08.10", formatDateRange("20260810", null))
        assertEquals("2026.08.11", formatDateRange(null, "20260811"))
        assertNull(formatDateRange(null, null))
    }

    @Test
    fun `금액에 천단위 구분자를 넣는다`() {
        assertEquals("1,200,000", 1_200_000.formatThousands())
        assertEquals("0", 0.formatThousands())
        assertEquals("120", 120.formatThousands())
    }

    @Test
    fun `금액이 null이면 null이다`() {
        assertNull(null.formatThousands())
    }
}
