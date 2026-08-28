package com.ams.youthhouse.core.notice.data.paging

import androidx.paging.PagingSource
import com.ams.youthhouse.core.common.error.AppException
import com.ams.youthhouse.core.notice.data.api.NoticeApi
import com.ams.youthhouse.core.notice.data.datasource.NoticeRemoteDataSource
import com.ams.youthhouse.core.notice.data.dto.NoticeBodyDto
import com.ams.youthhouse.core.notice.data.dto.NoticeHeaderDto
import com.ams.youthhouse.core.notice.data.dto.NoticeItemDto
import com.ams.youthhouse.core.notice.data.dto.NoticeListResponseDto
import com.ams.youthhouse.core.notice.data.dto.NoticeResponseDto
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
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
    fun `첫 페이지를 로드하면 prevKey는 null이고 nextKey는 2다`() = runTest {
        val pagingSource = pagingSource(FakeNoticeApi(totalCount = 100))

        val result = pagingSource.load(refreshParams()) as PagingSource.LoadResult.Page

        assertEquals(NoticePagingSource.PAGE_SIZE, result.data.size)
        assertNull(result.prevKey)
        assertEquals(2, result.nextKey)
    }

    /** 마지막 페이지 판정은 totalCount 기준이다. */
    @Test
    fun `마지막 페이지에서 nextKey는 null이다`() = runTest {
        val pagingSource = pagingSource(FakeNoticeApi(totalCount = 40))

        val result = pagingSource.load(appendParams(pageNo = 2)) as PagingSource.LoadResult.Page

        assertEquals(1, result.prevKey)
        assertNull(result.nextKey)
    }

    @Test
    fun `item이 비면 nextKey는 null이다`() = runTest {
        val pagingSource = pagingSource(FakeNoticeApi(totalCount = 1000, itemCount = 0))

        val result = pagingSource.load(appendParams(pageNo = 5)) as PagingSource.LoadResult.Page

        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    /** 데이터 없음(03)은 오류가 아니라 정상 종료다. Error로 만들면 화면에 에러 UI가 뜬다. */
    @Test
    fun `resultCode 03이면 빈 페이지와 nextKey null을 반환한다`() = runTest {
        val pagingSource = pagingSource(FakeNoticeApi(resultCode = "03", body = null))

        val result = pagingSource.load(refreshParams()) as PagingSource.LoadResult.Page

        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    @Test
    fun `resultCode 99면 Server 오류를 반환한다`() = runTest {
        val pagingSource = pagingSource(
            FakeNoticeApi(resultCode = "99", resultMsg = "기타 에러", body = null),
        )

        val result = pagingSource.load(refreshParams()) as PagingSource.LoadResult.Error
        val error = result.throwable as AppException.Server

        assertEquals("99", error.code)
        assertEquals("기타 에러", error.serverMessage)
    }

    /** 인증키 오류는 HTTP 403 + XML 본문으로 오므로 HTTP 레벨에서 잡혀야 한다. */
    @Test
    fun `HTTP 403은 Unauthorized로 변환된다`() = runTest {
        val pagingSource = pagingSource(
            FakeNoticeApi(
                throwable = HttpException(
                    Response.error<Unit>(
                        403,
                        "<OpenAPI_ServiceResponse><cmmMsgHeader><errMsg>SERVICE_KEY_IS_NOT_REGISTERED_ERROR</errMsg></cmmMsgHeader></OpenAPI_ServiceResponse>"
                            .toResponseBody("application/xml".toMediaType()),
                    ),
                ),
            ),
        )

        val result = pagingSource.load(refreshParams()) as PagingSource.LoadResult.Error
        val error = result.throwable as AppException.Unauthorized

        assertEquals(403, error.httpCode)
    }

    @Test
    fun `IOException은 Network 오류로 변환된다`() = runTest {
        val pagingSource = pagingSource(FakeNoticeApi(throwable = IOException("offline")))

        val result = pagingSource.load(refreshParams()) as PagingSource.LoadResult.Error

        assertTrue(result.throwable is AppException.Network)
    }

    @Test
    fun `지역 필터가 brtcCode로 전달된다`() = runTest {
        val api = FakeNoticeApi()
        val pagingSource = pagingSource(api, brtcCode = NoticeRegion.GYEONGGI.code)

        pagingSource.load(refreshParams())

        assertEquals("41", api.lastBrtcCode)
    }

    /** 전체 조회일 때 brtcCode를 붙이면 안 된다 (OkHttp가 null 파라미터를 생략한다). */
    @Test
    fun `지역이 없으면 brtcCode를 보내지 않는다`() = runTest {
        val api = FakeNoticeApi()
        val pagingSource = pagingSource(api, brtcCode = null)

        pagingSource.load(refreshParams())

        assertNull(api.lastBrtcCode)
    }

    /** 응답 언랩은 NoticeRemoteDataSource가 하므로 fake api를 그걸로 감싼다. */
    private fun pagingSource(
        api: FakeNoticeApi,
        category: NoticeCategory = NoticeCategory.RENTAL,
        brtcCode: String? = null,
    ) = NoticePagingSource(NoticeRemoteDataSource(api), category, brtcCode)

    @Test
    fun `분야에 따라 다른 엔드포인트를 호출한다`() = runTest {
        val rentalApi = FakeNoticeApi()
        pagingSource(rentalApi, category = NoticeCategory.RENTAL).load(refreshParams())
        assertEquals(NoticeCategory.RENTAL, rentalApi.lastCategory)

        val saleApi = FakeNoticeApi()
        pagingSource(saleApi, category = NoticeCategory.SALE).load(refreshParams())
        assertEquals(NoticeCategory.SALE, saleApi.lastCategory)
    }

    private fun refreshParams() = PagingSource.LoadParams.Refresh<Int>(
        key = null,
        loadSize = NoticePagingSource.PAGE_SIZE,
        placeholdersEnabled = false,
    )

    private fun appendParams(pageNo: Int) = PagingSource.LoadParams.Append(
        key = pageNo,
        loadSize = NoticePagingSource.PAGE_SIZE,
        placeholdersEnabled = false,
    )
}

/**
 * 손으로 쓴 fake. 목킹 라이브러리를 들이지 않는다 —
 * 인터페이스가 메서드 하나뿐이라 fake가 더 짧고 의도가 분명하다.
 */
private class FakeNoticeApi(
    private val totalCount: Int = 100,
    private val itemCount: Int = NoticePagingSource.PAGE_SIZE,
    private val resultCode: String = "00",
    private val resultMsg: String = "NORMAL SERVICE",
    private val body: NoticeBodyDto? = NoticeBodyDto(),
    private val throwable: Throwable? = null,
) : NoticeApi {

    /** 마지막 호출에 실린 brtcCode. 필터가 실제로 전달되는지 검증하는 용도. */
    var lastBrtcCode: String? = null
        private set

    /** 마지막으로 호출된 오퍼레이션. 분야에 따라 엔드포인트가 갈리는지 검증한다. */
    var lastCategory: NoticeCategory? = null
        private set

    override suspend fun getRentalNoticeList(pageNo: Int, numOfRows: Int, brtcCode: String?) =
        respond(NoticeCategory.RENTAL, pageNo, numOfRows, brtcCode)

    override suspend fun getSaleNoticeList(pageNo: Int, numOfRows: Int, brtcCode: String?) =
        respond(NoticeCategory.SALE, pageNo, numOfRows, brtcCode)

    private fun respond(
        category: NoticeCategory,
        pageNo: Int,
        numOfRows: Int,
        brtcCode: String?,
    ): NoticeListResponseDto {
        lastCategory = category
        lastBrtcCode = brtcCode
        throwable?.let { throw it }

        return NoticeListResponseDto(
            response = NoticeResponseDto(
                header = NoticeHeaderDto(resultCode = resultCode, resultMsg = resultMsg),
                body = body?.copy(
                    totalCount = totalCount.toString(),
                    numOfRows = numOfRows.toString(),
                    pageNo = pageNo.toString(),
                    item = List(itemCount) { index ->
                        NoticeItemDto(
                            pblancId = "${pageNo}_$index",
                            pblancNm = "공고 $pageNo-$index",
                        )
                    },
                ),
            ),
        )
    }
}
