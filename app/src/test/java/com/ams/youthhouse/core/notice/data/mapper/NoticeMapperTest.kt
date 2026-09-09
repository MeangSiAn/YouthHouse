package com.ams.youthhouse.core.notice.data.mapper

import com.ams.youthhouse.core.notice.data.dto.NoticeItemDto
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoticeMapperTest {

    /**
     * 도메인은 날짜를 사전순으로 비교해 기간을 판정하고 `daysBetween`이 8자리를 전제한다.
     * 하이픈이 남으면 D-day가 통째로 어긋난다.
     */
    @Test
    fun `날짜에서 하이픈을 지워 YYYYMMDD로 만든다`() {
        val notice = NoticeItemDto(
            applyStart = "2026-08-31",
            applyEnd = "2026-09-04",
            announceDate = "2026-08-19",
            winnerDate = "2026-12-03",
        ).toDomain()

        assertEquals("20260831", notice.period.beginDate)
        assertEquals("20260904", notice.period.endDate)
        assertEquals("20260819", notice.period.noticeDate)
        assertEquals("20261203", notice.period.winnerAnnounceDate)
    }

    @Test
    fun `분야는 한글 이름으로 온다`() {
        assertEquals(NoticeCategory.SALE, NoticeItemDto(category = "공공분양").toDomain().category)
        assertEquals(NoticeCategory.RENTAL, NoticeItemDto(category = "공공임대").toDomain().category)
    }

    /** 서버가 분야 이름을 바꾸더라도 목록이 통째로 죽지 않아야 한다. */
    @Test
    fun `모르는 분야는 임대로 접는다`() {
        assertEquals(NoticeCategory.RENTAL, NoticeItemDto(category = "신설분야").toDomain().category)
    }

    @Test
    fun `빈 문자열과 누락 필드는 null로 정규화된다`() {
        val notice = NoticeItemDto(complexName = "", heatType = null, contact = "  ").toDomain()

        assertNull(notice.complexName)
        assertNull(notice.heatingMethodName)
        assertNull(notice.contact)
        assertNull(notice.address.fullAddress)
    }

    /** 0원과 미기재를 구분할 방법이 없어 0을 값 없음으로 본다. */
    @Test
    fun `0인 금액과 호수는 null로 정규화된다`() {
        val notice = NoticeItemDto(deposit = 0, monthlyRent = 0, totalUnits = 0).toDomain()

        assertNull(notice.price.minDeposit)
        assertNull(notice.price.minMonthlyRent)
        assertNull(notice.supplyCount)
    }

    @Test
    fun `금액과 공급 호수를 그대로 옮긴다`() {
        val notice = NoticeItemDto(
            deposit = 2_778_000,
            monthlyRent = 55_360,
            balance = 2_639_100,
            totalUnits = 5,
        ).toDomain()

        assertEquals(2_778_000, notice.price.minDeposit)
        assertEquals(55_360, notice.price.minMonthlyRent)
        assertEquals(2_639_100, notice.price.minBalance)
        assertEquals(5, notice.supplyCount)
    }

    /**
     * 두 URL의 역할이 갈린다 — 신청 버튼은 접수처로, "공고 상세 보기"는 마이홈 원문으로 간다.
     * 이 대응이 뒤집히면 사용자가 신청 버튼을 눌렀는데 안내문이 열린다.
     */
    @Test
    fun `접수처와 원문 링크가 갈린다`() {
        val notice = NoticeItemDto(
            applyUrl = "https://apply.lh.or.kr/x",
            detailUrl = "https://www.myhome.go.kr/y",
        ).toDomain()

        assertEquals("https://apply.lh.or.kr/x", notice.noticeUrl)
        assertEquals("https://www.myhome.go.kr/y", notice.preferredUrl)
    }

    @Test
    fun `지역은 시도와 시군구로 나뉘어 담긴다`() {
        val notice = NoticeItemDto(sido = "경기도", sigungu = "평택시").toDomain()

        assertEquals("경기도", notice.address.provinceName)
        assertEquals("평택시", notice.address.districtName)
    }
}
