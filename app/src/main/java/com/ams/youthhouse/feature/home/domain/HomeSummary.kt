package com.ams.youthhouse.feature.home.domain

import com.ams.youthhouse.core.notice.domain.model.Notice

/**
 * 홈 화면이 그리는 모든 수치. 공고 스냅샷 한 덩어리에서 계산된다.
 */
data class HomeSummary(
    /** 마감이 가장 급한 공고. 임계일 이내가 아니면 `null`이고, 그러면 모듈 자체를 숨긴다. */
    val urgent: UrgentNotice?,
    /** 오늘 올라온 공고 (상한 적용). */
    val todayNotices: List<Notice>,
    /**
     * 날짜와 무관하게 최근 순으로 몇 건 (상한 적용).
     *
     * 지역을 정하지 않았을 때는 몇 건만 받아오므로 "오늘 올라온" 조건을 걸면
     * 대부분 0건이 된다. 그 화면에서는 둘러볼 거리를 남기기 위해 이쪽을 쓴다.
     */
    val recentNotices: List<Notice>,
    /** 상한을 적용하기 전 오늘 공고 전체 건수. 섹션 헤더에 "N건"으로 쓴다. */
    val todayNoticeTotalCount: Int,
    val openCount: Int,
    val upcomingCount: Int,
    /** 오늘 마감하는 건수. 기획서의 "마감" 칸을 대신한다. */
    val closingTodayCount: Int,
    /** 접수 예정 중 가장 이른 시작일 (`YYYYMMDD`). 접수중이 없을 때 다음 시기를 알린다. */
    val nextOpenDate: String?,
    /** 접수 예정 공고를 시작일이 이른 순으로 몇 건 (상한 적용). 접수중이 없을 때 "곧 열릴 공고"로 쓴다. */
    val upcomingNotices: List<Notice>,
) {
    val hasOpenNotice: Boolean get() = openCount > 0
}

data class UrgentNotice(
    val notice: Notice,
    /** 0이면 오늘 마감. */
    val daysLeft: Int,
)
