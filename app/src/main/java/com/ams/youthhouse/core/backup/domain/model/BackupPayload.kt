package com.ams.youthhouse.core.backup.domain.model

import com.ams.youthhouse.core.complex.domain.model.FavoriteComplex
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter

/**
 * 백업 파일 한 개에 담기는 것 전부.
 *
 * 임장노트는 사용자가 발로 뛰어 만든 기록이라 사라지면 끝이다. 공고·실거래는 서버에서
 * 다시 받으면 되지만 노트는 그렇지 않다. 서버를 두지 않기로 했으므로 사용자가 파일을
 * 직접 보관한다 — 드라이브든 메신저든.
 *
 * [authorToken]은 지금은 쓰이지 않는다. 나중에 노트를 공개로 전환할 때, 기기를 바꿔도
 * 같은 사람의 노트로 인식하기 위한 이전 경로다. 백업에 실어 두면 그때 가서 새로 만들
 * 필요가 없다.
 */
data class BackupPayload(
    val authorToken: String?,
    val settings: BackupSettings?,
    val favoriteNotices: List<Notice>,
    val favoriteComplexes: List<FavoriteComplex>,
    val siteVisitNotes: List<SiteVisitNote>,
)

data class BackupSettings(
    val region: NoticeRegion?,
    val category: NoticeCategory,
    val statusFilter: NoticeStatusFilter,
)

/** 내보내거나 불러온 건수. 화면이 "노트 4건 · 찜 3건"으로 알린다. */
data class BackupSummary(
    val noteCount: Int,
    val favoriteNoticeCount: Int,
    val favoriteComplexCount: Int,
)
