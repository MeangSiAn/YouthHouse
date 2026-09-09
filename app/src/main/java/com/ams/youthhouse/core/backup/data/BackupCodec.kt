package com.ams.youthhouse.core.backup.data

import com.ams.youthhouse.core.backup.data.dto.BackupComplexDto
import com.ams.youthhouse.core.backup.data.dto.BackupFileDto
import com.ams.youthhouse.core.backup.data.dto.BackupNoteDto
import com.ams.youthhouse.core.backup.data.dto.BackupSettingsDto
import com.ams.youthhouse.core.backup.data.dto.BackupSnapshotDto
import com.ams.youthhouse.core.backup.domain.model.BackupPayload
import com.ams.youthhouse.core.backup.domain.model.BackupSettings
import com.ams.youthhouse.core.backup.domain.repository.BackupFormatException
import com.ams.youthhouse.core.complex.domain.model.ComplexSnapshot
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.FavoriteComplex
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * 백업 파일 텍스트 ↔ 도메인. 파일 I/O는 모르므로 JVM 테스트로 바로 검증된다.
 *
 * 읽을 때는 관대하다 — 모르는 키는 무시하고, 모르는 enum 이름은 "미확인"으로, 범위 밖
 * 점수는 안으로 접는다. 옛 앱이 만든 파일이든 손으로 고친 파일이든 최대한 살린다.
 */
object BackupCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
        explicitNulls = false
    }

    fun encode(payload: BackupPayload, exportedAtMillis: Long): String =
        json.encodeToString(BackupFileDto.serializer(), payload.toDto(exportedAtMillis))

    /** @throws BackupFormatException JSON이 아니거나 이 앱의 파일이 아닐 때 */
    fun decode(text: String): BackupPayload {
        val dto = try {
            json.decodeFromString(BackupFileDto.serializer(), text)
        } catch (exception: SerializationException) {
            throw BackupFormatException("백업 파일을 읽을 수 없습니다: ${exception.message}")
        } catch (exception: IllegalArgumentException) {
            throw BackupFormatException("백업 파일을 읽을 수 없습니다: ${exception.message}")
        }
        if (dto.app != BackupFileDto.APP_ID) {
            throw BackupFormatException("이 앱의 백업 파일이 아닙니다: app=${dto.app}")
        }
        if (dto.formatVersion > BackupFileDto.FORMAT_VERSION) {
            throw BackupFormatException("더 새 버전의 백업 파일입니다: v${dto.formatVersion}")
        }
        return dto.toDomain()
    }

    private fun BackupPayload.toDto(exportedAtMillis: Long) = BackupFileDto(
        exportedAt = exportedAtMillis,
        authorToken = authorToken,
        settings = settings?.let {
            BackupSettingsDto(
                regionCode = it.region?.code,
                category = it.category.name,
                statusFilter = it.statusFilter.name,
            )
        },
        favoriteNotices = favoriteNotices,
        favoriteComplexes = favoriteComplexes.map {
            BackupComplexDto(kaptCode = it.kaptCode, name = it.name, regionLabel = it.regionLabel)
        },
        siteVisitNotes = siteVisitNotes.map { it.toDto() },
    )

    private fun SiteVisitNote.toDto() = BackupNoteDto(
        kaptCode = kaptCode,
        complexName = complexName,
        regionLabel = regionLabel,
        visitedOn = visitedOn,
        viewedUnit = viewedUnit,
        scores = ratings.scores.mapKeys { it.key.name },
        walkToStationMinutes = walkToStationMinutes,
        elevator = elevatorCondition.name,
        defect = defectStatus.name,
        memo = memo,
        snapshot = BackupSnapshotDto(
            builtYear = snapshot.builtYear,
            householdCount = snapshot.householdCount,
            subwayLabel = snapshot.subwayLabel,
            referenceArea = snapshot.referenceArea,
            referenceAmount = snapshot.referenceAmount,
        ),
        updatedAtMillis = updatedAtMillis,
    )

    private fun BackupFileDto.toDomain() = BackupPayload(
        authorToken = authorToken?.takeIf { it.isNotBlank() },
        settings = settings?.let {
            BackupSettings(
                region = NoticeRegion.entries.firstOrNull { region -> region.code == it.regionCode },
                category = NoticeCategory.entries.firstOrNull { c -> c.name == it.category }
                    ?: NoticeCategory.RENTAL,
                statusFilter = NoticeStatusFilter.fromName(it.statusFilter),
            )
        },
        favoriteNotices = favoriteNotices.filter { it.noticeId.isNotBlank() },
        favoriteComplexes = favoriteComplexes
            .filter { it.kaptCode.isNotBlank() }
            .map { FavoriteComplex(kaptCode = it.kaptCode, name = it.name, regionLabel = it.regionLabel) },
        siteVisitNotes = siteVisitNotes.filter { it.kaptCode.isNotBlank() }.map { it.toDomain() },
    )

    private fun BackupNoteDto.toDomain() = SiteVisitNote(
        kaptCode = kaptCode,
        complexName = complexName,
        regionLabel = regionLabel,
        visitedOn = visitedOn,
        viewedUnit = viewedUnit,
        // with()가 범위를 접는다. 모르는 항목 이름은 버린다.
        ratings = scores.entries.fold(VisitRatings()) { acc, (name, score) ->
            VisitCriterion.entries.firstOrNull { it.name == name }
                ?.let { acc.with(it, score) }
                ?: acc
        },
        walkToStationMinutes = walkToStationMinutes,
        elevatorCondition = ElevatorCondition.entries.firstOrNull { it.name == elevator }
            ?: ElevatorCondition.UNCHECKED,
        defectStatus = DefectStatus.entries.firstOrNull { it.name == defect }
            ?: DefectStatus.UNCHECKED,
        memo = memo,
        snapshot = snapshot?.let {
            ComplexSnapshot(
                builtYear = it.builtYear,
                householdCount = it.householdCount,
                subwayLabel = it.subwayLabel,
                referenceArea = it.referenceArea,
                referenceAmount = it.referenceAmount,
            )
        } ?: ComplexSnapshot.EMPTY,
        updatedAtMillis = updatedAtMillis,
    )
}
