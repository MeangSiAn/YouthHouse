package com.ams.youthhouse.core.notice.presentation.model

import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeAddress
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticePeriod
import com.ams.youthhouse.core.notice.domain.model.NoticePrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 상세 화면 타임라인의 단계 판정.
 *
 * "지금 어느 단계인가"가 틀리면 사용자가 접수 가능 여부를 잘못 읽는다.
 */
class NoticeScheduleTest {

    @Test
    fun `접수 전이면 접수 단계가 예정이다`() {
        val stages = notice(begin = "20260820", end = "20260822").toScheduleStages("20260814")

        assertEquals(ScheduleStageState.UPCOMING, stages.apply_().state)
    }

    @Test
    fun `접수 기간 안이면 현재 단계다`() {
        val stages = notice(begin = "20260810", end = "20260820").toScheduleStages("20260814")

        assertEquals(ScheduleStageState.CURRENT, stages.apply_().state)
    }

    /** 마감일 당일까지는 접수 중이다. 하루 일찍 닫으면 신청 기회를 뺏는다. */
    @Test
    fun `마감 당일도 현재 단계다`() {
        val stages = notice(begin = "20260810", end = "20260814").toScheduleStages("20260814")

        assertEquals(ScheduleStageState.CURRENT, stages.apply_().state)
    }

    @Test
    fun `마감이 지나면 완료 단계다`() {
        val stages = notice(begin = "20260801", end = "20260810").toScheduleStages("20260814")

        assertEquals(ScheduleStageState.DONE, stages.apply_().state)
    }

    @Test
    fun `공고일이 지났으면 모집공고 단계는 완료다`() {
        val stages = notice(noticeDate = "20260801").toScheduleStages("20260814")

        assertEquals(
            ScheduleStageState.DONE,
            stages.first { it.kind == ScheduleStageKind.ANNOUNCED }.state,
        )
    }

    @Test
    fun `당첨자 발표 전이면 예정이다`() {
        val stages = notice(winner = "20261208").toScheduleStages("20260814")

        assertEquals(
            ScheduleStageState.UPCOMING,
            stages.first { it.kind == ScheduleStageKind.RESULT }.state,
        )
    }

    @Test
    fun `날짜가 모두 있으면 3단계가 순서대로 나온다`() {
        val stages = notice(
            noticeDate = "20260801",
            begin = "20260810",
            end = "20260820",
            winner = "20261208",
        ).toScheduleStages("20260814")

        assertEquals(
            listOf(
                ScheduleStageKind.ANNOUNCED,
                ScheduleStageKind.APPLY,
                ScheduleStageKind.RESULT,
            ),
            stages.map { it.kind },
        )
    }

    /** 값이 없는 단계는 빈칸으로 그리지 않고 아예 만들지 않는다. */
    @Test
    fun `날짜가 없는 단계는 만들지 않는다`() {
        val stages = notice(noticeDate = null, begin = null, end = null, winner = null)
            .toScheduleStages("20260814")

        assertTrue(stages.isEmpty())
    }

    private fun List<NoticeScheduleStage>.apply_() =
        first { it.kind == ScheduleStageKind.APPLY }

    private fun notice(
        noticeDate: String? = "20260801",
        begin: String? = "20260810",
        end: String? = "20260820",
        winner: String? = "20261208",
    ) = Notice(
        category = NoticeCategory.RENTAL,
        pblancId = "1",
        houseSn = 0,
        title = "공고",
        statusName = null,
        supplyInstitutionName = "LH",
        houseTypeName = null,
        supplyTypeName = null,
        previousNoticeId = null,
        complexName = null,
        address = NoticeAddress(null, null, null, null, null, null),
        period = NoticePeriod(noticeDate, begin, end, winner),
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
