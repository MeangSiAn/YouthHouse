package com.ams.youthhouse.feature.trade.domain

import com.ams.youthhouse.feature.trade.domain.model.ComplexSnapshot
import com.ams.youthhouse.feature.trade.domain.model.DefectStatus
import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.domain.model.VisitCriterion
import com.ams.youthhouse.feature.trade.domain.model.VisitRatings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitComparisonTest {

    @Test
    fun `점수가 가장 높은 단지가 강조된다`() {
        val notes = listOf(
            note("a", light = 3),
            note("b", light = 5),
            note("c", light = 4),
        )

        assertEquals(setOf("b"), notes.compareVisits().bestByCriterion[VisitCriterion.LIGHT])
    }

    @Test
    fun `동점이면 모두 강조된다`() {
        val notes = listOf(note("a", light = 4), note("b", light = 4), note("c", light = 2))

        assertEquals(setOf("a", "b"), notes.compareVisits().bestByCriterion[VisitCriterion.LIGHT])
    }

    @Test
    fun `값을 남긴 단지가 하나뿐이면 강조하지 않는다`() {
        // 혼자 4점을 받았다고 "가장 좋다"고 표시하면 비교가 아니라 착시다.
        val notes = listOf(note("a", light = 4), note("b", light = null))

        assertTrue(notes.compareVisits().bestByCriterion[VisitCriterion.LIGHT].orEmpty().isEmpty())
    }

    @Test
    fun `점수를 매기지 않은 단지는 비교에서 빠진다`() {
        val notes = listOf(note("a", light = 2), note("b", light = null), note("c", light = 3))

        assertEquals(setOf("c"), notes.compareVisits().bestByCriterion[VisitCriterion.LIGHT])
    }

    @Test
    fun `역 도보는 짧을수록 좋다`() {
        val notes = listOf(note("a", walk = 12), note("b", walk = 5), note("c", walk = 8))

        assertEquals(setOf("b"), notes.compareVisits().shortestWalk)
    }

    @Test
    fun `종합은 매긴 항목만의 평균으로 비교한다`() {
        // a는 채광 하나만 5점, b는 네 항목 모두 4점. 빈 항목을 0점으로 치면 b가 이기지만
        // 매긴 것만 평균 내면 a(5.0)가 b(4.0)보다 높다.
        val notes = listOf(
            note("a", light = 5),
            note("b", light = 4, noise = 4, parking = 4, management = 4),
        )

        assertEquals(setOf("a"), notes.compareVisits().bestOverall)
    }

    @Test
    fun `노트가 없으면 아무것도 강조하지 않는다`() {
        val comparison = emptyList<SiteVisitNote>().compareVisits()

        assertTrue(comparison.bestOverall.isEmpty())
        assertTrue(comparison.shortestWalk.isEmpty())
        assertTrue(comparison.bestByCriterion.values.all { it.isEmpty() })
    }

    private fun note(
        code: String,
        light: Int? = null,
        noise: Int? = null,
        parking: Int? = null,
        management: Int? = null,
        walk: Int? = null,
    ) = SiteVisitNote(
        kaptCode = code,
        complexName = "단지 $code",
        regionLabel = "",
        visitedOn = "20260720",
        viewedUnit = "",
        ratings = VisitRatings()
            .with(VisitCriterion.LIGHT, light)
            .with(VisitCriterion.NOISE, noise)
            .with(VisitCriterion.PARKING, parking)
            .with(VisitCriterion.MANAGEMENT, management),
        walkToStationMinutes = walk,
        defectStatus = DefectStatus.UNCHECKED,
        memo = "",
        snapshot = ComplexSnapshot.EMPTY,
        updatedAtMillis = 0L,
    )
}
