package com.ams.youthhouse.core.notice.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ams.youthhouse.core.notice.data.datasource.NoticeRemoteDataSource
import com.ams.youthhouse.core.notice.data.paging.NoticePagingSource
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import com.ams.youthhouse.core.notice.domain.repository.NoticeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepositoryImpl @Inject constructor(
    private val remoteDataSource: NoticeRemoteDataSource,
) : NoticeRepository {

    override fun getNotices(
        category: NoticeCategory,
        region: NoticeRegion?,
        status: NoticeStatusFilter,
    ): Flow<PagingData<Notice>> = Pager(
        config = PagingConfig(
            pageSize = NoticePagingSource.PAGE_SIZE,
            // 기본값(pageSize * 3)을 쓰면 첫 로드와 다음 페이지의 offset이 어긋나 항목이 겹친다.
            initialLoadSize = NoticePagingSource.PAGE_SIZE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            NoticePagingSource(remoteDataSource, category, region, status)
        },
    ).flow

    override suspend fun getNoticeSnapshot(
        category: NoticeCategory,
        region: NoticeRegion?,
        maxCount: Int,
    ): List<Notice> =
        remoteDataSource.fetchNotices(
            category = category,
            region = region,
            // 홈은 접수중·예정·오늘 마감을 함께 세므로 상태로 거르지 않는다.
            status = NoticeStatusFilter.ALL,
            limit = maxCount.coerceAtMost(MAX_PAGE_LIMIT),
            offset = FIRST_OFFSET,
        ).notices

    override suspend fun getNotice(noticeId: String): Notice =
        remoteDataSource.fetchNotice(noticeId)

    private companion object {
        const val FIRST_OFFSET = 0

        /** 서버가 정한 `limit` 상한. 넘겨 보내면 422로 거절당한다. */
        const val MAX_PAGE_LIMIT = 200
    }
}
