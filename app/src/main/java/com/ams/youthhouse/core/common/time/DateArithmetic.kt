package com.ams.youthhouse.core.common.time

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * `YYYYMMDD` 두 날짜 사이의 일수. [from]이 [to]보다 나중이면 음수.
 *
 * `YYYYMMDD` 문자열은 사전순 비교로 선후를 알 수 있지만 **며칠 차이인지는 알 수 없다.**
 * D-day 숫자를 그리려면 이 계산이 필요하다.
 *
 * minSdk 24에서 `java.time`은 core library desugaring이 필요하므로 [Calendar]를 쓴다.
 * UTC 자정으로 정규화해 서머타임 영향을 없앤다.
 *
 * @return 형식이 `YYYYMMDD` 8자리 숫자가 아니면 `null`
 */
fun daysBetween(from: String?, to: String?): Int? {
    val fromMillis = from.toUtcMidnightMillis() ?: return null
    val toMillis = to.toUtcMidnightMillis() ?: return null
    return ((toMillis - fromMillis) / MILLIS_PER_DAY).toInt()
}

private fun String?.toUtcMidnightMillis(): Long? {
    val raw = this ?: return null
    if (raw.length != 8 || !raw.all(Char::isDigit)) return null

    val year = raw.substring(0, 4).toInt()
    val month = raw.substring(4, 6).toInt()
    val day = raw.substring(6, 8).toInt()
    if (month !in 1..12 || day !in 1..31) return null

    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US).apply {
        isLenient = false
        clear()
        set(year, month - 1, day)
    }
    return runCatching { calendar.timeInMillis }.getOrNull()
}

private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
