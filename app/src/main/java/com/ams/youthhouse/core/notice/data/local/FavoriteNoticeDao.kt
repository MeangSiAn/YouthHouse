package com.ams.youthhouse.core.notice.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteNoticeDao {

    @Query("SELECT * FROM favorite_notice ORDER BY savedAtMillis DESC")
    fun observeAll(): Flow<List<FavoriteNoticeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_notice WHERE `key` = :key)")
    suspend fun exists(key: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteNoticeEntity)

    @Query("DELETE FROM favorite_notice WHERE `key` = :key")
    suspend fun deleteByKey(key: String)
}
