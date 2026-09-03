package com.ams.youthhouse.feature.trade.domain.repository

import com.ams.youthhouse.feature.trade.domain.model.RecentComplex
import kotlinx.coroutines.flow.Flow

/**
 * 최근에 열어 본 단지. 매매 탭 검색창 아래 줄에 쓴다.
 *
 * 관심 단지와 달리 사용자가 관리하지 않는다 — 앱이 쌓고, 오래된 것부터 조용히 버린다.
 */
interface RecentComplexRepository {

    /** 최근에 본 것이 앞에 온다. 화면에 보여 줄 만큼만 흘려보낸다. */
    val recents: Flow<List<RecentComplex>>

    /** 단지 상세를 열었을 때 호출한다. 같은 단지면 시각만 갱신된다. */
    suspend fun record(complex: RecentComplex)
}
