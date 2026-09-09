package com.ams.youthhouse.core.notice.data.paging

import androidx.paging.PagingSource
import com.ams.youthhouse.core.common.error.AppException
import com.ams.youthhouse.core.notice.data.api.MosstisNoticeApi
import com.ams.youthhouse.core.notice.data.datasource.NoticeRemoteDataSource
import com.ams.youthhouse.core.notice.data.dto.NoticeItemDto
import com.ams.youthhouse.core.notice.data.dto.NoticeListResponseDto
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NoticePagingSourceTest {

    @Test
    fun `첫 페이지의 prevKey는 null이고 nextKey는 다음 offset이다`() = runTest {
        val pagingSource = pagingSource(FakeNoticeApi(total = 100))

        val result = pagingSource.load(refreshParams()) as PagingSource.LoadResult.Page

        assertEquals(NoticePagingSource.PAGE_SIZE, result.data.size)
        assertNull(result.prevKey)
        assertEquals(NoticePagingSource.PAGE_SIZE, result.nextKey)
    }

    /** 마지막 페이지 판정은 total 기준이다. */
    @Test
    fun `마지막 페이지에서 nextKey는 null이다`() = runTest {
        val pagingSource = pagingSource(FakeNoticeApi(total = 40))

        val result = pagingSource.load(appendParams(offset = 20)) as PagingSource.LoadResult.Page

        assertEquals(0, result.prevKey)
        assertNull(result.nextKey)
    }

    @Test
    fun `결과가 비면 nextKey는 null이다`() = runTest {
        val pagingSource = pagingSource(FakeNoticeApi(total = 1000, itemCount = 0))

        val result = pagingSource.load(appendParams(offset = 100)) as PagingSource.LoadResult.Page

        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    /** 서버에 보내는 건 페이지 번호가 아니라 offset이다. 어긋나면 항목이 겹치거나 건너뛴다. */
    @Test
    fun `요청 offset이 그대로 서버로 나간다`() = runTest {
        val api = FakeNoticeApi(total = 100)
        val pagingSource = pagingSource(api)

        pagingSource.load(appendParams(offset = 40))

        assertEquals(40, api.lastOffset)
        assertEquals(NoticePagingSource.PAGE_SIZE, api.lastLimit)
    }

    @Test
    fun `필터는 서버 쿼리로 나간다`() = runTest {
        val api = FakeNoticeApi(total = 10)
        val pagingSource = pagingSource(
            api = api,
            category = NoticeCategory.SALE,
            region = NoticeRegion.GYEONGGI,
            status = NoticeStatusFilter.UPCOMING,
        )

        pagingSource.load(refreshParams())

        assertEquals("공공분양", api.lastCategory)
        assertEquals("경기도", api.lastSido)
        assertEquals("upcoming", api.lastStatus)
    }

    /** 전체 지역은 파라미터를 아예 보내지 않는다. */
    @Test
    fun `지역이 없으면 sido를 보내지 않는다`() = runTest {
        val api = FakeNoticeApi(total = 10)

        pagingSource(api, region = null).load(refreshParams())

        assertNull(api.lastSido)
    }

    @Test
    fun `네트워크 오류는 Error로 돌아온다`() = runTest {
        val pagingSource = pagingSource(FakeNoticeApi(error = IOException("offline")))

        val result = pagingSource.load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
        assertTrue((result as PagingSource.LoadResult.Error).throwable is AppException.Network)
    }

    /** 백엔드 인증 실패(401)는 화면 문구가 갈리도록 도메인 예외로 정규화돼야 한다. */
    @Test
    fun `인증 실패는 Unauthorized로 정규화된다`() = runTest {
        val unauthorized = HttpException(
            Response.error<Unit>(401, "".toResponseBody("application/json".toMediaType())),
        )
        val pagingSource = pagingSource(FakeNoticeApi(error = unauthorized))

        val result = pagingSource.load(refreshParams())

        val throwable = (result as PagingSource.LoadResult.Error).throwable
        assertTrue(throwable is AppException.Unauthorized)
    }

    private fun pagingSource(
        api: FakeNoticeApi,
        category: NoticeCategory = NoticeCategory.RENTAL,
        region: NoticeRegion? = null,
        status: NoticeStatusFilter = NoticeStatusFilter.ALL,
    ) = NoticePagingSource(NoticeRemoteDataSource(api), category, region, status)

    private fun refreshParams() = PagingSource.LoadParams.Refresh<Int>(
        key = null,
        loadSize = NoticePagingSource.PAGE_SIZE,
        placeholdersEnabled = false,
    )

    private fun appendParams(offset: Int) = PagingSource.LoadParams.Append(
        key = offset,
        loadSize = NoticePagingSource.PAGE_SIZE,
        placeholdersEnabled = false,
    )
}

private class FakeNoticeApi(
    private val total: Int = 0,
    private val itemCount: Int = NoticePagingSource.PAGE_SIZE,
    private val error: Throwable? = null,
) : MosstisNoticeApi {

    var lastCategory: String? = null
    var lastSido: String? = null
    var lastStatus: String? = null
    var lastLimit: Int? = null
    var lastOffset: Int? = null

    override suspend fun getNotices(
        category: String,
        sido: String?,
        status: String,
        limit: Int,
        offset: Int,
    ): NoticeListResponseDto {
        lastCategory = category
        lastSido = sido
        lastStatus = status
        lastLimit = limit
        lastOffset = offset
        error?.let { throw it }

        return NoticeListResponseDto(
            total = total,
            limit = limit,
            offset = offset,
            results = List(itemCount) { index ->
                NoticeItemDto(noticeId = "id-${offset + index}", title = "공고 $index")
            },
        )
    }

    override suspend fun getNotice(noticeId: String): NoticeItemDto {
        error?.let { throw it }
        return NoticeItemDto(noticeId = noticeId)
    }
}
