package com.ams.youthhouse.feature.home.presentation.guide

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.ams.youthhouse.R

/**
 * 기획서 "알아두면 좋은 것" — 앱에 묶어 배포하는 짧은 안내 글.
 *
 * 서버가 없으니 리소스가 곧 콘텐츠다. 글마다 도입 한 문단 + 번호 붙은 항목 몇 개로
 * 형식을 고정해서, 새 글을 추가할 때 화면 코드를 건드리지 않게 한다.
 *
 * 항목 제목과 본문은 같은 길이의 배열 두 개로 쪼개 둔다 — 한 문자열에 구분자를 넣어
 * 자르는 방식은 번역·수정 중 구분자가 깨지면 조용히 어긋난다.
 */
enum class HomeGuide(
    val emoji: String,
    @param:StringRes val tagRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val leadRes: Int,
    @param:ArrayRes val pointTitlesRes: Int,
    @param:ArrayRes val pointBodiesRes: Int,
) {
    RENTAL_TYPES(
        emoji = "📄",
        tagRes = R.string.guide_tag_basics,
        titleRes = R.string.guide_rental_types_title,
        leadRes = R.string.guide_rental_types_lead,
        pointTitlesRes = R.array.guide_rental_types_point_titles,
        pointBodiesRes = R.array.guide_rental_types_point_bodies,
    ),
    VISIT_CHECKLIST(
        emoji = "🔍",
        tagRes = R.string.guide_tag_visit,
        titleRes = R.string.guide_visit_checklist_title,
        leadRes = R.string.guide_visit_checklist_lead,
        pointTitlesRes = R.array.guide_visit_checklist_point_titles,
        pointBodiesRes = R.array.guide_visit_checklist_point_bodies,
    ),
    DOCUMENTS(
        emoji = "📋",
        tagRes = R.string.guide_tag_prepare,
        titleRes = R.string.guide_documents_title,
        leadRes = R.string.guide_documents_lead,
        pointTitlesRes = R.array.guide_documents_point_titles,
        pointBodiesRes = R.array.guide_documents_point_bodies,
    ),
    ;

    companion object {
        /** 라우트 인자로 온 이름. 모르는 값이면 첫 글로 — 빈 화면보다 낫다. */
        fun fromName(name: String): HomeGuide = entries.firstOrNull { it.name == name } ?: entries.first()
    }
}
