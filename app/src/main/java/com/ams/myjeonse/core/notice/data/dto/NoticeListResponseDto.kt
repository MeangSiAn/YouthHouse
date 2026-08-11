package com.ams.myjeonse.core.notice.data.dto

import com.ams.myjeonse.core.network.serializer.LenientIntSerializer
import kotlinx.serialization.Serializable

/**
 * `rsdtRcritNtcList` 응답 DTO.
 *
 * 필드명은 서버 키를 그대로 쓴다(= 스펙의 거울). 의미 있는 이름으로의 변환은 매퍼가 한다.
 * 모든 필드에 기본값을 주어 서버가 필드를 빼도 역직렬화가 실패하지 않게 한다.
 */
@Serializable
data class NoticeListResponseDto(
    val response: NoticeResponseDto = NoticeResponseDto(),
)

@Serializable
data class NoticeResponseDto(
    val header: NoticeHeaderDto = NoticeHeaderDto(),
    /** 결과가 0건이면 `body` 키 자체가 내려오지 않는다. */
    val body: NoticeBodyDto? = null,
)

@Serializable
data class NoticeHeaderDto(
    val resultCode: String = "",
    val resultMsg: String = "",
)

/** 숫자로 보이는 값들이 문자열로 내려오므로(`"totalCount": "383"`) String으로 받는다. */
@Serializable
data class NoticeBodyDto(
    val totalCount: String = "",
    val numOfRows: String = "",
    val pageNo: String = "",
    /** 1건이어도 배열이고, 페이지 범위를 넘으면 빈 배열이다. */
    val item: List<NoticeItemDto> = emptyList(),
)

/**
 * 숫자 필드는 모두 [LenientIntSerializer]를 쓴다.
 * 이 API는 같은 필드를 레코드마다 숫자/문자열/빈 문자열로 섞어 보낸다
 * (실측: `totHshldCo`가 383건 중 82건에서 `""`).
 */
@Serializable
data class NoticeItemDto(
    val pblancId: String = "",
    @Serializable(with = LenientIntSerializer::class)
    val houseSn: Int = 0,
    val sttusNm: String = "",
    val pblancNm: String = "",
    val suplyInsttNm: String = "",
    val houseTyNm: String = "",
    val suplyTyNm: String = "",
    val beforePblancId: String = "",
    val rcritPblancDe: String = "",
    val przwnerPresnatnDe: String = "",
    val suplyHoCo: String = "",
    val refrnc: String = "",
    val url: String = "",
    val pcUrl: String = "",
    val mobileUrl: String = "",
    val hsmpNm: String = "",
    val brtcNm: String = "",
    val signguNm: String = "",
    val fullAdres: String = "",
    val rnCodeNm: String = "",
    val refrnLegaldongNm: String = "",
    val pnu: String = "",
    val heatMthdNm: String = "",
    @Serializable(with = LenientIntSerializer::class)
    val totHshldCo: Int = 0,
    @Serializable(with = LenientIntSerializer::class)
    val sumSuplyCo: Int = 0,
    @Serializable(with = LenientIntSerializer::class)
    val rentGtn: Int = 0,
    @Serializable(with = LenientIntSerializer::class)
    val enty: Int = 0,
    @Serializable(with = LenientIntSerializer::class)
    val prtpay: Int = 0,
    @Serializable(with = LenientIntSerializer::class)
    val surlus: Int = 0,
    @Serializable(with = LenientIntSerializer::class)
    val mtRntchrg: Int = 0,
    val beginDe: String = "",
    val endDe: String = "",
)
