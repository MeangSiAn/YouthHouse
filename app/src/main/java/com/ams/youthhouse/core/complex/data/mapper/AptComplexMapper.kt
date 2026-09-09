package com.ams.youthhouse.core.complex.data.mapper

import com.ams.youthhouse.core.complex.data.dto.AreaTrendDto
import com.ams.youthhouse.core.complex.data.dto.ComplexDetailResponseDto
import com.ams.youthhouse.core.complex.data.dto.ComplexInfoDto
import com.ams.youthhouse.core.complex.data.dto.ComplexSummaryDto
import com.ams.youthhouse.core.complex.domain.model.AptComplex
import com.ams.youthhouse.core.complex.domain.model.AreaBucket
import com.ams.youthhouse.core.complex.domain.model.AreaBucketKind
import com.ams.youthhouse.core.complex.domain.model.AreaTrend
import com.ams.youthhouse.core.complex.domain.model.BuildingInfo
import com.ams.youthhouse.core.complex.domain.model.ComplexDeal
import com.ams.youthhouse.core.complex.domain.model.ComplexDetail
import com.ams.youthhouse.core.complex.domain.model.Surroundings
import com.ams.youthhouse.core.complex.domain.model.TransitInfo
import com.ams.youthhouse.core.complex.domain.model.TrendPoint

fun ComplexSummaryDto.toDomain(): AptComplex = AptComplex(
    kaptCode = kaptCode,
    name = name,
    regionLabel = listOfNotNull(sido, sigungu, eupmyeondong)
        .filter { it.isNotBlank() }
        .joinToString(separator = " "),
)

fun ComplexDetailResponseDto.toDomain(): ComplexDetail {
    val trends = deals?.byArea.orEmpty()
        .mapNotNull { (areaText, trendDto) ->
            // by_area의 키는 전용면적을 문자열로 쓴 것("59.58"). 숫자가 아니면 버린다.
            areaText.toDoubleOrNull()?.let { area -> area to trendDto.toDomain() }
        }
        .toMap()

    return ComplexDetail(
        kaptCode = complex.kaptCode,
        name = info?.kaptName ?: complex.name,
        address = info?.doroJuso ?: info?.kaptAddr,
        useApprovalDate = info?.kaptUsedate,
        householdCount = info?.kaptdaCnt?.toInt(),
        dongCount = info?.kaptDongCnt,
        topFloor = info?.kaptTopFloor,
        heatingName = info?.codeHeatNm,
        building = info.toBuildingInfo(),
        areaComposition = info.toAreaComposition(),
        transit = info.toTransitInfo(),
        surroundings = info.toSurroundings(),
        // 세대가 많은 대표 평형이 먼저 보이도록 큰 면적부터. by_area에 실제로 있는 것만 남긴다.
        areas = deals?.areas.orEmpty().filter { it in trends }.sortedDescending(),
        trendsByArea = trends,
        dealsNote = deals?.note,
    )
}

private fun ComplexInfoDto?.toBuildingInfo(): BuildingInfo = BuildingInfo(
    houseTypeName = this?.codeAptNm,
    hallTypeName = this?.codeHallNm,
    structureName = this?.codeStr,
    builderName = this?.kaptBcompany,
    developerName = this?.kaptAcompany,
    managementName = this?.codeMgrNm,
    securityCompany = this?.kaptdSecCom,
    elevatorCount = this?.kaptdEcnt,
    parkingGround = this?.kaptdPcnt?.toIntOrNull(),
    parkingUnderground = this?.kaptdPcntu?.toIntOrNull(),
    cctvCount = this?.kaptdCccnt?.toIntOrNull(),
    evChargerGround = this?.groundElChargerCnt,
    evChargerUnderground = this?.undergroundElChargerCnt,
)

/** 세대가 0인 구간은 만들지 않는다 — "85~135㎡ 0세대"는 정보가 아니라 잡음이다. */
private fun ComplexInfoDto?.toAreaComposition(): List<AreaBucket> = listOfNotNull(
    AreaBucketKind.UNDER_60 bucketOf this?.kaptMparea60,
    AreaBucketKind.FROM_60_TO_85 bucketOf this?.kaptMparea85,
    AreaBucketKind.FROM_85_TO_135 bucketOf this?.kaptMparea135,
    AreaBucketKind.OVER_135 bucketOf this?.kaptMparea136,
)

private infix fun AreaBucketKind.bucketOf(count: Double?): AreaBucket? =
    count?.toInt()?.takeIf { it > 0 }?.let { AreaBucket(kind = this, householdCount = it) }

private fun ComplexInfoDto?.toTransitInfo(): TransitInfo = TransitInfo(
    subwayLine = this?.subwayLine?.trimOrNull(),
    subwayStation = this?.subwayStation?.trimOrNull(),
    subwayWalkTime = this?.kaptdWtimesub?.trimOrNull(),
    busWalkTime = this?.kaptdWtimebus?.trimOrNull(),
)

private fun ComplexInfoDto?.toSurroundings(): Surroundings = Surroundings(
    // 편의·교육은 "관공서(청림동) 병원(고려병원)"처럼 공백으로 이어 붙어 온다.
    // 괄호 안에는 공백이 없어 공백 분리가 안전하다.
    convenient = this?.convenientFacility.splitFacilities(" "),
    education = this?.educationFacility.splitFacilities(" "),
    // 단지 내 시설만 쉼표로 온다.
    welfare = this?.welfareFacility.splitFacilities(","),
)

private fun String?.splitFacilities(separator: String): List<String> =
    this?.split(separator)
        ?.map(String::trim)
        ?.filter { it.isNotEmpty() }
        .orEmpty()

private fun String.trimOrNull(): String? = trim().takeIf { it.isNotEmpty() }

private fun AreaTrendDto.toDomain(): AreaTrend = AreaTrend(
    latestAmount = latest?.amount,
    latestDate = latest?.date,
    latestFloor = latest?.floor,
    change = change,
    dealCount = count,
    monthlyAverages = trend.map {
        TrendPoint(yearMonth = it.ym, averageAmount = it.avg, dealCount = it.count)
    },
    deals = deals.map { ComplexDeal(date = it.date, floor = it.floor, amount = it.amount) },
)
