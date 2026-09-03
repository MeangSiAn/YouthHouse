package com.ams.youthhouse.feature.home.domain

import com.ams.youthhouse.core.common.time.daysBetween
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory

/** 마감 임박으로 볼 남은 일수. 기획서 H-02는 D-3 이내로 정의한다. */
const val URGENT_THRESHOLD_DAYS = 3

/**
 * 홈의 "오늘의 새 공고"는 최대 2건 (기획서 H-04).
 * 목록의 축소판이지 대체재가 아니므로, 나머지는 "전체 보기"로 넘긴다.
 */
const val TODAY_NOTICE_LIMIT = 2

/**
 * 공고 스냅샷에서 홈이 쓸 수치를 뽑는다.
 *
 * DI가 필요 없는 순수 함수라 JVM 테스트로 바로 검증된다.
 * 홈 전용 규칙(임계일, 건수 상한)을 담으므로 공유 계층인 `core/notice`가 아니라 여기 둔다.
 *
 * @param today `YYYYMMDD`
 */
fun List<Notice>.toHomeSummary(
    today: String,
    urgentThresholdDays: Int = URGENT_THRESHOLD_DAYS,
    todayNoticeLimit: Int = TODAY_NOTICE_LIMIT,
): HomeSummary {
    // 같은 공고가 시군구별로 쪼개져 여러 행으로 온다.
    // 걷어내지 않으면 "오늘의 새 공고 3건"이 같은 공고 3행이 된다.
    //
    // 키에 분야를 넣는 이유: 임대와 분양은 pblancId 시퀀스가 서로 독립이다.
    // 지금은 값이 겹치지 않지만(임대 18976~, 분양 1120~) 보장된 게 아니라,
    // 겹치는 순간 서로 다른 공고가 하나로 합쳐져 조용히 사라진다.
    val notices = distinctBy { it.category to it.pblancId }

    // 접수 상태는 statusName(sttusNm = "일반공고"/"정정공고", 공고 종류다)이 아니라
    // 기간으로 판정해야 한다.
    val open = notices.filter { it.isOpenOn(today) }
    val upcoming = notices.filter { notice ->
        val begin = notice.period.beginDate
        begin != null && today < begin
    }

    val urgent = open
        .mapNotNull { notice ->
            val daysLeft = daysBetween(today, notice.period.endDate) ?: return@mapNotNull null
            UrgentNotice(notice = notice, daysLeft = daysLeft)
        }
        .filter { it.daysLeft >= 0 }
        .minByOrNull { it.daysLeft }
        ?.takeIf { it.daysLeft <= urgentThresholdDays }

    val todayNotices = notices.filter { it.period.noticeDate == today }

    return HomeSummary(
        urgent = urgent,
        todayNotices = todayNotices.pickAcrossCategories(todayNoticeLimit),
        todayNoticeTotalCount = todayNotices.size,
        recentNotices = notices.pickAcrossCategories(todayNoticeLimit),
        openCount = open.size,
        upcomingCount = upcoming.size,
        closingTodayCount = open.count { it.period.endDate == today },
        nextOpenDate = upcoming.mapNotNull { it.period.beginDate }.minOrNull(),
        // 기획서 SCREEN 03 "곧 열릴 공고" — 접수중이 없는 달에 기다릴 거리를 남긴다.
        upcomingNotices = upcoming
            .sortedBy { it.period.beginDate.orEmpty() }
            .take(todayNoticeLimit),
    )
}

/**
 * 분야를 번갈아 가며 최신순으로 [limit]건을 고른다.
 *
 * 그냥 공고일 내림차순으로 자르면 **분양이 홈에 영원히 나오지 않는다.**
 * 임대가 분양보다 5배 많아 최신 날짜를 독점하기 때문이다(실측: 상위 3건이 전부 임대).
 * 홈은 "지금 뭐가 올라왔는지"를 보여주는 자리이므로 분야가 고루 보이는 편이 목적에 맞다.
 *
 * 한쪽 분야만 있으면 그 분야로만 채운다.
 */
private fun List<Notice>.pickAcrossCategories(limit: Int): List<Notice> {
    val queues = NoticeCategory.entries
        .map { category ->
            filter { it.category == category }
                .sortedByDescending { it.period.noticeDate.orEmpty() }
        }
        .filter { it.isNotEmpty() }

    if (queues.isEmpty()) return emptyList()

    val picked = mutableListOf<Notice>()
    var round = 0
    while (picked.size < limit && queues.any { it.size > round }) {
        for (queue in queues) {
            if (picked.size >= limit) break
            queue.getOrNull(round)?.let(picked::add)
        }
        round++
    }
    return picked
}
