package com.ams.youthhouse.core.notice.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 자체 백엔드 `GET /notice` 응답.
 *
 * 필드명은 서버 키를 그대로 쓴다(= 스펙의 거울). 의미 있는 이름으로의 변환은 매퍼가 한다.
 * 모든 필드에 기본값을 주어 서버가 필드를 빼도 역직렬화가 실패하지 않게 한다.
 *
 * data.go.kr을 직접 부르던 시절에는 같은 필드가 레코드마다 숫자/문자열/빈 문자열로 섞여 와
 * `LenientIntSerializer`가 필요했다. 백엔드가 정규화해 주므로 더는 필요 없다.
 */
@Serializable
data class NoticeListResponseDto(
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    val results: List<NoticeItemDto> = emptyList(),
)

/**
 * 공고 한 건.
 *
 * 목록(`/notice`)과 상세(`/notice/{id}`)가 같은 모양을 공유하되 상세에만 있는 필드가 있다
 * ([address]·[heatType]·[contact]·[balance]·[downPayment]).
 * 두 응답을 한 타입으로 받고, 목록에서는 그 필드들이 `null`로 남는다.
 *
 * 상세 응답의 `raw`(마이홈 원문 전체)는 정규화 필드와 내용이 겹쳐 받지 않는다.
 */
@Serializable
data class NoticeItemDto(
    /** 행마다 고유한 해시. 목록 키·상세 조회·찜 키가 모두 이 값을 쓴다. */
    @SerialName("notice_id") val noticeId: String = "",
    @SerialName("pblanc_id") val pblancId: String = "",
    val title: String = "",
    val supplier: String? = null,
    /** "공공임대" / "공공분양" */
    val category: String = "",
    @SerialName("supply_type") val supplyType: String? = null,
    @SerialName("house_type") val houseType: String? = null,
    @SerialName("complex_name") val complexName: String? = null,
    val sido: String? = null,
    val sigungu: String? = null,
    /** `YYYY-MM-DD`. 도메인은 `YYYYMMDD`를 쓰므로 매퍼가 하이픈을 지운다. */
    @SerialName("apply_start") val applyStart: String? = null,
    @SerialName("apply_end") val applyEnd: String? = null,
    @SerialName("announce_date") val announceDate: String? = null,
    @SerialName("winner_date") val winnerDate: String? = null,
    @SerialName("total_units") val totalUnits: Int? = null,
    /** 금액은 원 단위. 도메인이 Int라 매퍼에서 범위를 접는다. */
    val deposit: Long? = null,
    @SerialName("monthly_rent") val monthlyRent: Long? = null,
    /** "일반공고" / "정정공고" */
    @SerialName("notice_status") val noticeStatus: String? = null,
    /** 실제 접수처(LH청약플러스 등). */
    @SerialName("apply_url") val applyUrl: String? = null,
    /** 마이홈포털 공고 상세(웹). */
    @SerialName("detail_url") val detailUrl: String? = null,
    /**
     * 서버가 계산한 접수 상태(`open`/`upcoming`/`closed`)와 남은 일수.
     *
     * 도메인은 이 값을 쓰지 않는다 — 찜한 공고는 서버에 없어도 로컬 스냅숏으로 그려야 하고,
     * 그때 상태는 저장된 날짜와 "오늘"로 다시 계산해야 하기 때문이다.
     * 목록 필터링용 쿼리 파라미터로만 서버 판정을 쓴다.
     */
    val status: String? = null,
    @SerialName("days_left") val daysLeft: Int? = null,

    // 아래는 상세 응답에만 있다.
    val address: String? = null,
    @SerialName("heat_type") val heatType: String? = null,
    val contact: String? = null,
    val balance: Long? = null,
    @SerialName("down_payment") val downPayment: Long? = null,
)
