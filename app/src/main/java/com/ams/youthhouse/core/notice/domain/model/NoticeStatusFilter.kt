package com.ams.youthhouse.core.notice.domain.model

/**
 * 접수 상태로 목록을 좁히는 필터.
 *
 * **서버가 아니라 앱이 거른다.** 이 API에는 상태 파라미터도 정렬 파라미터도 없고,
 * 상태는 `beginDate`/`endDate`와 오늘을 비교해야 나오는 파생값이다.
 * 그래서 페이지를 받아 온 뒤 `PagingData`에서 걸러낸다.
 *
 * 걸러낸 결과가 한 페이지에 몇 건 남지 않을 수 있지만, Paging이 다음 페이지를
 * 이어서 불러오므로 스크롤은 그대로 이어진다.
 */
enum class NoticeStatusFilter {
    /** 지금 접수 중. 마감 임박(D-3 이내)도 여기 포함된다. */
    OPEN,

    /** 아직 시작하지 않은 공고. */
    UPCOMING,

    /** 거르지 않는다. 마감된 공고까지 그대로 보여준다. */
    ALL,
    ;

    companion object {

        /** 저장된 값이 없거나 모르는 값이면 접수중. 목록을 열었을 때 가장 자주 찾는 상태다. */
        val Default: NoticeStatusFilter = OPEN

        fun fromName(name: String?): NoticeStatusFilter =
            entries.firstOrNull { it.name == name } ?: Default
    }
}
