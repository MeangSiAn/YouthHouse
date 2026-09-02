package com.ams.youthhouse.feature.schedule.domain

import com.ams.youthhouse.core.common.time.daysBetween
import com.ams.youthhouse.core.notice.domain.model.Notice

/**
 * 찜 중에서 "다가오는 마감"에 올릴 것을 고른다.
 *
 * 기준: 오늘 접수중이면서 마감까지 [UPCOMING_WINDOW_DAYS]일 이내.
 * 마감이 가까운 순으로 정렬한다 — 이 구간의 존재 이유가 "지금 급한 것"이므로
 * 찜한 순서가 아니라 급한 순서가 맞다.
 *
 * @param today `YYYYMMDD`
 */
fun List<Notice>.filterUpcomingDeadlines(today: String): List<Notice> =
    filter { notice ->
        val daysLeft = daysBetween(today, notice.period.endDate)
        notice.isOpenOn(today) && daysLeft != null && daysLeft <= UPCOMING_WINDOW_DAYS
    }.sortedBy { it.period.endDate }

/** 기획서 SCREEN 03 "다가오는 마감 · D-7 이내" */
const val UPCOMING_WINDOW_DAYS = 7
