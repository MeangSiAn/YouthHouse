package com.ams.youthhouse.feature.home.presentation.model

import com.ams.youthhouse.core.common.format.formatYearMonthDay
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.notice.presentation.model.toUiModel
import com.ams.youthhouse.feature.home.domain.HomeSummary

data class UrgentNoticeUiModel(
    val notice: NoticeUiModel,
    val daysLeft: Int,
    /** "2026.08.05" — 마감일. 문구는 Composable이 붙인다. */
    val deadlineDate: String?,
)

data class HomeSummaryUiModel(
    val urgent: UrgentNoticeUiModel?,
    val todayNotices: List<NoticeUiModel>,
    val todayNoticeTotalCount: Int,
    val recentNotices: List<NoticeUiModel>,
    val openCount: Int,
    val upcomingCount: Int,
    val closingTodayCount: Int,
    /** "2026.08.16" — 다음 접수 시작일. */
    val nextOpenDate: String?,
    val hasOpenNotice: Boolean,
)

fun HomeSummary.toUiModel(today: String): HomeSummaryUiModel = HomeSummaryUiModel(
    urgent = urgent?.let { urgentNotice ->
        UrgentNoticeUiModel(
            notice = urgentNotice.notice.toUiModel(today),
            daysLeft = urgentNotice.daysLeft,
            deadlineDate = urgentNotice.notice.period.endDate.formatYearMonthDay(),
        )
    },
    todayNotices = todayNotices.map { it.toUiModel(today) },
    todayNoticeTotalCount = todayNoticeTotalCount,
    recentNotices = recentNotices.map { it.toUiModel(today) },
    openCount = openCount,
    upcomingCount = upcomingCount,
    closingTodayCount = closingTodayCount,
    nextOpenDate = nextOpenDate.formatYearMonthDay(),
    hasOpenNotice = hasOpenNotice,
)
