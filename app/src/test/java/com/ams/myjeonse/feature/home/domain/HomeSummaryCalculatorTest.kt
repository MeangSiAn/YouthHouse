package com.ams.myjeonse.feature.home.domain

import com.ams.myjeonse.core.notice.domain.model.Notice
import com.ams.myjeonse.core.notice.domain.model.NoticeAddress
import com.ams.myjeonse.core.notice.domain.model.NoticePeriod
import com.ams.myjeonse.core.notice.domain.model.NoticePrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 홈 화면 로직 전체의 안전망.
 *
 * 세 가지 상태(지역 미설정 / 공고 없음 / 기본)의 분기 조건이 여기서 결정되므로
 * 화면보다 이 계산이 먼저 맞아야 한다.
 */
class HomeSummaryCalculatorTest {

    /** 같은 공고가 시군구별로 쪼개져 오는 API 특성. 안 걷어내면 같은 공고가 3번 보인다. */
    @Test
    fun `같은 공고가 여러 행으로 와도 한 건으로 센다`() {
        val notices = listOf(
            notice(pblancId = "1", signgu = "중구", noticeDate = TODAY),
            notice(pblancId = "1", signgu = "남구", noticeDate = TODAY),
            notice(pblancId = "1", signgu = "북구", noticeDate = TODAY),
            notice(pblancId = "2", signgu = "동구", noticeDate = TODAY),
        )

        val summary = notices.toHomeSummary(TODAY)

        assertEquals(2, summary.todayNoticeTotalCount)
        assertEquals(2, summary.todayNotices.size)
    }

    @Test
    fun `오늘 마감이면 daysLeft는 0이다`() {
        val summary = listOf(openNotice(end = TODAY)).toHomeSummary(TODAY)

        assertEquals(0, summary.urgent?.daysLeft)
        assertEquals(1, summary.closingTodayCount)
    }

    @Test
    fun `마감 임박 경계는 D-3까지 노출하고 D-4는 숨긴다`() {
        val threeDaysLeft = listOf(openNotice(end = "20260806")).toHomeSummary(TODAY)
        assertEquals(3, threeDaysLeft.urgent?.daysLeft)

        val fourDaysLeft = listOf(openNotice(end = "20260807")).toHomeSummary(TODAY)
        assertNull("D-4는 마감 임박이 아니다", fourDaysLeft.urgent)
    }

    @Test
    fun `마감이 가장 급한 한 건만 고른다`() {
        val notices = listOf(
            openNotice(pblancId = "far", end = "20260806"),
            openNotice(pblancId = "near", end = "20260804"),
            openNotice(pblancId = "mid", end = "20260805"),
        )

        val summary = notices.toHomeSummary(TODAY)

        assertEquals("near", summary.urgent?.notice?.pblancId)
        assertEquals(1, summary.urgent?.daysLeft)
    }

    @Test
    fun `오늘의 새 공고는 상한을 넘지 않지만 전체 건수는 따로 보관한다`() {
        val notices = (1..5).map { notice(pblancId = "$it", noticeDate = TODAY) }

        val summary = notices.toHomeSummary(TODAY)

        assertEquals(TODAY_NOTICE_LIMIT, summary.todayNotices.size)
        assertEquals(5, summary.todayNoticeTotalCount)
    }

    @Test
    fun `접수중과 예정을 기간으로 구분한다`() {
        val notices = listOf(
            openNotice(pblancId = "open1", end = "20260810"),
            openNotice(pblancId = "open2", end = "20260812"),
            upcomingNotice(pblancId = "soon", begin = "20260901"),
        )

        val summary = notices.toHomeSummary(TODAY)

        assertEquals(2, summary.openCount)
        assertEquals(1, summary.upcomingCount)
        assertTrue(summary.hasOpenNotice)
    }

    /** statusName(sttusNm)은 "일반공고" 같은 공고 종류라 접수 상태 판정에 쓰면 안 된다. */
    @Test
    fun `statusName은 접수 상태 판정에 영향을 주지 않는다`() {
        val summary = listOf(
            openNotice(end = "20260810").copy(statusName = "정정공고"),
        ).toHomeSummary(TODAY)

        assertEquals(1, summary.openCount)
    }

    @Test
    fun `접수중이 없으면 다음 시작일을 알려준다`() {
        val notices = listOf(
            upcomingNotice(pblancId = "a", begin = "20260901"),
            upcomingNotice(pblancId = "b", begin = "20260816"),
        )

        val summary = notices.toHomeSummary(TODAY)

        assertEquals(0, summary.openCount)
        assertEquals(false, summary.hasOpenNotice)
        assertEquals("20260816", summary.nextOpenDate)
    }

    @Test
    fun `기간이 비어 있는 공고는 어느 쪽으로도 세지 않는다`() {
        val summary = listOf(
            notice(pblancId = "no-period", begin = null, end = null),
        ).toHomeSummary(TODAY)

        assertEquals(0, summary.openCount)
        assertEquals(0, summary.upcomingCount)
        assertNull(summary.urgent)
    }

    /** 지역 미설정 화면은 몇 건만 받아오므로 "오늘" 조건 없이 보여줄 목록이 따로 필요하다. */
    @Test
    fun `최근 공고는 날짜와 무관하게 상한만큼 담는다`() {
        val notices = (1..5).map { notice(pblancId = "$it", noticeDate = "20260101") }

        val summary = notices.toHomeSummary(TODAY)

        assertEquals(0, summary.todayNoticeTotalCount)
        assertEquals(TODAY_NOTICE_LIMIT, summary.recentNotices.size)
    }

    @Test
    fun `빈 목록에서도 터지지 않는다`() {
        val summary = emptyList<Notice>().toHomeSummary(TODAY)

        assertNull(summary.urgent)
        assertEquals(0, summary.openCount)
        assertEquals(0, summary.upcomingCount)
        assertEquals(0, summary.todayNoticeTotalCount)
        assertTrue(summary.recentNotices.isEmpty())
        assertNull(summary.nextOpenDate)
    }

    private companion object {
        const val TODAY = "20260803"

        fun notice(
            pblancId: String = "1",
            signgu: String? = null,
            noticeDate: String? = null,
            begin: String? = null,
            end: String? = null,
        ) = Notice(
            pblancId = pblancId,
            houseSn = 0,
            title = "공고 $pblancId",
            statusName = "일반공고",
            supplyInstitutionName = "LH",
            houseTypeName = null,
            supplyTypeName = "매입임대",
            previousNoticeId = null,
            complexName = null,
            address = NoticeAddress(
                provinceName = "울산광역시",
                districtName = signgu,
                fullAddress = null,
                roadName = null,
                legalDongName = null,
                pnu = null,
            ),
            period = NoticePeriod(
                noticeDate = noticeDate,
                beginDate = begin,
                endDate = end,
                winnerAnnounceDate = null,
            ),
            price = NoticePrice(null, null, null, null, null),
            heatingMethodName = null,
            totalHouseholdCount = null,
            supplyCount = null,
            supplyHouseCount = null,
            contact = null,
            noticeUrl = null,
            pcUrl = null,
            mobileUrl = null,
        )

        fun openNotice(pblancId: String = "1", end: String) =
            notice(pblancId = pblancId, begin = "20260701", end = end)

        fun upcomingNotice(pblancId: String = "1", begin: String) =
            notice(pblancId = pblancId, begin = begin, end = "20261231")
    }
}
