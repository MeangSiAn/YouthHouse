package com.ams.youthhouse.core.notice.data.mapper

import com.ams.youthhouse.core.notice.data.dto.NoticeItemDto
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoticeMapperTest {

    /** API는 값 없음을 null이 아니라 빈 문자열로 준다. */
    @Test
    fun `빈 문자열은 null로 정규화된다`() {
        val notice = NoticeItemDto(
            pblancId = "1",
            pblancNm = "제목",
            hsmpNm = "",
            fullAdres = "",
            beforePblancId = "",
            heatMthdNm = "",
        ).toDomain(NoticeCategory.RENTAL)

        assertNull(notice.complexName)
        assertNull(notice.address.fullAddress)
        assertNull(notice.previousNoticeId)
        assertNull(notice.heatingMethodName)
    }

    /** 금액·세대수의 값 없음은 0으로 온다. 0원과 미기재를 구분할 방법이 없어 null로 본다. */
    @Test
    fun `0인 금액과 세대수는 null로 정규화된다`() {
        val notice = NoticeItemDto(rentGtn = 0, mtRntchrg = 0, totHshldCo = 0, sumSuplyCo = 0)
            .toDomain(NoticeCategory.RENTAL)

        assertNull(notice.price.minDeposit)
        assertNull(notice.price.minMonthlyRent)
        assertNull(notice.totalHouseholdCount)
        assertNull(notice.supplyCount)
    }

    @Test
    fun `값이 있는 필드는 그대로 매핑된다`() {
        val notice = NoticeItemDto(
            pblancId = "20955",
            houseSn = 2,
            pblancNm = "매입임대 모집공고",
            sttusNm = "일반공고",
            brtcNm = "울산광역시",
            signguNm = "중구",
            rentGtn = 12_000_000,
            sumSuplyCo = 3,
            beginDe = "20260810",
            endDe = "20260811",
        ).toDomain(NoticeCategory.RENTAL)

        assertEquals("20955", notice.pblancId)
        assertEquals(2, notice.houseSn)
        assertEquals("매입임대 모집공고", notice.title)
        assertEquals("일반공고", notice.statusName)
        assertEquals("울산광역시", notice.address.provinceName)
        assertEquals("중구", notice.address.districtName)
        assertEquals(12_000_000, notice.price.minDeposit)
        assertEquals(3, notice.supplyCount)
    }

    @Test
    fun `원문 링크는 모바일 PC 원공고 순으로 고른다`() {
        val mobileFirst = NoticeItemDto(
            url = "https://apply.lh.or.kr/x",
            pcUrl = "https://www.myhome.go.kr/x",
            mobileUrl = "https://m.myhome.go.kr/x",
        ).toDomain(NoticeCategory.RENTAL)
        assertEquals("https://m.myhome.go.kr/x", mobileFirst.preferredUrl)

        val pcFallback = NoticeItemDto(
            url = "https://apply.lh.or.kr/x",
            pcUrl = "https://www.myhome.go.kr/x",
            mobileUrl = "",
        ).toDomain(NoticeCategory.RENTAL)
        assertEquals("https://www.myhome.go.kr/x", pcFallback.preferredUrl)

        val noticeFallback = NoticeItemDto(url = "https://apply.lh.or.kr/x").toDomain(NoticeCategory.RENTAL)
        assertEquals("https://apply.lh.or.kr/x", noticeFallback.preferredUrl)
    }

    /** 분양은 보증금·월세·공급유형이 응답에 없어 전부 null이어야 한다. */
    @Test
    fun `분양은 임대 전용 필드가 null이고 분야가 SALE이다`() {
        val notice = NoticeItemDto(
            pblancId = "1462",
            pblancNm = "성남복정2 신혼희망타운(공공분양)",
            houseTyNm = "아파트",
            enty = 43_287_000,
            surlus = 304_680_000,
        ).toDomain(NoticeCategory.SALE)

        assertEquals(NoticeCategory.SALE, notice.category)
        assertNull(notice.supplyTypeName)
        assertNull(notice.price.minDeposit)
        assertNull(notice.price.minMonthlyRent)
        assertNull(notice.totalHouseholdCount)
        assertEquals(43_287_000, notice.price.minDownPayment)
        assertEquals(304_680_000, notice.price.minBalance)
    }

    /** YYYYMMDD는 사전순 비교가 곧 시간순 비교라 문자열만으로 기간 판정이 된다. */
    @Test
    fun `모집 기간 판정은 경계값을 포함한다`() {
        val notice = NoticeItemDto(beginDe = "20260810", endDe = "20260811").toDomain(NoticeCategory.RENTAL)

        assertEquals(false, notice.isOpenOn("20260809"))
        assertEquals(true, notice.isOpenOn("20260810"))
        assertEquals(true, notice.isOpenOn("20260811"))
        assertEquals(false, notice.isOpenOn("20260812"))
    }

    @Test
    fun `모집 기간이 비어 있으면 모집중이 아니다`() {
        val notice = NoticeItemDto(beginDe = "", endDe = "").toDomain(NoticeCategory.RENTAL)

        assertEquals(false, notice.isOpenOn("20260810"))
    }
}
