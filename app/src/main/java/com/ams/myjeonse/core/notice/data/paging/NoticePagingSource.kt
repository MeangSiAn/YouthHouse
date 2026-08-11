package com.ams.myjeonse.core.notice.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ams.myjeonse.core.notice.data.datasource.NoticeRemoteDataSource
import com.ams.myjeonse.core.notice.domain.model.Notice
import kotlinx.coroutines.CancellationException

/**
 * 페이지 번호 기반 [PagingSource].
 *
 * `params.loadSize`를 쓰지 않고 항상 [PAGE_SIZE]를 보낸다.
 * Paging의 기본 `initialLoadSize`는 `pageSize * 3`이라, 그대로 두면 첫 로드가 60건을
 * 요청했다고 간주하면서 페이지 번호는 1이 되어 이후 페이지와 항목이 겹친다.
 * Pager 쪽에서도 `initialLoadSize = PAGE_SIZE`로 맞춘다.
 *
 * 응답 언랩(`resultCode` 분기, body 없음)은 [NoticeRemoteDataSource]가 담당한다.
 * 데이터 없음은 빈 페이지로 돌아오므로 여기서는 오류와 구분할 필요가 없다.
 */
class NoticePagingSource(
    private val remoteDataSource: NoticeRemoteDataSource,
    private val brtcCode: String? = null,
) : PagingSource<Int, Notice>() {

    override fun getRefreshKey(state: PagingState<Int, Notice>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notice> {
        val pageNo = params.key ?: START_PAGE

        return try {
            val page = remoteDataSource.fetchNotices(
                pageNo = pageNo,
                numOfRows = PAGE_SIZE,
                brtcCode = brtcCode,
            )

            // 빈 페이지면 nextKey를 null로 두어 Paging이 endOfPaginationReached로 넘어가게 한다.
            val isLastPage = page.notices.isEmpty() || pageNo * PAGE_SIZE >= page.totalCount

            LoadResult.Page(
                data = page.notices,
                prevKey = if (pageNo == START_PAGE) null else pageNo - 1,
                nextKey = if (isLastPage) null else pageNo + 1,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            LoadResult.Error(throwable)
        }
    }

    companion object {
        const val PAGE_SIZE = 20

        private const val START_PAGE = 1
    }
}
