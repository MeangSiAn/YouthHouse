package com.ams.youthhouse.core.notice.domain.model

/**
 * 모집공고의 분야.
 *
 * 같은 서비스의 서로 다른 오퍼레이션에서 오고, 한 화면에 섞여 보이므로
 * "이게 임대인지 분양인지"는 도메인 사실이다.
 *
 * 응답 필드는 [SALE]이 [RENTAL]의 부분집합이다(분양 전용 필드 없음).
 * 분양에는 임대보증금·월임대료·공급유형명이 아예 오지 않아 매퍼에서 `null`이 된다.
 */
enum class NoticeCategory {
    /** 공공임대 — 보증금과 월임대료를 내고 거주한다. */
    RENTAL,

    /** 공공분양 — 계약금·중도금·잔금을 치르고 소유권을 취득한다. */
    SALE,
}
