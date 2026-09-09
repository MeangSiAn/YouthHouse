package com.ams.youthhouse.core.notice.data.datasource

import com.ams.youthhouse.core.network.safeApiCall
import com.ams.youthhouse.core.notice.data.api.MosstisNoticeApi
import com.ams.youthhouse.core.notice.data.mapper.toDomain
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import javax.inject.Inject
import javax.inject.Singleton

data class NoticePage(
    val notices: List<Notice>,
    val totalCount: Int,
)

/**
 * 도메인 타입을 서버 쿼리 문자열로 바꾸고, 응답을 도메인으로 되돌린다.
 *
 * data.go.kr을 직접 부르던 시절에는 여기서 응답 껍데기(`resultCode` 분기, `body` 없음,
 * "데이터 없음"을 오류가 아닌 0건으로 해석)를 벗겨야 했다. 백엔드는 0건을 빈 배열로 주고
 * 오류는 HTTP 상태로 알리므로 그 분기가 통째로 사라졌다.
 *
 * 대신 HTTP 오류를 [safeApiCall]로 도메인 예외로 바꾼다. 화면의 에러 문구가
 * `AppException` 종류로 갈리기 때문이다.
 */
@Singleton
class NoticeRemoteDataSource @Inject constructor(
    private val noticeApi: MosstisNoticeApi,
) {

    suspend fun fetchNotices(
        category: NoticeCategory,
        region: NoticeRegion?,
        status: NoticeStatusFilter,
        limit: Int,
        offset: Int,
    ): NoticePage {
        val response = safeApiCall {
            noticeApi.getNotices(
                category = category.toQueryValue(),
                // 코드가 아니라 이름으로 거른다. 백엔드가 돌려주는 sido 값이
                // NoticeRegion.regionName과 정확히 같아 변환표가 필요 없다.
                sido = region?.regionName,
                status = status.toQueryValue(),
                limit = limit,
                offset = offset,
            )
        }

        return NoticePage(
            notices = response.results.map { it.toDomain() },
            totalCount = response.total,
        )
    }

    /** 단건 조회. 목록에 없던 필드를 채우는 용도라 실패는 호출부가 무시할 수 있어야 한다. */
    suspend fun fetchNotice(noticeId: String): Notice =
        safeApiCall { noticeApi.getNotice(noticeId) }.toDomain()
}

private fun NoticeCategory.toQueryValue(): String = when (this) {
    NoticeCategory.RENTAL -> "공공임대"
    NoticeCategory.SALE -> "공공분양"
}

private fun NoticeStatusFilter.toQueryValue(): String = when (this) {
    NoticeStatusFilter.OPEN -> "open"
    NoticeStatusFilter.UPCOMING -> "upcoming"
    NoticeStatusFilter.ALL -> "all"
}
