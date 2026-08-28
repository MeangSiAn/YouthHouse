package com.ams.youthhouse.core.notice.presentation.model

import com.ams.youthhouse.core.common.format.formatDateRange
import com.ams.youthhouse.core.common.format.formatThousands
import com.ams.youthhouse.core.common.format.formatYearMonthDay
import com.ams.youthhouse.core.common.time.daysBetween
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import kotlinx.serialization.Serializable

/** 카드에 찍히는 접수 상태. 기획서 `.stt`의 live/soon/urg/done에 대응한다. */
@Serializable
enum class NoticeStatus { OPEN, URGENT, UPCOMING, CLOSED, UNKNOWN }

/**
 * 화면에 그대로 그릴 수 있게 포맷을 끝낸 모델.
 *
 * 이 API에는 고유 키가 없어 상세 화면을 식별자로 다시 조회할 수 없다.
 * 그래서 이 모델 자체를 Navigation 인자로 실어 보낸다([Serializable]인 이유).
 * 그 덕분에 프로세스 사망 후 복귀도 NavController의 백스택 복원만으로 해결된다.
 *
 * 단위(원, 세대 등)는 담지 않는다 — 지역화를 위해 Composable이 문자열 리소스로 붙인다.
 */
@Serializable
data class NoticeUiModel(
    val category: NoticeCategory,
    val title: String,
    val status: NoticeStatus,
    /** "D-2", "D-16 시작" 같은 짧은 상태 문구. 계산할 수 없으면 `null`. */
    val statusLabel: String?,
    val statusName: String?,
    val supplyTypeName: String?,
    val houseTypeName: String?,
    val supplyInstitutionName: String?,
    val complexName: String?,
    val regionName: String?,
    val fullAddress: String?,
    val noticeDate: String?,
    val applyPeriod: String?,
    val winnerAnnounceDate: String?,
    val deposit: String?,
    val monthlyRent: String?,
    val downPayment: String?,
    val interimPayment: String?,
    val balance: String?,
    val totalHouseholdCount: String?,
    val supplyCount: String?,
    val heatingMethodName: String?,
    val contact: String?,
    /** 마이홈포털 공고 상세(웹). 원문 확인용. */
    val detailUrl: String?,
    /** 실제 접수처(LH청약플러스 등). 기관이 직접 운영하는 신청 창구다. */
    val applyUrl: String?,
    val scheduleStages: List<NoticeScheduleStage>,
) {
    /** 카드 서브텍스트. 지역과 주택유형을 한 줄로 합친다. */
    val subtitle: String?
        get() = listOfNotNull(regionName, houseTypeName)
            .joinToString(separator = " · ")
            .takeIf { it.isNotBlank() }
}

/**
 * @param today `YYYYMMDD`. D-day는 "지금"이 있어야 계산되므로 호출부가 넘긴다.
 */
fun Notice.toUiModel(today: String): NoticeUiModel {
    val (status, statusLabel) = resolveStatus(today)

    return NoticeUiModel(
        category = category,
        title = title,
        status = status,
        statusLabel = statusLabel,
        statusName = statusName,
        supplyTypeName = supplyTypeName,
        houseTypeName = houseTypeName,
        supplyInstitutionName = supplyInstitutionName,
        complexName = complexName,
        regionName = listOfNotNull(address.provinceName, address.districtName)
            .joinToString(separator = " ")
            .takeIf { it.isNotBlank() },
        fullAddress = address.fullAddress,
        noticeDate = period.noticeDate.formatYearMonthDay(),
        applyPeriod = formatDateRange(period.beginDate, period.endDate),
        winnerAnnounceDate = period.winnerAnnounceDate.formatYearMonthDay(),
        deposit = price.minDeposit.formatThousands(),
        monthlyRent = price.minMonthlyRent.formatThousands(),
        downPayment = price.minDownPayment.formatThousands(),
        interimPayment = price.minInterimPayment.formatThousands(),
        balance = price.minBalance.formatThousands(),
        totalHouseholdCount = totalHouseholdCount.formatThousands(),
        supplyCount = supplyCount.formatThousands(),
        heatingMethodName = heatingMethodName,
        contact = contact,
        detailUrl = preferredUrl,
        applyUrl = noticeUrl,
        scheduleStages = toScheduleStages(today),
    )
}

/**
 * 기획서는 카드 상태를 D-day 중심으로 보여준다.
 * 날짜를 나열하는 것보다 "얼마나 급한지"가 먼저 읽히게 하기 위해서다.
 */
private fun Notice.resolveStatus(today: String): Pair<NoticeStatus, String?> {
    val begin = period.beginDate
    val end = period.endDate

    return when {
        begin != null && today < begin ->
            NoticeStatus.UPCOMING to daysBetween(today, begin)?.let { "D-$it 시작" }

        isOpenOn(today) -> {
            val daysLeft = daysBetween(today, end)
            val status = if (daysLeft != null && daysLeft <= URGENT_DAYS) {
                NoticeStatus.URGENT
            } else {
                NoticeStatus.OPEN
            }
            status to daysLeft?.let { "D-$it" }
        }

        end != null && today > end -> NoticeStatus.CLOSED to null

        else -> NoticeStatus.UNKNOWN to null
    }
}

private const val URGENT_DAYS = 3
