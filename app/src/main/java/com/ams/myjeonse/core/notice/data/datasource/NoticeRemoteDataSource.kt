package com.ams.myjeonse.core.notice.data.datasource

import com.ams.myjeonse.core.common.error.AppException
import com.ams.myjeonse.core.network.safeApiCall
import com.ams.myjeonse.core.notice.data.api.NoticeApi
import com.ams.myjeonse.core.notice.data.mapper.toDomain
import com.ams.myjeonse.core.notice.domain.model.Notice
import javax.inject.Inject
import javax.inject.Singleton

data class NoticePage(
    val notices: List<Notice>,
    val totalCount: Int,
)

/**
 * 응답 껍데기(`resultCode` 분기, body 없음 처리)를 한 곳에서 벗긴다.
 *
 * 페이징과 홈 요약이 같은 엔드포인트를 다르게 쓰지만 언랩 규칙은 동일하므로 공유한다.
 */
@Singleton
class NoticeRemoteDataSource @Inject constructor(
    private val noticeApi: NoticeApi,
) {

    suspend fun fetchNotices(
        pageNo: Int,
        numOfRows: Int,
        brtcCode: String?,
    ): NoticePage {
        val response = safeApiCall {
            noticeApi.getNoticeList(
                pageNo = pageNo,
                numOfRows = numOfRows,
                brtcCode = brtcCode,
            )
        }.response

        return when (response.header.resultCode) {
            RESULT_CODE_SUCCESS -> NoticePage(
                notices = response.body?.item.orEmpty().map { it.toDomain() },
                totalCount = response.body?.totalCount?.toIntOrNull() ?: 0,
            )

            // 데이터 없음은 오류가 아니라 결과가 0건인 것이다. body 키 자체가 없다.
            RESULT_CODE_NO_DATA -> NoticePage(notices = emptyList(), totalCount = 0)

            else -> throw AppException.Server(
                code = response.header.resultCode,
                serverMessage = response.header.resultMsg,
            )
        }
    }

    private companion object {
        const val RESULT_CODE_SUCCESS = "00"
        const val RESULT_CODE_NO_DATA = "03"
    }
}
