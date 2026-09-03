package com.ams.youthhouse.feature.trade.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 임장노트 한 건. 단지당 하나라 [kaptCode]가 기본키다.
 *
 * 점수는 항목별 열로 편다 — JSON으로 뭉치면 나중에 "채광 4점 이상" 같은 질의를 못 한다.
 */
@Entity(tableName = "site_visit_note")
data class SiteVisitNoteEntity(
    @PrimaryKey val kaptCode: String,
    val complexName: String,
    val regionLabel: String,
    /** `YYYYMMDD` */
    val visitedOn: String,
    val viewedUnit: String,
    val lightScore: Int?,
    val noiseScore: Int?,
    val parkingScore: Int?,
    val managementScore: Int?,
    val walkToStationMinutes: Int?,
    /** [com.ams.youthhouse.feature.trade.domain.model.ElevatorCondition]의 이름. */
    val elevatorCondition: String,
    /** [com.ams.youthhouse.feature.trade.domain.model.DefectStatus]의 이름. */
    val defectStatus: String,
    val memo: String,
    val builtYear: String?,
    val householdCount: Int?,
    val subwayLabel: String?,
    val referenceArea: Double?,
    val referenceAmount: Long?,
    val updatedAtMillis: Long,
)

@Dao
interface SiteVisitNoteDao {

    @Query("SELECT * FROM site_visit_note ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<SiteVisitNoteEntity>>

    @Query("SELECT * FROM site_visit_note WHERE kaptCode = :kaptCode")
    fun observeByCode(kaptCode: String): Flow<SiteVisitNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SiteVisitNoteEntity)

    @Query("DELETE FROM site_visit_note WHERE kaptCode = :kaptCode")
    suspend fun deleteByCode(kaptCode: String)
}
