package com.ams.youthhouse.feature.trade.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 최근에 열어 본 단지.
 *
 * 검색어 문자열이 아니라 **단지**를 남긴다. 기획서가 이 줄에 기대하는 동작은
 * "다시 볼 단지로 빠르게 복귀"인데, 검색어를 되살리면 결과 목록을 한 번 더
 * 거쳐야 해서 탭이 하나 늘어난다.
 *
 * 같은 단지를 다시 열면 새 행이 아니라 시각만 갱신된다([kaptCode]가 기본키).
 */
@Entity(tableName = "recent_complex")
data class RecentComplexEntity(
    @PrimaryKey val kaptCode: String,
    val name: String,
    val regionLabel: String,
    val viewedAtMillis: Long,
)

@Dao
interface RecentComplexDao {

    @Query("SELECT * FROM recent_complex ORDER BY viewedAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentComplexEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentComplexEntity)

    /**
     * 목록에 보이는 만큼만 남기고 오래된 행을 지운다.
     *
     * 기록할 때마다 호출한다. 이 표는 화면에 몇 줄 보여 주는 것이 전부라
     * 방문 이력을 무한히 쌓아 둘 이유가 없다.
     */
    @Query(
        """
        DELETE FROM recent_complex
        WHERE kaptCode NOT IN (
            SELECT kaptCode FROM recent_complex ORDER BY viewedAtMillis DESC LIMIT :keep
        )
        """,
    )
    suspend fun trimTo(keep: Int)
}
