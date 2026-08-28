package com.ams.youthhouse.core.notice.data.datasource

import com.ams.youthhouse.core.common.error.AppException
import com.ams.youthhouse.core.network.safeApiCall
import com.ams.youthhouse.core.notice.data.api.NoticeApi
import com.ams.youthhouse.core.notice.data.mapper.toDomain
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import javax.inject.Inject
import javax.inject.Singleton

data class NoticePage(
    val notices: List<Notice>,
    val totalCount: Int,
)

/**
 * 응답 껍데기(`resultCode` 분기, body 없음 처리)를 한 곳에서 벗긴다.
 *
 * 임대·분양 두 오퍼레이션이 응답 규약을 공유하므로 언랩 로직도 공유한다.
 * 분야에 따라 호출할 엔드포인트만 달라진다.
 */
@Singleton
class NoticeRemoteDataSource @Inject constructor(
    private val noticeApi: NoticeApi,
) {

    suspend fun fetchNotices(
        category: NoticeCategory,
        pageNo: Int,
        numOfRows: Int,
        brtcCode: String?,
    ): NoticePage {
        val response = safeApiCall {
            when (category) {
                NoticeCategory.RENTAL -> noticeApi.getRentalNoticeList(pageNo, numOfRows, brtcCode)
                NoticeCategory.SALE -> noticeApi.getSaleNoticeList(pageNo, numOfRows, brtcCode)
            }
        }.response

        return when (response.header.resultCode) {
            RESULT_CODE_SUCCESS -> NoticePage(
                notices = response.body?.item.orEmpty().map { it.toDomain(category) },
                totalCount = response.body?.totalCount?.toIntOrNull() ?: 0,
            )

            // 데이터 없음은 오류가 아니라 결과가 0건인 것이다. body 키 자체가 없다.
            // 분양은 서울·대전·세종·제주가 실제로 0건이라 흔히 지나가는 경로다.
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
