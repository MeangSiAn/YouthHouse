package com.ams.youthhouse.core.backup.data.dto

import com.ams.youthhouse.core.notice.domain.model.Notice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 백업 파일 형식. **iOS와 공유하는 계약**이라 필드 이름을 바꾸면 양쪽 다 깨진다.
 *
 * 도메인 모델을 그대로 직렬화하지 않고 따로 둔 이유: 도메인 필드를 고쳐도 옛 백업 파일은
 * 읽혀야 한다. [formatVersion]을 올리는 것은 옛 파일을 못 읽게 되는 변경뿐이다.
 *
 * 공고 찜은 [Notice] 스냅숏을 그대로 싣는다. 찜 DB가 이미 같은 JSON을 저장하고 있고,
 * 마감된 공고는 서버에서 다시 받을 수 없어 스냅숏 자체가 원본이다.
 */
@Serializable
data class BackupFileDto(
    @SerialName("app") val app: String = APP_ID,
    @SerialName("format_version") val formatVersion: Int = FORMAT_VERSION,
    /** epoch millis. 표시용이라 정밀도는 중요하지 않다. */
    @SerialName("exported_at") val exportedAt: Long,
    @SerialName("author_token") val authorToken: String? = null,
    @SerialName("settings") val settings: BackupSettingsDto? = null,
    @SerialName("favorite_notices") val favoriteNotices: List<Notice> = emptyList(),
    @SerialName("favorite_complexes") val favoriteComplexes: List<BackupComplexDto> = emptyList(),
    @SerialName("site_visit_notes") val siteVisitNotes: List<BackupNoteDto> = emptyList(),
) {
    companion object {
        const val APP_ID = "youthhouse"
        const val FORMAT_VERSION = 1
    }
}

@Serializable
data class BackupSettingsDto(
    /** 지역 API 코드("11"). `null`이면 전체. */
    @SerialName("region_code") val regionCode: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("status_filter") val statusFilter: String? = null,
)

@Serializable
data class BackupComplexDto(
    @SerialName("kapt_code") val kaptCode: String,
    @SerialName("name") val name: String,
    @SerialName("region_label") val regionLabel: String = "",
)

@Serializable
data class BackupNoteDto(
    @SerialName("kapt_code") val kaptCode: String,
    @SerialName("complex_name") val complexName: String,
    @SerialName("region_label") val regionLabel: String = "",
    /** `YYYYMMDD` */
    @SerialName("visited_on") val visitedOn: String,
    @SerialName("viewed_unit") val viewedUnit: String = "",
    /** 항목 이름(LIGHT/NOISE/PARKING/MANAGEMENT) → 1~5. 매기지 않은 항목은 없다. */
    @SerialName("scores") val scores: Map<String, Int> = emptyMap(),
    @SerialName("walk_to_station_minutes") val walkToStationMinutes: Int? = null,
    @SerialName("elevator") val elevator: String? = null,
    @SerialName("defect") val defect: String? = null,
    @SerialName("memo") val memo: String = "",
    @SerialName("snapshot") val snapshot: BackupSnapshotDto? = null,
    @SerialName("updated_at") val updatedAtMillis: Long = 0L,
)

@Serializable
data class BackupSnapshotDto(
    @SerialName("built_year") val builtYear: String? = null,
    @SerialName("household_count") val householdCount: Int? = null,
    @SerialName("subway_label") val subwayLabel: String? = null,
    @SerialName("reference_area") val referenceArea: Double? = null,
    @SerialName("reference_amount") val referenceAmount: Long? = null,
)
