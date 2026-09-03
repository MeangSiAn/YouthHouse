package com.ams.youthhouse.feature.trade.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 자체 백엔드(data.mosstis.com) 응답. 실호출로 확인한 스키마 그대로다(2026-09).
 *
 * 금액 필드는 실거래가 API 관례대로 **만원 단위** 정수다.
 * 모든 필드에 기본값을 둔다 — 백엔드 배포로 필드가 빠져도 파싱이 죽지 않게.
 */
@Serializable
data class ComplexSearchResponseDto(
    val query: String = "",
    val results: List<ComplexSummaryDto> = emptyList(),
)

@Serializable
data class ComplexSummaryDto(
    @SerialName("kapt_code") val kaptCode: String = "",
    val name: String = "",
    val sido: String? = null,
    val sigungu: String? = null,
    val eupmyeondong: String? = null,
    @SerialName("sigungu_code") val sigunguCode: String? = null,
    @SerialName("bjd_code") val bjdCode: String? = null,
)

@Serializable
data class ComplexDetailResponseDto(
    val complex: ComplexSummaryDto = ComplexSummaryDto(),
    val info: ComplexInfoDto? = null,
    val deals: DealsBlockDto? = null,
)

/**
 * K-apt 기본 정보 65필드 중 화면이 쓰는 것만 남긴다. 나머지는 ignoreUnknownKeys가 버린다.
 *
 * **타입이 필드마다 제각각이다.** 세대수는 실수(`671.0`), 동수·주차대수는 문자열(`"8"`),
 * 승강기·충전기는 정수로 온다. 원문 타입 그대로 받고 도메인 변환에서 정수로 맞춘다.
 */
@Serializable
data class ComplexInfoDto(
    val kaptName: String? = null,
    val kaptAddr: String? = null,
    val doroJuso: String? = null,
    /** `YYYYMMDD` 사용승인일 */
    val kaptUsedate: String? = null,
    val kaptdaCnt: Double? = null,
    val kaptDongCnt: String? = null,
    val kaptTopFloor: Int? = null,
    val codeHeatNm: String? = null,
    val codeAptNm: String? = null,
    val codeHallNm: String? = null,
    val codeStr: String? = null,
    val codeMgrNm: String? = null,
    val kaptBcompany: String? = null,
    val kaptAcompany: String? = null,
    val kaptdSecCom: String? = null,
    val kaptdEcnt: Int? = null,
    val kaptdPcnt: String? = null,
    val kaptdPcntu: String? = null,
    val kaptdCccnt: String? = null,
    /** 전용 60㎡ 이하 세대수. 네 구간 모두 실수로 온다. */
    val kaptMparea60: Double? = null,
    val kaptMparea85: Double? = null,
    val kaptMparea135: Double? = null,
    val kaptMparea136: Double? = null,
    val kaptdWtimebus: String? = null,
    val subwayLine: String? = null,
    val subwayStation: String? = null,
    val kaptdWtimesub: String? = null,
    val convenientFacility: String? = null,
    val educationFacility: String? = null,
    val welfareFacility: String? = null,
    val groundElChargerCnt: Int? = null,
    val undergroundElChargerCnt: Int? = null,
)

@Serializable
data class DealsBlockDto(
    val type: String? = null,
    val months: Int? = null,
    val areas: List<Double> = emptyList(),
    @SerialName("by_area") val byArea: Map<String, AreaTrendDto> = emptyMap(),
    val note: String? = null,
)

@Serializable
data class AreaTrendDto(
    val latest: DealDto? = null,
    val change: Long? = null,
    val count: Int = 0,
    val trend: List<TrendPointDto> = emptyList(),
    val deals: List<DealDto> = emptyList(),
)

@Serializable
data class TrendPointDto(
    val ym: String = "",
    val avg: Long = 0,
    val count: Int = 0,
)

@Serializable
data class DealDto(
    val date: String? = null,
    val floor: Int? = null,
    val amount: Long = 0,
    @SerialName("monthly_rent") val monthlyRent: Long = 0,
)
