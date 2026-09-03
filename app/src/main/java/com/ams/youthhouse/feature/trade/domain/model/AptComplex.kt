package com.ams.youthhouse.feature.trade.domain.model

/**
 * 단지 검색 결과 한 건. K-apt 단지 목록(자체 백엔드 적재본)에서 온다.
 *
 * [kaptCode]가 K-apt 전역 고유 코드라, 공고와 달리 식별자 재조회가 성립한다.
 * 그래서 찜(관심 단지)도 스냅숏이 아니라 코드 + 표시용 필드만 저장한다.
 */
data class AptComplex(
    val kaptCode: String,
    val name: String,
    /** "서울특별시 관악구 봉천동" 형태의 표시용 지역 문자열. */
    val regionLabel: String,
)

/** 단지 기본정보 + 평형별 실거래. 백엔드가 한 호출로 합쳐 준다. */
data class ComplexDetail(
    val kaptCode: String,
    val name: String,
    val address: String?,
    /** `YYYYMMDD` 사용승인일. 표시용 연도는 presentation이 자른다. */
    val useApprovalDate: String?,
    val householdCount: Int?,
    val dongCount: String?,
    val heatingName: String?,
    val constructorName: String?,
    /** 전용면적(㎡) 목록. 거래가 있었던 평형만 온다. 큰 면적부터 정렬돼 있지 않다. */
    val areas: List<Double>,
    val trendsByArea: Map<Double, AreaTrend>,
    /** 거래가 없거나 단지명 매칭이 실패했을 때 백엔드가 주는 안내. */
    val dealsNote: String?,
)

/** 한 평형의 실거래 요약. 금액은 전부 **만원 단위**다. */
data class AreaTrend(
    val latestAmount: Long?,
    val latestDate: String?,
    val latestFloor: Int?,
    /** 직전 거래 대비 증감(만원). 비교 대상이 없으면 `null`. */
    val change: Long?,
    val dealCount: Int,
    /** 월별 평균가. 과거 → 최근 순. */
    val monthlyAverages: List<TrendPoint>,
    /** 개별 거래. 최근 순. */
    val deals: List<ComplexDeal>,
)

data class TrendPoint(
    /** `YYYYMM` */
    val yearMonth: String,
    val averageAmount: Long,
)

data class ComplexDeal(
    /** `YYYY-MM-DD` */
    val date: String?,
    val floor: Int?,
    val amount: Long,
)

/** 관심 단지 한 건. */
data class FavoriteComplex(
    val kaptCode: String,
    val name: String,
    val regionLabel: String,
)
