package com.ams.myjeonse.core.notice.presentation.model

/**
 * `@Preview`용 고정 데이터.
 *
 * [NoticeUiModel]은 필드가 23개라 미리보기마다 전부 나열하면 화면 코드가 묻힌다.
 * 필요한 것만 이름으로 덮어쓴다.
 */
fun previewNoticeUiModel(
    title: String = "[울산권] 2026년 기존주택등 매입임대주택 입주자 모집 공고",
    status: NoticeStatus = NoticeStatus.URGENT,
    statusLabel: String? = "D-2",
    supplyTypeName: String? = "매입임대",
    regionName: String? = "울산광역시 중구",
    applyPeriod: String? = "2026.08.10 ~ 2026.08.11",
): NoticeUiModel = NoticeUiModel(
    title = title,
    status = status,
    statusLabel = statusLabel,
    statusName = "일반공고",
    supplyTypeName = supplyTypeName,
    houseTypeName = "다가구주택",
    supplyInstitutionName = "LH",
    complexName = null,
    regionName = regionName,
    fullAddress = null,
    noticeDate = "2026.07.09",
    applyPeriod = applyPeriod,
    winnerAnnounceDate = "2026.10.02",
    deposit = "12,000,000",
    monthlyRent = "150,000",
    downPayment = null,
    interimPayment = null,
    balance = null,
    totalHouseholdCount = null,
    supplyCount = "3",
    heatingMethodName = "개별난방",
    contact = "LH 콜센터 : 1600-1004",
    detailUrl = "https://m.myhome.go.kr",
)
