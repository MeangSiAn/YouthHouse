package com.ams.youthhouse.core.notice.domain.repository

import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import kotlinx.coroutines.flow.Flow

/**
 * 사용자가 고른 공고 필터. 홈과 공고 탭이 이 하나를 공유한다.
 *
 * 저장 기술(DataStore)은 `core/datastore`가 알고, 이 인터페이스는 도메인 타입만 다룬다.
 * 반대로 `core/datastore`가 [NoticeRegion]을 알면 저장 인프라가 도메인에 오염된다.
 */
interface NoticeFilterRepository {

    /** `null`이면 전체 지역. 저장된 값이 준비되기 전에는 아무것도 emit하지 않는다. */
    val selectedRegion: Flow<NoticeRegion?>

    /** 분야는 "전체"가 없다. 저장된 값이 없으면 [NoticeCategory.RENTAL]. */
    val selectedCategory: Flow<NoticeCategory>

    suspend fun setSelectedRegion(region: NoticeRegion?)

    suspend fun setSelectedCategory(category: NoticeCategory)
}
