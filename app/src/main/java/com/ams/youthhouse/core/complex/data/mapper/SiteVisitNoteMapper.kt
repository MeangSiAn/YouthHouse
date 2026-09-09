package com.ams.youthhouse.core.complex.data.mapper

import com.ams.youthhouse.core.complex.data.local.SiteVisitNoteEntity
import com.ams.youthhouse.core.complex.domain.model.ComplexSnapshot
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings

fun SiteVisitNoteEntity.toDomain(): SiteVisitNote = SiteVisitNote(
    kaptCode = kaptCode,
    complexName = complexName,
    regionLabel = regionLabel,
    visitedOn = visitedOn,
    viewedUnit = viewedUnit,
    // with()가 범위를 접어 주므로 손으로 고친 DB 값이 와도 도메인 불변식이 지켜진다.
    ratings = VisitRatings()
        .with(VisitCriterion.LIGHT, lightScore)
        .with(VisitCriterion.NOISE, noiseScore)
        .with(VisitCriterion.PARKING, parkingScore)
        .with(VisitCriterion.MANAGEMENT, managementScore),
    walkToStationMinutes = walkToStationMinutes,
    elevatorCondition = ElevatorCondition.entries.firstOrNull { it.name == elevatorCondition }
        ?: ElevatorCondition.UNCHECKED,
    defectStatus = DefectStatus.entries.firstOrNull { it.name == defectStatus }
        ?: DefectStatus.UNCHECKED,
    memo = memo,
    snapshot = ComplexSnapshot(
        builtYear = builtYear,
        householdCount = householdCount,
        subwayLabel = subwayLabel,
        referenceArea = referenceArea,
        referenceAmount = referenceAmount,
    ),
    updatedAtMillis = updatedAtMillis,
)

fun SiteVisitNote.toEntity(updatedAtMillis: Long): SiteVisitNoteEntity = SiteVisitNoteEntity(
    kaptCode = kaptCode,
    complexName = complexName,
    regionLabel = regionLabel,
    visitedOn = visitedOn,
    viewedUnit = viewedUnit,
    lightScore = ratings[VisitCriterion.LIGHT],
    noiseScore = ratings[VisitCriterion.NOISE],
    parkingScore = ratings[VisitCriterion.PARKING],
    managementScore = ratings[VisitCriterion.MANAGEMENT],
    walkToStationMinutes = walkToStationMinutes,
    elevatorCondition = elevatorCondition.name,
    defectStatus = defectStatus.name,
    memo = memo,
    builtYear = snapshot.builtYear,
    householdCount = snapshot.householdCount,
    subwayLabel = snapshot.subwayLabel,
    referenceArea = snapshot.referenceArea,
    referenceAmount = snapshot.referenceAmount,
    updatedAtMillis = updatedAtMillis,
)
