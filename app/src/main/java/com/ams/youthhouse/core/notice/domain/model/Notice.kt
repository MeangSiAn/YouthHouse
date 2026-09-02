package com.ams.youthhouse.core.notice.domain.model

import kotlinx.serialization.Serializable

/**
 * 공공임대주택 모집공고 한 건.
 *
 * ## 이 API에는 고유 키가 없다
 * 같은 공고(`pblancId`)가 시군구별로 여러 행으로 쪼개져 내려오고
 * (`signguNm`·`sumSuplyCo`만 다름), 32개 필드가 완전히 동일한 행도 존재한다.
 * 따라서 `pblancId`도, `(pblancId, houseSn)` 조합도 목록 항목의 키가 될 수 없다.
 * 목록 UI는 위치 기반 키를 쓰고, 상세 화면은 모델 자체를 전달받는다.
 * 단, **공고 단위**로는 `(category, pblancId)`가 유효하다 — 찜이 이 키를 쓴다.
 *
 * ## 날짜
 * `YYYYMMDD` 원문을 유지한다. 사전순 비교가 곧 시간순 비교라
 * 기간 판정이 문자열만으로 정확하고, `java.time` desugaring이 필요 없다.
 *
 * ## 빈 값
 * API는 값 없음을 `null`이 아니라 빈 문자열/0으로 준다. 매퍼가 `null`로 정규화한다.
 *
 * ## [Serializable]인 이유
 * 이 API는 식별자로 재조회할 방법이 없어, 찜은 모델 스냅숏 자체를 저장한다.
 * kotlinx.serialization은 순수 Kotlin이라 domain 순수성 규칙과 충돌하지 않는다.
 */
@Serializable
data class Notice(
    val category: NoticeCategory,
    val pblancId: String,
    val houseSn: Int,
    val title: String,
    val statusName: String?,
    val supplyInstitutionName: String?,
    val houseTypeName: String?,
    val supplyTypeName: String?,
    val previousNoticeId: String?,
    val complexName: String?,
    val address: NoticeAddress,
    val period: NoticePeriod,
    val price: NoticePrice,
    val heatingMethodName: String?,
    val totalHouseholdCount: Int?,
    val supplyCount: Int?,
    val supplyHouseCount: String?,
    val contact: String?,
    val noticeUrl: String?,
    val pcUrl: String?,
    val mobileUrl: String?,
) {

    /** 원문을 열 때 쓸 링크. 모바일 포털 → PC 포털 → 원공고 순으로 고른다. */
    val preferredUrl: String?
        get() = mobileUrl ?: pcUrl ?: noticeUrl

    /**
     * [today]가 모집 기간 안인지 판정한다.
     *
     * @param today `YYYYMMDD` 형식
     */
    fun isOpenOn(today: String): Boolean {
        val begin = period.beginDate ?: return false
        val end = period.endDate ?: return false
        return today in begin..end
    }
}

@Serializable
data class NoticeAddress(
    val provinceName: String?,
    val districtName: String?,
    val fullAddress: String?,
    val roadName: String?,
    val legalDongName: String?,
    val pnu: String?,
)

@Serializable
data class NoticePeriod(
    val noticeDate: String?,
    val beginDate: String?,
    val endDate: String?,
    val winnerAnnounceDate: String?,
)

@Serializable
data class NoticePrice(
    val minDeposit: Int?,
    val minDownPayment: Int?,
    val minInterimPayment: Int?,
    val minBalance: Int?,
    val minMonthlyRent: Int?,
)
