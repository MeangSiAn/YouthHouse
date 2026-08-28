package com.ams.youthhouse.core.notice.data.api

import com.ams.youthhouse.core.notice.data.dto.NoticeListResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 국토교통부_마이홈포털 공공주택 모집공고 조회.
 *
 * `serviceKey`는 선언하지 않는다 — `ServiceKeyInterceptor`가 모든 요청에 붙인다.
 * 응답 기본 포맷이 JSON이므로 `type=json` 같은 파라미터도 넣지 않는다.
 *
 * 두 오퍼레이션은 응답 스키마·0건 처리·페이지네이션 규약이 모두 같아
 * [NoticeListResponseDto] 하나를 공유한다.
 */
interface NoticeApi {

    /** 공공**임대** 모집공고. */
    @GET("1613000/HWSPR02/rsdtRcritNtcList")
    suspend fun getRentalNoticeList(
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        /** 광역시도 코드. `null`이면 OkHttp가 파라미터 자체를 생략해 전체 조회가 된다. */
        @Query("brtcCode") brtcCode: String? = null,
    ): NoticeListResponseDto

    /**
     * 공공**분양** 모집공고.
     *
     * 응답 필드가 임대의 부분집합이라(임대보증금·월임대료·공급유형명 없음)
     * DTO는 그대로 쓰고 없는 키는 기본값으로 흡수한다.
     */
    @GET("1613000/HWSPR02/ltRsdtRcritNtcList")
    suspend fun getSaleNoticeList(
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("brtcCode") brtcCode: String? = null,
    ): NoticeListResponseDto
}
