package com.ams.youthhouse.core.notice.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ams.youthhouse.core.notice.data.datasource.NoticeRemoteDataSource
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import kotlinx.coroutines.CancellationException

/**
 * offset 기반 [PagingSource].
 *
 * **서버가 안정적인 정렬을 보장한다는 전제** 위에 있다. offset 페이징은 같은 쿼리가
 * 매번 같은 순서를 돌려줘야 성립한다 — 순서가 흔들리면 항목이 겹치거나 조용히 빠진다.
 * 백엔드가 마감 임박순(apply_end)으로 정렬해 주므로 지금은 안전하다.
 * (정렬 없이 20건씩 10페이지를 받았을 때 200건 중 59건이 누락된 적이 있다.)
 *
 * `params.loadSize`를 쓰지 않고 항상 [PAGE_SIZE]를 보낸다. Paging의 기본
 * `initialLoadSize`는 `pageSize * 3`이라, 그대로 두면 첫 로드가 60건을 요청했다고
 * 간주하면서 다음 키는 20이 되어 항목이 겹친다. Pager 쪽에서도
 * `initialLoadSize = PAGE_SIZE`로 맞춘다.
 */
class NoticePagingSource(
    private val remoteDataSource: NoticeRemoteDataSource,
    private val category: NoticeCategory,
    private val region: NoticeRegion?,
    private val status: NoticeStatusFilter,
) : PagingSource<Int, Notice>() {

    /**
     * 무효화 후 다시 로드할 지점. 화면에 보이던 위치가 속한 페이지의 offset을 그대로 쓴다.
     * offset 기반이라 페이지 번호를 되돌리는 계산이 필요 없다.
     */
    override fun getRefreshKey(state: PagingState<Int, Notice>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(PAGE_SIZE)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(PAGE_SIZE)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notice> {
        val offset = params.key ?: START_OFFSET

        return try {
            val page = remoteDataSource.fetchNotices(
                category = category,
                region = region,
                status = status,
                limit = PAGE_SIZE,
                offset = offset,
            )

            val loadedCount = offset + page.notices.size
            val isLastPage = page.notices.isEmpty() || loadedCount >= page.totalCount

            LoadResult.Page(
                data = page.notices,
                prevKey = if (offset == START_OFFSET) null else (offset - PAGE_SIZE).coerceAtLeast(START_OFFSET),
                nextKey = if (isLastPage) null else loadedCount,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            LoadResult.Error(throwable)
        }
    }

    companion object {
        const val PAGE_SIZE = 20

        private const val START_OFFSET = 0
    }
}
