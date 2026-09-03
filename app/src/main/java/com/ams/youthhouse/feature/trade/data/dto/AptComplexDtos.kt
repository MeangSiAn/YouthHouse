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

/** K-apt 기본 정보 65필드 중 화면이 쓰는 것만 남긴다. 나머지는 ignoreUnknownKeys가 버린다. */
@Serializable
data class ComplexInfoDto(
    val kaptName: String? = null,
    val kaptAddr: String? = null,
    val doroJuso: String? = null,
    /** `YYYYMMDD` 사용승인일 */
    val kaptUsedate: String? = null,
    /** 세대수. API가 `671.0`처럼 실수로 준다. */
    val kaptdaCnt: Double? = null,
    /** 동수. API가 `"8"`처럼 문자열로 준다. */
    val kaptDongCnt: String? = null,
    val codeHeatNm: String? = null,
    val kaptBcompany: String? = null,
    val codeAptNm: String? = null,
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
