package com.ams.youthhouse.core.notice.domain.model

import kotlinx.serialization.Serializable

/**
 * 공공임대주택 모집공고 한 건.
 *
 * ## 식별
 * 같은 공고(`pblancId`)가 시군구·단지별로 여러 행으로 쪼개져 내려온다. 원본 API에는
 * 그 행들을 구분할 키가 없었지만, 자체 백엔드가 행 내용으로 [noticeId]를 만들어 준다.
 * 상세 재조회와 찜이 이 값을 쓴다.
 *
 * 내용 해시라서 **모든 필드가 똑같은 행끼리는 같은 값을 갖는다**(실측: 400행 중 1쌍).
 * 화면에서도 구분되지 않는 행들이라 찜이 함께 켜져도 모순은 아니지만,
 * Compose 목록 key로는 쓸 수 없다 — 중복 키는 즉시 크래시다.
 *
 * ## 날짜
 * `YYYYMMDD` 원문을 유지한다. 사전순 비교가 곧 시간순 비교라
 * 기간 판정이 문자열만으로 정확하고, `java.time` desugaring이 필요 없다.
 * 백엔드는 `YYYY-MM-DD`로 주므로 매퍼가 하이픈을 지운다.
 *
 * ## 빈 값
 * 매퍼가 빈 문자열과 0을 `null`로 정규화한다.
 *
 * ## [Serializable]인 이유
 * 찜은 모델 스냅숏 자체를 저장한다. 마감된 공고는 백엔드 목록에서 사라지므로
 * 재조회로는 복원할 수 없고, 저장해 둔 스냅숏만이 그 공고를 다시 그릴 수 있다.
 * kotlinx.serialization은 순수 Kotlin이라 domain 순수성 규칙과 충돌하지 않는다.
 */
@Serializable
data class Notice(
    /**
     * 백엔드가 행 내용으로 만든 해시.
     *
     * 기본값을 두는 이유: 이 필드가 생기기 전에 저장된 찜 스냅숏(JSON)에는 키가 없어,
     * 기본값이 없으면 역직렬화가 통째로 실패한다.
     */
    val noticeId: String = "",
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

/**
 * 목록으로 받아 둔 공고에 상세 응답의 값을 덮어쓴다.
 *
 * 상세에만 있는 필드(주소·문의처·난방방식·잔금)를 채우는 것이 목적이라,
 * 상세가 비워 보낸 필드는 목록에서 온 값을 그대로 둔다.
 */
fun Notice.mergeDetail(detail: Notice): Notice = copy(
    statusName = detail.statusName ?: statusName,
    supplyInstitutionName = detail.supplyInstitutionName ?: supplyInstitutionName,
    houseTypeName = detail.houseTypeName ?: houseTypeName,
    supplyTypeName = detail.supplyTypeName ?: supplyTypeName,
    complexName = detail.complexName ?: complexName,
    address = NoticeAddress(
        provinceName = detail.address.provinceName ?: address.provinceName,
        districtName = detail.address.districtName ?: address.districtName,
        fullAddress = detail.address.fullAddress ?: address.fullAddress,
        roadName = detail.address.roadName ?: address.roadName,
        legalDongName = detail.address.legalDongName ?: address.legalDongName,
        pnu = detail.address.pnu ?: address.pnu,
    ),
    price = NoticePrice(
        minDeposit = detail.price.minDeposit ?: price.minDeposit,
        minDownPayment = detail.price.minDownPayment ?: price.minDownPayment,
        minInterimPayment = detail.price.minInterimPayment ?: price.minInterimPayment,
        minBalance = detail.price.minBalance ?: price.minBalance,
        minMonthlyRent = detail.price.minMonthlyRent ?: price.minMonthlyRent,
    ),
    heatingMethodName = detail.heatingMethodName ?: heatingMethodName,
    supplyCount = detail.supplyCount ?: supplyCount,
    contact = detail.contact ?: contact,
    noticeUrl = detail.noticeUrl ?: noticeUrl,
    pcUrl = detail.pcUrl ?: pcUrl,
)
