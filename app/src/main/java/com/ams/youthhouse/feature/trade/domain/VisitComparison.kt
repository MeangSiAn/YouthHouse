package com.ams.youthhouse.feature.trade.domain

import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.domain.model.VisitCriterion

/**
 * 비교표에서 강조할 "가장 나은" 단지들.
 *
 * - 값을 남긴 단지가 둘 이상일 때만 강조한다. 혼자 값이 있는 항목은 비교가 아니다.
 * - 동점이면 모두 강조한다. 임의로 하나만 고르면 없는 차이를 만들어 낸다.
 */
data class VisitComparison(
    val bestByCriterion: Map<VisitCriterion, Set<String>>,
    /** 매긴 항목 평균이 가장 높은 단지. */
    val bestOverall: Set<String>,
    /** 역까지 도보가 가장 짧은 단지. */
    val shortestWalk: Set<String>,
) {
    fun isBest(criterion: VisitCriterion, kaptCode: String): Boolean =
        kaptCode in bestByCriterion[criterion].orEmpty()

    companion object {
        val EMPTY = VisitComparison(emptyMap(), emptySet(), emptySet())
    }
}

fun List<SiteVisitNote>.compareVisits(): VisitComparison = VisitComparison(
    bestByCriterion = VisitCriterion.entries.associateWith { criterion ->
        leaders(higherIsBetter = true) { it.ratings[criterion]?.toDouble() }
    },
    bestOverall = leaders(higherIsBetter = true) { it.ratings.average },
    shortestWalk = leaders(higherIsBetter = false) { it.walkToStationMinutes?.toDouble() },
)

private fun List<SiteVisitNote>.leaders(
    higherIsBetter: Boolean,
    selector: (SiteVisitNote) -> Double?,
): Set<String> {
    val scored = mapNotNull { note -> selector(note)?.let { note.kaptCode to it } }
    if (scored.size < MIN_COMPARABLE) return emptySet()

    val best = if (higherIsBetter) scored.maxOf { it.second } else scored.minOf { it.second }
    return scored.filter { it.second == best }.map { it.first }.toSet()
}

private const val MIN_COMPARABLE = 2
