package com.ams.youthhouse.feature.trade.domain.repository

import com.ams.youthhouse.feature.trade.domain.model.FavoriteComplex
import kotlinx.coroutines.flow.Flow

/**
 * 관심 단지 저장소.
 *
 * 공고 찜과 달리 스냅숏이 필요 없다 — [FavoriteComplex.kaptCode]로 언제든 재조회되므로
 * 목록 표시에 쓰는 최소 필드만 저장한다.
 */
interface FavoriteComplexRepository {

    /** 최근에 추가한 것이 앞에 온다. */
    val favorites: Flow<List<FavoriteComplex>>

    val favoriteCodes: Flow<Set<String>>

    suspend fun toggle(complex: FavoriteComplex)
}
