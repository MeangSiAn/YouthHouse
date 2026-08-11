package com.ams.myjeonse.core.notice.data.api

import com.ams.myjeonse.core.notice.data.dto.NoticeListResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NoticeApi {

    /**
     * 국토교통부_마이홈포털 공공주택 모집공고 조회.
     *
     * `serviceKey`는 선언하지 않는다 — `ServiceKeyInterceptor`가 모든 요청에 붙인다.
     * 응답 기본 포맷이 JSON이므로 `type=json` 같은 파라미터도 넣지 않는다.
     */
    @GET("1613000/HWSPR02/rsdtRcritNtcList")
    suspend fun getNoticeList(
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        /** 광역시도 코드. `null`이면 OkHttp가 파라미터 자체를 생략해 전체 조회가 된다. */
        @Query("brtcCode") brtcCode: String? = null,
    ): NoticeListResponseDto
}
