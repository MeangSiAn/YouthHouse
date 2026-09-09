package com.ams.youthhouse.core.notice.data.mapper

import com.ams.youthhouse.core.notice.data.dto.NoticeItemDto
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeAddress
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticePeriod
import com.ams.youthhouse.core.notice.domain.model.NoticePrice

/** 서버가 빈 문자열을 주는 경우가 남아 있어 한 번 더 거른다. */
private fun String?.orNull(): String? = this?.takeIf { it.isNotBlank() }

/**
 * `YYYY-MM-DD` → `YYYYMMDD`.
 *
 * 도메인은 날짜를 사전순으로 비교해 기간을 판정한다. 하이픈이 섞이면 그 비교가
 * 여전히 성립하긴 하지만, `daysBetween`이 8자리를 전제하므로 형식을 통일해야 한다.
 */
private fun String?.toCompactDate(): String? = orNull()?.replace("-", "")

/**
 * 금액·세대수의 값 없음은 `null` 또는 `0`으로 온다.
 * "0원"과 "미기재"를 구분할 방법이 없어 0을 값 없음으로 본다.
 *
 * 도메인이 `Int`라 범위를 접는다. 원 단위 금액이 21억을 넘길 일은 실무상 없지만,
 * 넘칠 때 음수로 뒤집히는 것보다는 상한에 붙는 편이 화면에서 덜 위험하다.
 */
private fun Long?.positiveIntOrNull(): Int? =
    this?.takeIf { it > 0 }?.coerceAtMost(Int.MAX_VALUE.toLong())?.toInt()

private fun Int?.positiveOrNull(): Int? = this?.takeIf { it > 0 }

/** 서버는 분야를 한글 이름으로 준다. 모르는 값은 임대로 접는다 — 목록이 통째로 죽는 것보다 낫다. */
private fun String.toCategory(): NoticeCategory = when (this) {
    CATEGORY_SALE -> NoticeCategory.SALE
    else -> NoticeCategory.RENTAL
}

internal fun NoticeItemDto.toDomain(): Notice = Notice(
    noticeId = noticeId,
    category = category.toCategory(),
    pblancId = pblancId,
    // 백엔드는 행 일련번호를 주지 않는다. 식별은 noticeId가 맡으므로 자리만 채운다.
    houseSn = 0,
    title = title,
    statusName = noticeStatus.orNull(),
    supplyInstitutionName = supplier.orNull(),
    houseTypeName = houseType.orNull(),
    supplyTypeName = supplyType.orNull(),
    // 정정공고의 원공고 번호는 백엔드가 정규화 필드로 노출하지 않는다.
    previousNoticeId = null,
    complexName = complexName.orNull(),
    address = NoticeAddress(
        provinceName = sido.orNull(),
        districtName = sigungu.orNull(),
        // 아래 셋은 상세 응답에만 있다. 목록에서는 비어 있는 게 정상이다.
        fullAddress = address.orNull(),
        roadName = null,
        legalDongName = null,
        pnu = null,
    ),
    period = NoticePeriod(
        noticeDate = announceDate.toCompactDate(),
        beginDate = applyStart.toCompactDate(),
        endDate = applyEnd.toCompactDate(),
        winnerAnnounceDate = winnerDate.toCompactDate(),
    ),
    price = NoticePrice(
        minDeposit = deposit.positiveIntOrNull(),
        minDownPayment = downPayment.positiveIntOrNull(),
        // 중도금은 백엔드 정규화 필드에 없다.
        minInterimPayment = null,
        minBalance = balance.positiveIntOrNull(),
        minMonthlyRent = monthlyRent.positiveIntOrNull(),
    ),
    heatingMethodName = heatType.orNull(),
    // 백엔드의 total_units는 이 공고의 공급 호수다(원문 sumSuplyCo).
    totalHouseholdCount = null,
    supplyCount = totalUnits.positiveOrNull(),
    supplyHouseCount = null,
    contact = contact.orNull(),
    // 원본 API의 `url`이 접수처, `pcUrl`이 마이홈 상세였다. 백엔드의 두 URL이 그대로 대응한다.
    // 이 대응을 지키면 `preferredUrl`(상세 보기)과 UI의 신청 버튼이 지금처럼 갈린다.
    noticeUrl = applyUrl.orNull(),
    pcUrl = detailUrl.orNull(),
    mobileUrl = null,
)

private const val CATEGORY_SALE = "공공분양"
