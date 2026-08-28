package com.ams.youthhouse.core.notice.data.mapper

import com.ams.youthhouse.core.notice.data.dto.NoticeItemDto
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeAddress
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticePeriod
import com.ams.youthhouse.core.notice.domain.model.NoticePrice

/** API는 값 없음을 `null`이 아니라 빈 문자열로 준다. */
private fun String.orNull(): String? = takeIf { it.isNotBlank() }

/**
 * API는 금액·세대수의 값 없음을 `0`으로 준다.
 * "0원"과 "미기재"를 구분할 방법이 없어 0을 값 없음으로 본다.
 */
private fun Int.positiveOrNull(): Int? = takeIf { it > 0 }

internal fun NoticeItemDto.toDomain(category: NoticeCategory): Notice = Notice(
    category = category,
    pblancId = pblancId,
    houseSn = houseSn,
    title = pblancNm,
    statusName = sttusNm.orNull(),
    supplyInstitutionName = suplyInsttNm.orNull(),
    houseTypeName = houseTyNm.orNull(),
    supplyTypeName = suplyTyNm.orNull(),
    previousNoticeId = beforePblancId.orNull(),
    complexName = hsmpNm.orNull(),
    address = NoticeAddress(
        provinceName = brtcNm.orNull(),
        districtName = signguNm.orNull(),
        fullAddress = fullAdres.orNull(),
        roadName = rnCodeNm.orNull(),
        legalDongName = refrnLegaldongNm.orNull(),
        pnu = pnu.orNull(),
    ),
    period = NoticePeriod(
        noticeDate = rcritPblancDe.orNull(),
        beginDate = beginDe.orNull(),
        endDate = endDe.orNull(),
        winnerAnnounceDate = przwnerPresnatnDe.orNull(),
    ),
    price = NoticePrice(
        minDeposit = rentGtn.positiveOrNull(),
        minDownPayment = enty.positiveOrNull(),
        minInterimPayment = prtpay.positiveOrNull(),
        minBalance = surlus.positiveOrNull(),
        minMonthlyRent = mtRntchrg.positiveOrNull(),
    ),
    heatingMethodName = heatMthdNm.orNull(),
    totalHouseholdCount = totHshldCo.positiveOrNull(),
    supplyCount = sumSuplyCo.positiveOrNull(),
    supplyHouseCount = suplyHoCo.orNull(),
    contact = refrnc.orNull(),
    noticeUrl = url.orNull(),
    pcUrl = pcUrl.orNull(),
    mobileUrl = mobileUrl.orNull(),
)
