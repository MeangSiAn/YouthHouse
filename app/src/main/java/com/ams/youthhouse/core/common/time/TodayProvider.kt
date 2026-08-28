package com.ams.youthhouse.core.common.time

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 오늘 날짜를 `YYYYMMDD`로 제공한다.
 *
 * 인터페이스로 뽑은 이유는 두 가지다.
 * - D-day 계산 테스트를 고정 날짜로 돌릴 수 있다.
 * - "모든 공고가 마감된 상태"처럼 실제 데이터로는 재현하기 어려운 화면을
 *   미래 날짜를 주입해 확인할 수 있다.
 */
fun interface TodayProvider {
    fun today(): String
}

/**
 * 공고 마감은 한국 시간 기준이다.
 * 기기 기본 타임존을 쓰면 해외에서 D-day가 하루 어긋난다.
 */
@Singleton
class SystemTodayProvider @Inject constructor() : TodayProvider {

    override fun today(): String {
        val calendar = Calendar.getInstance(KST, Locale.KOREA)
        return "%04d%02d%02d".format(
            Locale.US,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    private companion object {
        val KST: TimeZone = TimeZone.getTimeZone("Asia/Seoul")
    }
}
