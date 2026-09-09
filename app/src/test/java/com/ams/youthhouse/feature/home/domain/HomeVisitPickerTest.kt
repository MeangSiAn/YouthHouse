package com.ams.youthhouse.feature.home.domain

import com.ams.youthhouse.core.complex.domain.model.ComplexSnapshot
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeVisitPickerTest {

    private val today = "20260904"

    @Test
    fun `예정이 먼저, 그다음 미완, 마지막에 완료 기록이다`() {
        // 저장소 순서(최근 수정순)로 완료 → 예정 → 미완이 와도 홈 순서는 바뀐다.
        val notes = listOf(
            note("done", visitedOn = "20260901", light = 4, memo = "좋음"),
            note("planned", visitedOn = "20260912"),
            note("incomplete", visitedOn = "20260830", light = 3, memo = ""),
        )

        assertEquals(
            listOf("planned", "incomplete", "done"),
            notes.pickForHome(today).map { it.kaptCode },
        )
    }

    @Test
    fun `예정이 여럿이면 가까운 날짜가 앞이다`() {
        val notes = listOf(
            note("far", visitedOn = "20260920"),
            note("near", visitedOn = "20260906"),
        )

        assertEquals(listOf("near", "far"), notes.pickForHome(today).map { it.kaptCode })
    }

    @Test
    fun `오늘 방문은 예정이 아니다`() {
        assertFalse(note("a", visitedOn = today).isPlannedOn(today))
        assertTrue(note("a", visitedOn = "20260905").isPlannedOn(today))
    }

    @Test
    fun `점수나 메모 중 하나라도 비면 미완이다`() {
        assertTrue(note("a", visitedOn = "20260901", light = 4, memo = "").isIncomplete)
        assertTrue(note("a", visitedOn = "20260901", light = null, memo = "메모").isIncomplete)
        assertFalse(note("a", visitedOn = "20260901", light = 4, memo = "메모").isIncomplete)
    }

    @Test
    fun `상한을 넘지 않는다`() {
        val notes = (1..5).map { note("n$it", visitedOn = "20260901", light = 4, memo = "m") }

        assertEquals(HOME_VISIT_LIMIT, notes.pickForHome(today).size)
    }

    private fun note(
        code: String,
        visitedOn: String,
        light: Int? = null,
        memo: String = "",
    ) = SiteVisitNote(
        kaptCode = code,
        complexName = "단지 $code",
        regionLabel = "",
        visitedOn = visitedOn,
        viewedUnit = "",
        ratings = VisitRatings().with(VisitCriterion.LIGHT, light),
        walkToStationMinutes = null,
        elevatorCondition = ElevatorCondition.UNCHECKED,
        defectStatus = DefectStatus.UNCHECKED,
        memo = memo,
        snapshot = ComplexSnapshot.EMPTY,
        updatedAtMillis = 0L,
    )
}
