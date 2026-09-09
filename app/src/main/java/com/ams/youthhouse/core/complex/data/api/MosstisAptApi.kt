package com.ams.youthhouse.core.complex.data.api

import com.ams.youthhouse.core.complex.data.dto.ComplexDetailResponseDto
import com.ams.youthhouse.core.complex.data.dto.ComplexSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MosstisAptApi {

    @GET("apt_complex/search")
    suspend fun searchComplexes(
        @Query("q") query: String,
        @Query("limit") limit: Int,
    ): ComplexSearchResponseDto

    /** 기본정보 + 최근 실거래를 한 번에. 백엔드가 공공 API 두 개를 합쳐 준다. */
    @GET("apt_complex/{kaptCode}/detail")
    suspend fun getComplexDetail(
        @Path("kaptCode") kaptCode: String,
        @Query("months") months: Int,
    ): ComplexDetailResponseDto
}
