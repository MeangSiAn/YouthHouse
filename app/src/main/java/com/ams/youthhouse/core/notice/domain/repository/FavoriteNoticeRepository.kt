package com.ams.youthhouse.core.notice.domain.repository

import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import kotlinx.coroutines.flow.Flow

/**
 * 찜한 공고 저장소.
 *
 * 이 API는 식별자로 공고를 재조회할 수 없으므로(고유 키 없음, 상세 조회 오퍼레이션 없음)
 * 찜은 [Notice] 스냅숏 자체를 저장한다. 공고가 내려가도 찜 목록에는 남는다는 뜻이고,
 * 그게 사용자가 기대하는 동작이기도 하다 — 마감된 공고도 이력으로 보인다.
 *
 * 키는 **공고 단위**인 `(category, pblancId)`다. 같은 공고가 시군구별 여러 행으로
 * 내려와도 찜은 한 건이며, 어느 행에서 찜하든 같은 공고로 취급된다.
 */
interface FavoriteNoticeRepository {

    /** 찜한 공고 전체. 최근에 찜한 것이 앞에 온다. */
    val favorites: Flow<List<Notice>>

    /** 하트 표시용 키 집합. 목록 화면이 카드마다 포함 여부를 조회한다. */
    val favoriteKeys: Flow<Set<FavoriteNoticeKey>>

    /** 이미 찜했으면 해제하고, 아니면 저장한다. */
    suspend fun toggle(notice: Notice)
}

data class FavoriteNoticeKey(
    val category: NoticeCategory,
    val pblancId: String,
)

fun Notice.toFavoriteKey(): FavoriteNoticeKey = FavoriteNoticeKey(category, pblancId)
