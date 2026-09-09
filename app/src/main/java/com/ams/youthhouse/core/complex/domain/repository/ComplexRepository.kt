package com.ams.youthhouse.core.complex.domain.repository

import com.ams.youthhouse.core.complex.domain.model.AptComplex
import com.ams.youthhouse.core.complex.domain.model.ComplexDetail

interface ComplexRepository {

    /** 단지명 자동완성. 백엔드가 2자 미만을 거부하므로 호출부가 길이를 보장한다. */
    suspend fun search(query: String): List<AptComplex>

    /** 기본정보 + 최근 [months]개월 평형별 실거래를 한 번에 가져온다. */
    suspend fun getDetail(kaptCode: String, months: Int): ComplexDetail
}
