package com.ams.myjeonse.core.notice.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 지역 코드는 실제 API 호출로 하나씩 확인한 값이다.
 * 표준 행정코드와 다른 값이 섞여 있어, 누가 "표준에 맞게" 고치면 조용히 조회가 0건이 된다.
 */
class NoticeRegionTest {

    /** 광주(29)·전남(46)은 이 API에서 아무것도 돌려주지 않는다. 통합 시도는 비표준 코드 12를 쓴다. */
    @Test
    fun `전남광주통합특별시는 비표준 코드 12를 쓴다`() {
        assertEquals("12", NoticeRegion.JEONNAM_GWANGJU.code)
        assertEquals("전남광주통합특별시", NoticeRegion.JEONNAM_GWANGJU.regionName)
    }

    @Test
    fun `실측으로 확인한 16개 시도를 모두 갖는다`() {
        assertEquals(16, NoticeRegion.entries.size)
    }

    @Test
    fun `코드는 중복되지 않는다`() {
        val codes = NoticeRegion.entries.map { it.code }

        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `코드는 두 자리 숫자다`() {
        NoticeRegion.entries.forEach { region ->
            assertTrue(
                "${region.name}의 코드가 두 자리 숫자가 아님: ${region.code}",
                region.code.matches(Regex("^\\d{2}$")),
            )
        }
    }

    @Test
    fun `지역명은 비어 있지 않다`() {
        NoticeRegion.entries.forEach { region ->
            assertTrue(region.name, region.regionName.isNotBlank())
        }
    }
}
