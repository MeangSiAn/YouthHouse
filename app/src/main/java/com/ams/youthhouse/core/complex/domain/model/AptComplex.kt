package com.ams.youthhouse.core.complex.domain.model

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

/**
 * 단지 기본정보 + 평형별 실거래. 백엔드가 K-apt와 실거래가 API를 한 호출로 합쳐 준다.
 *
 * K-apt는 65개 필드를 주는데, 그중 "집을 고를 때 실제로 보는 것"만 골라 담았다.
 * 관리비 회계 코드처럼 앱에서 판단에 못 쓰는 값은 의도적으로 뺐다.
 */
data class ComplexDetail(
    val kaptCode: String,
    val name: String,
    val address: String?,
    /** `YYYYMMDD` 사용승인일. 표시용 연도는 presentation이 자른다. */
    val useApprovalDate: String?,
    val householdCount: Int?,
    val dongCount: String?,
    val topFloor: Int?,
    val heatingName: String?,
    val building: BuildingInfo,
    /** 전용면적 구간별 세대 구성. 세대가 0인 구간은 빠져 있다. */
    val areaComposition: List<AreaBucket>,
    val transit: TransitInfo,
    val surroundings: Surroundings,
    /** 전용면적(㎡) 목록. 거래가 있었던 평형만 온다. */
    val areas: List<Double>,
    val trendsByArea: Map<Double, AreaTrend>,
    /** 거래가 없거나 단지명 매칭이 실패했을 때 백엔드가 주는 안내. */
    val dealsNote: String?,
)

data class BuildingInfo(
    val houseTypeName: String?,
    /** 계단식/복도식/혼합식. 같은 평수라도 체감 면적과 채광이 달라진다. */
    val hallTypeName: String?,
    val structureName: String?,
    val builderName: String?,
    val developerName: String?,
    val managementName: String?,
    val securityCompany: String?,
    val elevatorCount: Int?,
    val parkingGround: Int?,
    val parkingUnderground: Int?,
    val cctvCount: Int?,
    val evChargerGround: Int?,
    val evChargerUnderground: Int?,
) {
    val totalParking: Int?
        get() = if (parkingGround == null && parkingUnderground == null) {
            null
        } else {
            parkingGround.orZero() + parkingUnderground.orZero()
        }

    val totalEvCharger: Int?
        get() = if (evChargerGround == null && evChargerUnderground == null) {
            null
        } else {
            evChargerGround.orZero() + evChargerUnderground.orZero()
        }

    private fun Int?.orZero(): Int = this ?: 0
}

/** K-apt가 주는 전용면적 구간. 값 자체가 이 네 칸으로 고정돼 있다. */
enum class AreaBucketKind { UNDER_60, FROM_60_TO_85, FROM_85_TO_135, OVER_135 }

data class AreaBucket(
    val kind: AreaBucketKind,
    val householdCount: Int,
)

data class TransitInfo(
    val subwayLine: String?,
    val subwayStation: String?,
    /** "15~20분이내" 형태의 원문. 구간 표기라 숫자로 파싱하지 않는다. */
    val subwayWalkTime: String?,
    val busWalkTime: String?,
) {
    val hasAny: Boolean
        get() = listOf(subwayLine, subwayStation, subwayWalkTime, busWalkTime)
            .any { !it.isNullOrBlank() }
}

/**
 * 주변 시설 한 분류와 그 이름들. `초등학교 → [치동초교]`, `대형상가 → [LG하이프라자, 하이마트]`.
 * 이름이 하나도 없는 분류는 만들지 않는다.
 */
data class FacilityGroup(
    val category: String,
    val names: List<String>,
)

/** 주변·단지 내 시설. 원문이 한 줄 문자열이라 매퍼가 분류·항목으로 쪼개 둔다. */
data class Surroundings(
    val convenient: List<FacilityGroup>,
    val education: List<FacilityGroup>,
    /** 단지 내 시설은 분류 없이 이름만 온다. */
    val welfare: List<String>,
) {
    val hasAny: Boolean
        get() = convenient.isNotEmpty() || education.isNotEmpty() || welfare.isNotEmpty()
}

/** 한 평형의 실거래 요약. 금액은 전부 **만원 단위**다. */
data class AreaTrend(
    val latestAmount: Long?,
    val latestDate: String?,
    val latestFloor: Int?,
    /** 직전 거래 대비 증감(만원). 비교 대상이 없으면 `null`. */
    val change: Long?,
    val dealCount: Int,
    /** 월별 평균가와 거래건수. 과거 → 최근 순. */
    val monthlyAverages: List<TrendPoint>,
    /** 개별 거래. 최근 순. */
    val deals: List<ComplexDeal>,
)

data class TrendPoint(
    /** `YYYYMM` */
    val yearMonth: String,
    val averageAmount: Long,
    val dealCount: Int,
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

/**
 * 최근에 열어 본 단지. 필드는 [FavoriteComplex]와 같지만 뜻이 다르다 —
 * 이쪽은 사용자가 고른 것이 아니라 앱이 관찰한 흔적이라 지워도 잃을 것이 없다.
 */
data class RecentComplex(
    val kaptCode: String,
    val name: String,
    val regionLabel: String,
)
