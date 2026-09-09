package com.ams.youthhouse.feature.home.presentation.model

import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.feature.home.domain.isIncomplete
import com.ams.youthhouse.feature.home.domain.isPlannedOn

/**
 * 홈 임장기록 카드 하나.
 *
 * 항목 넷 중 채광·주차만 싣는다 — 홈은 떠올리는 자리라 카드가 좁고, 집을 고를 때 먼저
 * 갈리는 두 가지가 그것이다. 나머지는 매매 탭 카드와 비교표의 몫이다.
 */
data class HomeVisitUiModel(
    val kaptCode: String,
    val complexName: String,
    val regionLabel: String,
    /** "07.27" */
    val visitedOnLabel: String,
    /** 방문일이 오늘 이후. */
    val isPlanned: Boolean,
    /** 점수나 메모가 비어 "마저 채우기"를 재촉할 기록. */
    val isIncomplete: Boolean,
    val lightScore: Int?,
    val parkingScore: Int?,
    val ratedCount: Int,
    val totalCriteria: Int,
    val viewedUnit: String,
)

fun SiteVisitNote.toHomeVisitUiModel(today: String): HomeVisitUiModel {
    val planned = isPlannedOn(today)
    return HomeVisitUiModel(
        kaptCode = kaptCode,
        complexName = complexName,
        regionLabel = regionLabel,
        visitedOnLabel = visitedOn.formatMonthDay(),
        isPlanned = planned,
        // 아직 안 간 곳은 비어 있는 게 당연하다. 미완은 다녀온 기록에만 붙인다.
        isIncomplete = !planned && isIncomplete,
        lightScore = ratings[VisitCriterion.LIGHT],
        parkingScore = ratings[VisitCriterion.PARKING],
        ratedCount = ratings.scores.size,
        totalCriteria = VisitCriterion.entries.size,
        viewedUnit = viewedUnit,
    )
}

/** `20260727` → `07.27`. 형식이 다르면 원문 그대로. */
private fun String.formatMonthDay(): String =
    if (length == YYYYMMDD_LENGTH && all(Char::isDigit)) {
        "${substring(4, 6)}.${substring(6, 8)}"
    } else {
        this
    }

private const val YYYYMMDD_LENGTH = 8
