package com.ams.youthhouse.core.notice.domain.model

/**
 * 모집공고 조회의 광역시도 필터.
 *
 * [code]는 API의 `brtcCode` 파라미터 값이다.
 * **행정표준코드와 일치하지 않는 값이 섞여 있어 실제 호출로 하나씩 확인한 목록이다.**
 * 특히 [JEONNAM_GWANGJU]의 `12`는 표준 코드 체계에 없는 값이라 추측으로는 찾을 수 없다.
 * (광주 `29`, 전남 `46`은 이 API에서 아무것도 돌려주지 않는다.)
 *
 * [regionName]은 API가 `brtcNm`으로 돌려주는 공식 명칭 그대로다.
 * UI 문구가 아니라 데이터이므로 문자열 리소스로 빼지 않는다.
 *
 * 필터를 걸지 않는 "전체"는 이 enum이 아니라 `null`로 표현한다.
 */
enum class NoticeRegion(
    val code: String,
    val regionName: String,
) {
    SEOUL("11", "서울특별시"),
    JEONNAM_GWANGJU("12", "전남광주통합특별시"),
    BUSAN("26", "부산광역시"),
    DAEGU("27", "대구광역시"),
    INCHEON("28", "인천광역시"),
    DAEJEON("30", "대전광역시"),
    ULSAN("31", "울산광역시"),
    SEJONG("36", "세종특별자치시"),
    GYEONGGI("41", "경기도"),
    CHUNGBUK("43", "충청북도"),
    CHUNGNAM("44", "충청남도"),
    GYEONGBUK("47", "경상북도"),
    GYEONGNAM("48", "경상남도"),
    JEJU("50", "제주특별자치도"),
    GANGWON("51", "강원특별자치도"),
    JEONBUK("52", "전북특별자치도"),
    ;

    companion object {

        /**
         * 저장된 코드를 되돌린다.
         *
         * 모르는 코드(API가 코드 체계를 바꾼 경우 등)는 `null`(전체)로 폴백한다.
         * 예외를 던지면 저장값 하나 때문에 앱이 시작하지 못한다.
         */
        fun fromCode(code: String?): NoticeRegion? =
            code?.let { value -> entries.firstOrNull { it.code == value } }
    }
}
