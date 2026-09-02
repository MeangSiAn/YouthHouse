package com.ams.youthhouse.core.notice.data.repository

import com.ams.youthhouse.core.notice.data.local.FavoriteNoticeDao
import com.ams.youthhouse.core.notice.data.local.FavoriteNoticeEntity
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeKey
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeRepository
import com.ams.youthhouse.core.notice.domain.repository.toFavoriteKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteNoticeRepositoryImpl @Inject constructor(
    private val dao: FavoriteNoticeDao,
) : FavoriteNoticeRepository {

    // API DTO용 전역 Json과 분리한다. 스냅숏은 우리가 쓴 것을 우리가 읽는 구조지만,
    // 앱 업데이트로 Notice에 필드가 추가/삭제될 수 있어 관대하게 읽는다.
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override val favorites: Flow<List<Notice>> =
        dao.observeAll().map { entities ->
            // 스냅숏이 옛 버전과 호환되지 않으면 그 한 건만 조용히 건너뛴다.
            // 찜 목록 전체가 죽는 것보다 낫다.
            entities.mapNotNull { entity ->
                runCatching { json.decodeFromString<Notice>(entity.noticeJson) }.getOrNull()
            }
        }

    // 저장된 키 문자열을 파싱하지 않고 스냅숏에서 다시 계산한다.
    // 키 구성이 바뀌어도(이번처럼) 옛 데이터와 어긋날 일이 없다.
    override val favoriteKeys: Flow<Set<FavoriteNoticeKey>> =
        favorites.map { notices -> notices.map { it.toFavoriteKey() }.toSet() }

    override suspend fun toggle(notice: Notice) {
        val key = notice.toFavoriteKey().asStorageKey()
        if (dao.exists(key)) {
            dao.deleteByKey(key)
        } else {
            dao.insert(
                FavoriteNoticeEntity(
                    key = key,
                    category = notice.category.name,
                    pblancId = notice.pblancId,
                    noticeJson = json.encodeToString(notice),
                    savedAtMillis = System.currentTimeMillis(),
                ),
            )
        }
    }
}

/**
 * DB 기본키로 쓸 불투명 문자열. **다시 파싱하지 않는다** — 같은 행인지 판별하는 용도뿐이다.
 * 지역·단지명에 구분자가 들어가는 병적인 경우까지 안전할 필요는 없고,
 * 만에 하나 충돌해도 "화면상 구분 안 되는 두 행이 함께 찜되는" 정도라 해가 없다.
 */
private fun FavoriteNoticeKey.asStorageKey(): String =
    listOf(category.name, pblancId, houseSn.toString(), districtName, complexName)
        .joinToString(separator = "|")
