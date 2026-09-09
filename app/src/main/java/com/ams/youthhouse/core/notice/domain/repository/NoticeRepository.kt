package com.ams.youthhouse.core.notice.domain.repository

import androidx.paging.PagingData
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {

    /**
     * 모집공고 목록을 페이지 단위로 흘려보낸다.
     *
     * `PagingData`는 `androidx.paging:paging-common` 타입이다. 이 아티팩트는
     * Android SDK에 의존하지 않는 순수 Kotlin이라 domain의 순수성을 깨지 않는다.
     *
     * 세 인자 모두 서버 쿼리로 나간다 — 걸러진 결과만 받으므로 앱이 페이지를 받아
     * 다시 거를 일이 없다.
     *
     * @param category 임대와 분양은 한 번에 한 분야만 페이징한다.
     * @param region `null`이면 전체 지역
     */
    fun getNotices(
        category: NoticeCategory,
        region: NoticeRegion?,
        status: NoticeStatusFilter,
    ): Flow<PagingData<Notice>>

    /**
     * 지금 시점의 공고를 한 덩어리로 가져온다. 페이지 상태를 갖지 않는 스냅샷이다.
     *
     * 홈 요약처럼 "전체를 한 번 훑어야 계산이 되는" 경우에 쓴다.
     * 접수중·예정·오늘 마감을 한꺼번에 세야 하므로 상태로 거르지 않는다.
     *
     * @param maxCount 한 번에 받을 최대 건수. 서버 상한이 200이라 그보다 크게 줘도 잘린다.
     *   지역을 지정하면 한 분야당 100건 아래라 한 번으로 전 건을 덮는다.
     */
    suspend fun getNoticeSnapshot(
        category: NoticeCategory,
        region: NoticeRegion?,
        maxCount: Int,
    ): List<Notice>

    /**
     * 공고 한 건을 다시 조회한다. 목록에 없는 필드(주소·문의처·난방방식·잔금)를 채운다.
     *
     * 마감된 공고는 서버 목록에서 사라지므로 여기서도 찾을 수 없다.
     * 호출부는 실패를 화면 오류로 올리지 말고 갖고 있던 스냅숏으로 계속 그려야 한다.
     */
    suspend fun getNotice(noticeId: String): Notice
}
