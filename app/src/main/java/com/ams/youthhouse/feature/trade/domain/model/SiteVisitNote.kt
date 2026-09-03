package com.ams.youthhouse.feature.trade.domain.model

/**
 * 현장 점검 항목. 기획서가 네 개로 고정했다 — 현장에서 한 손으로 찍을 수 있는 수.
 * 선언 순서가 곧 화면 순서다.
 */
enum class VisitCriterion { LIGHT, NOISE, PARKING, MANAGEMENT }

/** 항목별 1~5점. 매기지 않은 항목은 [scores]에 없다. */
data class VisitRatings(
    val scores: Map<VisitCriterion, Int> = emptyMap(),
) {
    operator fun get(criterion: VisitCriterion): Int? = scores[criterion]

    /** `null`이면 그 항목을 지운다. 범위 밖 값은 안으로 접는다. */
    fun with(criterion: VisitCriterion, score: Int?): VisitRatings =
        if (score == null) {
            copy(scores = scores - criterion)
        } else {
            copy(scores = scores + (criterion to score.coerceIn(MIN_SCORE, MAX_SCORE)))
        }

    /** 매긴 항목만의 평균. 하나도 없으면 `null` — 빈 항목을 0점으로 치면 비교가 왜곡된다. */
    val average: Double?
        get() = scores.values.takeIf { it.isNotEmpty() }?.average()

    val isEmpty: Boolean
        get() = scores.isEmpty()

    companion object {
        const val MIN_SCORE = 1
        const val MAX_SCORE = 5
    }
}

/** 누수·곰팡이 확인 결과. "안 봤다"와 "없었다"는 다른 정보라 셋으로 둔다. */
enum class DefectStatus { UNCHECKED, NONE, FOUND }

/**
 * 엘리베이터 체감. 기획서 dev2.0 임장노트의 "엘리베이터" 줄이다.
 *
 * 대수는 K-apt가 알려 주므로 노트에는 **현장에서만 알 수 있는 것**만 남긴다 —
 * 대수가 같아도 출근 시간에 서는지 아닌지는 가 봐야 안다.
 * 점수 4항목에 넣지 않은 이유는 기획서가 척도를 넷으로 고정했기 때문이다.
 */
enum class ElevatorCondition { UNCHECKED, COMFORTABLE, CROWDED, NONE }

/**
 * 노트를 쓸 때 단지 상세에서 떠 온 값.
 *
 * 비교표는 네트워크 없이 이것만으로 그린다. 실거래는 "방문 당시" 값이라 지금과 다를 수
 * 있는데, 임장 기록으로는 그편이 맞다 — 그때 얼마였는지가 남는다.
 */
data class ComplexSnapshot(
    /** `YYYY` */
    val builtYear: String?,
    val householdCount: Int?,
    /** "2호선 · 서울대입구역 · 15~20분이내" 형태의 표시용 한 줄. */
    val subwayLabel: String?,
    /** 노트를 쓸 때 보고 있던 전용면적(㎡). */
    val referenceArea: Double?,
    /** 그 평형의 당시 최근 실거래가(만원). */
    val referenceAmount: Long?,
) {
    val isEmpty: Boolean
        get() = builtYear == null && householdCount == null && subwayLabel == null &&
            referenceArea == null && referenceAmount == null

    companion object {
        val EMPTY = ComplexSnapshot(null, null, null, null, null)
    }
}

/**
 * 임장노트 한 건. 단지당 하나다 — 두 번째 방문은 덮어쓴다.
 *
 * 단지별로 하나여야 비교표의 열이 곧 단지가 된다. 방문마다 쌓으면 "어느 방문을
 * 비교할 것인가"라는 질문이 생기는데, 현장 메모 앱에 그 질문은 과하다.
 */
data class SiteVisitNote(
    val kaptCode: String,
    val complexName: String,
    val regionLabel: String,
    /** `YYYYMMDD` */
    val visitedOn: String,
    /** "84㎡ · 12층 · 남향" 같은 자유 서술. */
    val viewedUnit: String,
    val ratings: VisitRatings,
    val walkToStationMinutes: Int?,
    val elevatorCondition: ElevatorCondition,
    val defectStatus: DefectStatus,
    val memo: String,
    val snapshot: ComplexSnapshot,
    val updatedAtMillis: Long,
)
