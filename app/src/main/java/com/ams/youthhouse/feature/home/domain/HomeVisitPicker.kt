package com.ams.youthhouse.feature.home.domain

import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote

/** 홈 임장기록 블록에 놓는 건수. 기획서 H-06은 예정 1 + 완료 2 구성이다. */
const val HOME_VISIT_LIMIT = 3

/**
 * 홈에 올릴 임장노트를 고른다 — 예정 → 미완 → 나머지 최근순.
 *
 * 기획서 NOTE-02: 할 일이 남은 기록을 완료된 기록보다 위로. 방문 예정은 아직 갈 곳이고,
 * 미완 기록은 "기억이 사라지기 전에" 채워야 할 곳이다. 둘 다 없으면 최근 것을 보여준다.
 *
 * 임장노트 모델은 trade 슬라이스의 도메인 타입이다. 홈은 여러 슬라이스의 요약을 모으는
 * 화면이라 도메인 모델까지는 참조한다(구현·화면은 참조하지 않는다).
 *
 * @param today `YYYYMMDD`
 */
fun List<SiteVisitNote>.pickForHome(
    today: String,
    limit: Int = HOME_VISIT_LIMIT,
): List<SiteVisitNote> {
    val (planned, done) = partition { it.isPlannedOn(today) }
    val (incomplete, complete) = done.partition { it.isIncomplete }
    // done 쪽은 저장소 순서(최근 수정순)를 그대로 지킨다.
    return (planned.sortedBy { it.visitedOn } + incomplete + complete).take(limit)
}

/** 방문일이 오늘 이후면 아직 가지 않은 곳이다. */
fun SiteVisitNote.isPlannedOn(today: String): Boolean = visitedOn > today

/** 점수도 메모도 없으면 "갔다"는 사실만 남은 기록이다. */
val SiteVisitNote.isIncomplete: Boolean
    get() = ratings.isEmpty || memo.isBlank()
