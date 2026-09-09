package com.ams.youthhouse.core.notice.domain.repository

import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import kotlinx.coroutines.flow.Flow

/**
 * 찜한 공고 저장소.
 *
 * 찜은 [Notice] 스냅숏 자체를 저장한다. 백엔드는 마감된 공고를 목록에 남기지 않으므로
 * `notice_id`로 다시 조회해도 찾을 수 없고, 저장해 둔 스냅숏만이 그 공고를 다시 그린다.
 * 마감된 공고가 이력으로 남는 것은 사용자가 기대하는 동작이기도 하다.
 *
 * 키는 **행 단위**다. 같은 공고(`pblancId`)가 시군구·단지별 여러 행으로 내려오는데,
 * 목록에는 그 행들이 지역명이 다른 별개 카드로 보인다. 영통구 카드를 찜했는데
 * 기흥·오산 카드까지 켜지면 "내가 누른 것"과 "표시된 것"이 어긋난다.
 */
interface FavoriteNoticeRepository {

    /** 찜한 공고 전체. 최근에 찜한 것이 앞에 온다. */
    val favorites: Flow<List<Notice>>

    /** 하트 표시용 키 집합. 목록 화면이 카드마다 포함 여부를 조회한다. */
    val favoriteKeys: Flow<Set<FavoriteNoticeKey>>

    /** 이미 찜했으면 해제하고, 아니면 저장한다. */
    suspend fun toggle(notice: Notice)
}

/**
 * 찜 한 건의 정체성.
 *
 * 백엔드의 `notice_id` 하나면 된다. 예전에는 공고 번호·지역·단지명을 조합해
 * 행을 구분해야 했다 — 원본 API에 행 식별자가 없었기 때문이다.
 *
 * 내용이 완전히 같은 행끼리는 id도 같아 함께 찜된다. 화면에서도 구분되지 않는
 * 행들이라 "내가 누른 것"과 "표시된 것"이 어긋나 보이지 않는다.
 */
@JvmInline
value class FavoriteNoticeKey(val noticeId: String)

fun Notice.toFavoriteKey(): FavoriteNoticeKey = FavoriteNoticeKey(noticeId)
