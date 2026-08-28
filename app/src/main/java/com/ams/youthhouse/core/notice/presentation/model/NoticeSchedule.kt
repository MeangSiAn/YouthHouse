package com.ams.youthhouse.core.notice.presentation.model

import com.ams.youthhouse.core.common.format.formatDateRange
import com.ams.youthhouse.core.common.format.formatYearMonthDay
import com.ams.youthhouse.core.notice.domain.model.Notice
import kotlinx.serialization.Serializable

/** 일정 단계의 진행 상태. 오늘을 기준으로 판정한다. */
@Serializable
enum class ScheduleStageState { DONE, CURRENT, UPCOMING }

/** 어떤 단계인지. 문구는 presentation이 리소스로 붙인다. */
@Serializable
enum class ScheduleStageKind { ANNOUNCED, APPLY, RESULT }

@Serializable
data class NoticeScheduleStage(
    val kind: ScheduleStageKind,
    val state: ScheduleStageState,
    /** "2026.08.25 ~ 2026.08.27" 처럼 포맷이 끝난 날짜. */
    val dateText: String,
)

/**
 * 기획서 SCREEN 05의 일정 타임라인.
 *
 * 기획서는 4단계(인터넷 접수 / 서류 제출 / 당첨자 발표 / 계약 체결)지만
 * **서류 제출과 계약 체결은 이 API가 주지 않는다.** 없는 단계를 빈칸으로 그리면
 * 앱이 모르는 것을 아는 척하게 되므로, 실제로 아는 3단계만 만든다.
 * 나머지 일정은 "공고 원문"에서 확인하도록 안내한다.
 *
 * 네 날짜 필드 모두 실측 채움율 100%지만, 값이 비는 경우 그 단계는 제외한다.
 */
fun Notice.toScheduleStages(today: String): List<NoticeScheduleStage> {
    val stages = mutableListOf<NoticeScheduleStage>()

    period.noticeDate?.let { noticeDate ->
        stages += NoticeScheduleStage(
            kind = ScheduleStageKind.ANNOUNCED,
            state = if (today >= noticeDate) ScheduleStageState.DONE else ScheduleStageState.UPCOMING,
            dateText = noticeDate.formatYearMonthDay().orEmpty(),
        )
    }

    val begin = period.beginDate
    val end = period.endDate
    if (begin != null || end != null) {
        stages += NoticeScheduleStage(
            kind = ScheduleStageKind.APPLY,
            state = when {
                begin != null && today < begin -> ScheduleStageState.UPCOMING
                end != null && today > end -> ScheduleStageState.DONE
                else -> ScheduleStageState.CURRENT
            },
            dateText = formatDateRange(begin, end).orEmpty(),
        )
    }

    period.winnerAnnounceDate?.let { winnerDate ->
        stages += NoticeScheduleStage(
            kind = ScheduleStageKind.RESULT,
            state = when {
                today > winnerDate -> ScheduleStageState.DONE
                today == winnerDate -> ScheduleStageState.CURRENT
                else -> ScheduleStageState.UPCOMING
            },
            dateText = winnerDate.formatYearMonthDay().orEmpty(),
        )
    }

    return stages
}
