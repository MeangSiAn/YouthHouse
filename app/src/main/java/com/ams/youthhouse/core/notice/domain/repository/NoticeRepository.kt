package com.ams.youthhouse.core.notice.domain.repository

import androidx.paging.PagingData
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {

    /**
     * 모집공고 목록을 페이지 단위로 흘려보낸다.
     *
     * `PagingData`는 `androidx.paging:paging-common` 타입이다. 이 아티팩트는
     * Android SDK에 의존하지 않는 순수 Kotlin이라 domain의 순수성을 깨지 않는다.
     *
     * @param category 임대와 분양은 서로 다른 오퍼레이션이라 한 번에 한 분야만 페이징한다.
     *   이 API에는 정렬 파라미터가 없어 두 분야를 합쳐도 순서에 의미가 없다.
     * @param region `null`이면 전체 지역
     */
    fun getNotices(category: NoticeCategory, region: NoticeRegion?): Flow<PagingData<Notice>>

    /**
     * 지금 시점의 공고를 한 덩어리로 가져온다. 페이지 상태를 갖지 않는 스냅샷이다.
     *
     * 홈 요약처럼 "전체를 한 번 훑어야 계산이 되는" 경우에 쓴다.
     * 마감 임박 한 건을 고르려면 전 건의 마감일을 봐야 하는데, 이 API에는 정렬
     * 파라미터가 없어 서버가 대신 골라줄 수 없기 때문이다.
     *
     * @param maxCount 한 번에 받을 최대 건수. 이 API는 `numOfRows` 상한이 사실상 없어
     *   500이면 지역 단위 전 건(임대 최대 76행, 분양 최대 27행)을 덮는다.
     *   다만 지역 미지정은 임대만 300행이 넘고(약 450KB) 분양도 63행이라
     *   호출자가 작은 값을 줘야 한다.
     */
    suspend fun getNoticeSnapshot(
        category: NoticeCategory,
        region: NoticeRegion?,
        maxCount: Int,
    ): List<Notice>
}
