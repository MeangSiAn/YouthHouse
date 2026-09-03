package com.ams.youthhouse.feature.trade.data.mapper

import com.ams.youthhouse.feature.trade.data.dto.AreaTrendDto
import com.ams.youthhouse.feature.trade.data.dto.ComplexDetailResponseDto
import com.ams.youthhouse.feature.trade.data.dto.ComplexSummaryDto
import com.ams.youthhouse.feature.trade.domain.model.AptComplex
import com.ams.youthhouse.feature.trade.domain.model.AreaTrend
import com.ams.youthhouse.feature.trade.domain.model.ComplexDeal
import com.ams.youthhouse.feature.trade.domain.model.ComplexDetail
import com.ams.youthhouse.feature.trade.domain.model.TrendPoint

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
        heatingName = info?.codeHeatNm,
        constructorName = info?.kaptBcompany,
        // 세대가 많은 대표 평형이 먼저 보이도록 큰 면적부터. by_area에 실제로 있는 것만 남긴다.
        areas = deals?.areas.orEmpty().filter { it in trends }.sortedDescending(),
        trendsByArea = trends,
        dealsNote = deals?.note,
    )
}

private fun AreaTrendDto.toDomain(): AreaTrend = AreaTrend(
    latestAmount = latest?.amount,
    latestDate = latest?.date,
    latestFloor = latest?.floor,
    change = change,
    dealCount = count,
    monthlyAverages = trend.map { TrendPoint(yearMonth = it.ym, averageAmount = it.avg) },
    deals = deals.map { ComplexDeal(date = it.date, floor = it.floor, amount = it.amount) },
)
