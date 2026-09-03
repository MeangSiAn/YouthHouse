package com.ams.youthhouse.feature.home.presentation.model

import com.ams.youthhouse.feature.home.domain.isIncomplete
import com.ams.youthhouse.feature.home.domain.isPlannedOn
import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.domain.model.VisitCriterion
import com.ams.youthhouse.feature.trade.domain.model.VisitRatings
import kotlin.math.roundToInt

/**
 * 홈 임장기록 한 줄. 항목별 점수는 싣지 않는다 — 홈은 "몇 곳 다녀왔고 어디가 괜찮았나"까지만
 * 보여주고, 채광이 몇 점인지는 노트와 비교표의 몫이다.
 */
data class HomeVisitUiModel(
    val kaptCode: String,
    val complexName: String,
    val regionLabel: String,
    /** "07.27" */
    val visitedOnLabel: String,
    /** 방문일이 오늘 이후. 카드로 크게 놓는다. */
    val isPlanned: Boolean,
    /** 점수나 메모가 비어 "마저 채우기"를 재촉할 기록. */
    val isIncomplete: Boolean,
    /** 매긴 항목 평균을 반올림한 별 개수. 매긴 게 없으면 `null`. */
    val stars: Int?,
    val ratedCount: Int,
    val totalCriteria: Int,
    val viewedUnit: String,
) {
    companion object {
        const val MAX_STARS = VisitRatings.MAX_SCORE
    }
}

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
        stars = ratings.average?.roundToInt(),
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
