package com.ams.youthhouse.feature.trade.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * 관심 단지 한 건. [kaptCode]가 K-apt 전역 고유 코드라 그대로 기본키다.
 * 공고 찜과 달리 재조회가 가능하므로 스냅숏 없이 표시용 필드만 둔다.
 */
@Entity(tableName = "favorite_complex")
data class FavoriteComplexEntity(
    @PrimaryKey val kaptCode: String,
    val name: String,
    val regionLabel: String,
    val savedAtMillis: Long,
)

@Dao
interface FavoriteComplexDao {

    @Query("SELECT * FROM favorite_complex ORDER BY savedAtMillis DESC")
    fun observeAll(): Flow<List<FavoriteComplexEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_complex WHERE kaptCode = :kaptCode)")
    suspend fun exists(kaptCode: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteComplexEntity)

    @Query("DELETE FROM favorite_complex WHERE kaptCode = :kaptCode")
    suspend fun deleteByCode(kaptCode: String)
}

/**
 * trade 슬라이스 전용 DB. notice 쪽 DB와 분리한다 —
 * 두 슬라이스가 서로의 스키마 버전에 발목 잡히지 않게.
 */
@Database(
    entities = [FavoriteComplexEntity::class, SiteVisitNoteEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class TradeDatabase : RoomDatabase() {
    abstract fun favoriteComplexDao(): FavoriteComplexDao
    abstract fun siteVisitNoteDao(): SiteVisitNoteDao
}
