package com.ams.youthhouse.core.notice.data.api

import com.ams.youthhouse.core.notice.data.dto.NoticeItemDto
import com.ams.youthhouse.core.notice.data.dto.NoticeListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 자체 백엔드의 공공주택 모집공고 조회.
 *
 * 백엔드가 data.go.kr을 대신 호출해 정규화·중복정리·상태판정까지 마친 결과를 준다.
 * 덕분에 앱에는 공공데이터포털 인증키가 들어가지 않는다.
 *
 * 인증 헤더(`x-api-key`)는 `ApiKeyHeaderInterceptor`가 모든 요청에 붙인다.
 */
interface MosstisNoticeApi {

    /**
     * @param category "공공임대" / "공공분양". 서버가 한글 이름으로 받는다.
     * @param sido 광역시도 이름. `null`이면 Retrofit이 파라미터를 생략해 전체 조회가 된다.
     * @param status `open` / `upcoming` / `closed` / `all`
     */
    @GET("notice")
    suspend fun getNotices(
        @Query("category") category: String,
        @Query("sido") sido: String?,
        @Query("status") status: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): NoticeListResponseDto

    /** 목록에 없는 필드(주소·문의처·난방방식·잔금)를 채우기 위한 단건 조회. */
    @GET("notice/{noticeId}")
    suspend fun getNotice(@Path("noticeId") noticeId: String): NoticeItemDto
}
