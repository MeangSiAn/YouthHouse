package com.ams.youthhouse.core.notice.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 찜한 공고 한 건.
 *
 * 공고 본문은 [noticeJson]에 도메인 모델을 통째로 직렬화해 담는다.
 * 필드를 컬럼으로 펼치지 않는 이유: 이 데이터는 조회·표시만 하고 필드 단위로
 * 질의할 일이 없으며, API 응답 필드가 바뀔 때 스키마 마이그레이션을 피하고 싶어서다.
 * 질의에 쓰는 것(키·정렬)만 컬럼으로 뺀다.
 */
@Entity(tableName = "favorite_notice")
data class FavoriteNoticeEntity(
    /** "RENTAL:21096" 형태. (category, pblancId)를 합친 공고 단위 키. */
    @PrimaryKey val key: String,
    val category: String,
    val pblancId: String,
    val noticeJson: String,
    /** epoch millis. 최근 찜이 위로 오는 정렬 기준. */
    val savedAtMillis: Long,
)
