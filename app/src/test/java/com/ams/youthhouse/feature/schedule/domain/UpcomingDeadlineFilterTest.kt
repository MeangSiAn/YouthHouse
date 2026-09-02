package com.ams.youthhouse.feature.schedule.domain

import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeAddress
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticePeriod
import com.ams.youthhouse.core.notice.domain.model.NoticePrice
import org.junit.Assert.assertEquals
import org.junit.Test

class UpcomingDeadlineFilterTest {

    private val today = "20260903"

    @Test
    fun `접수중이고 마감 7일 이내면 포함된다`() {
        val notices = listOf(notice("a", begin = "20260901", end = "20260910"))

        val result = notices.filterUpcomingDeadlines(today)

        assertEquals(listOf("a"), result.map { it.pblancId })
    }

    @Test
    fun `마감일 당일도 포함된다`() {
        val notices = listOf(notice("a", begin = "20260901", end = today))

        assertEquals(1, notices.filterUpcomingDeadlines(today).size)
    }

    @Test
    fun `접수중이라도 마감이 8일 이상 남으면 제외된다`() {
        val notices = listOf(notice("a", begin = "20260901", end = "20260911"))

        assertEquals(0, notices.filterUpcomingDeadlines(today).size)
    }

    @Test
    fun `접수 시작 전이면 마감이 가까워도 제외된다`() {
        val notices = listOf(notice("a", begin = "20260905", end = "20260908"))

        assertEquals(0, notices.filterUpcomingDeadlines(today).size)
    }

    @Test
    fun `이미 마감된 공고는 제외된다`() {
        val notices = listOf(notice("a", begin = "20260801", end = "20260902"))

        assertEquals(0, notices.filterUpcomingDeadlines(today).size)
    }

    @Test
    fun `기간 정보가 없으면 제외된다`() {
        val notices = listOf(notice("a", begin = null, end = null))

        assertEquals(0, notices.filterUpcomingDeadlines(today).size)
    }

    @Test
    fun `마감이 가까운 순으로 정렬된다`() {
        val notices = listOf(
            notice("later", begin = "20260901", end = "20260909"),
            notice("sooner", begin = "20260901", end = "20260904"),
        )

        val result = notices.filterUpcomingDeadlines(today)

        assertEquals(listOf("sooner", "later"), result.map { it.pblancId })
    }

    private fun notice(pblancId: String, begin: String?, end: String?): Notice = Notice(
        category = NoticeCategory.RENTAL,
        pblancId = pblancId,
        houseSn = 0,
        title = "공고 $pblancId",
        statusName = null,
        supplyInstitutionName = null,
        houseTypeName = null,
        supplyTypeName = null,
        previousNoticeId = null,
        complexName = null,
        address = NoticeAddress(null, null, null, null, null, null),
        period = NoticePeriod(
            noticeDate = null,
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
}
