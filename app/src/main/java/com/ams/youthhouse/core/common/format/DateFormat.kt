package com.ams.youthhouse.core.common.format

/**
 * `YYYYMMDD` → `YYYY.MM.DD`. 형식이 다르면 원문을 그대로 돌려준다.
 *
 * `java.time`을 쓰지 않는 이유: minSdk 24에서는 core library desugaring이 필요한데,
 * 형식이 고정 8자리 숫자라 문자열 슬라이싱으로 충분하다.
 */
fun String?.formatYearMonthDay(): String? {
    val raw = this ?: return null
    if (raw.length != 8 || !raw.all(Char::isDigit)) return raw
    return "${raw.substring(0, 4)}.${raw.substring(4, 6)}.${raw.substring(6, 8)}"
}

/** 시작일과 종료일을 `YYYY.MM.DD ~ YYYY.MM.DD`로 합친다. 한쪽만 있으면 그쪽만 표시한다. */
fun formatDateRange(beginDate: String?, endDate: String?): String? {
    val begin = beginDate.formatYearMonthDay()
    val end = endDate.formatYearMonthDay()
    return when {
        begin != null && end != null -> "$begin ~ $end"
        else -> begin ?: end
    }
}
