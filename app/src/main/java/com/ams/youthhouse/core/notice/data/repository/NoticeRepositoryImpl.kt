package com.ams.youthhouse.core.notice.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ams.youthhouse.core.notice.data.datasource.NoticeRemoteDataSource
import com.ams.youthhouse.core.notice.data.paging.NoticePagingSource
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
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
    ): Flow<PagingData<Notice>> = Pager(
        config = PagingConfig(
            pageSize = NoticePagingSource.PAGE_SIZE,
            // 기본값(pageSize * 3)을 쓰면 페이지 번호 기반 API에서 항목이 중복 로드된다.
            initialLoadSize = NoticePagingSource.PAGE_SIZE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { NoticePagingSource(remoteDataSource, category, region?.code) },
    ).flow

    override suspend fun getNoticeSnapshot(
        category: NoticeCategory,
        region: NoticeRegion?,
        maxCount: Int,
    ): List<Notice> =
        remoteDataSource.fetchNotices(
            category = category,
            pageNo = FIRST_PAGE,
            numOfRows = maxCount,
            brtcCode = region?.code,
        ).notices

    private companion object {
        const val FIRST_PAGE = 1
    }
}
