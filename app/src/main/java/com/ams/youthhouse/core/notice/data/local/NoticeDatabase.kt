package com.ams.youthhouse.core.notice.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * notice 슬라이스의 로컬 DB.
 *
 * core/database가 아니라 여기(core/notice/data/local)에 두는 이유:
 * 엔티티가 notice 도메인의 것이라, DB를 밖으로 빼면 core/database → core/notice
 * 의존이 생기며 슬라이스 응집이 깨진다. 다른 슬라이스가 DB를 갖게 되면
 * 그때 공용 DB로 합칠지 결정한다.
 */
@Database(
    entities = [FavoriteNoticeEntity::class],
    // v3: 찜 키를 백엔드의 notice_id로 교체. 스키마는 같지만 키 포맷이 달라
    // 옛 행이 남으면 같은 찜이 두 번 저장될 수 있어 버전을 올려 비운다(미출시 상태라 무해).
    version = 3,
    exportSchema = false,
)
abstract class NoticeDatabase : RoomDatabase() {
    abstract fun favoriteNoticeDao(): FavoriteNoticeDao
}
