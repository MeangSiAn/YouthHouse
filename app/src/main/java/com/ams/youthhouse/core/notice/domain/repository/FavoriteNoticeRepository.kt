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
 * 키는 **행 단위**다. 같은 공고(`pblancId`)가 시군구·단지별 여러 행으로 내려오는데,
 * 목록에는 그 행들이 지역명이 다른 별개 카드로 보인다. 영통구 카드를 찜했는데
 * 기흥·오산 카드까지 켜지면 "내가 누른 것"과 "표시된 것"이 어긋난다.
 * 그래서 사용자가 시각적으로 구분할 수 있는 필드까지 키에 포함시킨다.
 * 32개 필드가 완전히 동일한 중복 행은 같은 키를 갖지만, 어차피 화면에서도
 * 구분할 수 없는 행이므로 함께 켜져도 모순이 없다.
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
 * 찜 한 건의 정체성. [Notice] 내용에서만 파생되므로 저장된 스냅숏에서 언제든 재계산된다.
 * 빈 값은 ""로 정규화해 `null`/`""` 차이로 같은 행이 두 키를 갖지 않게 한다.
 */
data class FavoriteNoticeKey(
    val category: NoticeCategory,
    val pblancId: String,
    val houseSn: Int,
    val districtName: String,
    val complexName: String,
)

fun Notice.toFavoriteKey(): FavoriteNoticeKey = FavoriteNoticeKey(
    category = category,
    pblancId = pblancId,
    houseSn = houseSn,
    districtName = address.districtName.orEmpty(),
    complexName = complexName.orEmpty(),
)
