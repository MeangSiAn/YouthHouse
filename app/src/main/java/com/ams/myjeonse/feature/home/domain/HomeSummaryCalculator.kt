package com.ams.myjeonse.feature.home.domain

import com.ams.myjeonse.core.common.time.daysBetween
import com.ams.myjeonse.core.notice.domain.model.Notice

/** 마감 임박으로 볼 남은 일수. 기획서 H-02는 D-3 이내로 정의한다. */
const val URGENT_THRESHOLD_DAYS = 3

/** 홈의 "오늘의 새 공고"는 최대 3건 (기획서 H-04). */
const val TODAY_NOTICE_LIMIT = 3

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
    val notices = distinctBy { it.pblancId }

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
        todayNotices = todayNotices.take(todayNoticeLimit),
        todayNoticeTotalCount = todayNotices.size,
        recentNotices = notices.take(todayNoticeLimit),
        openCount = open.size,
        upcomingCount = upcoming.size,
        closingTodayCount = open.count { it.period.endDate == today },
        nextOpenDate = upcoming.mapNotNull { it.period.beginDate }.minOrNull(),
    )
}
