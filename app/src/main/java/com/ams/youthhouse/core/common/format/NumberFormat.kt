package com.ams.youthhouse.core.common.format

import java.util.Locale

/**
 * 천 단위 구분자를 넣는다. `1200000` → `1,200,000`
 *
 * 단위(원, 세대 등)는 붙이지 않는다. 지역화를 위해 presentation의 문자열 리소스가 붙인다.
 * 구분자는 로케일에 상관없이 `,`로 고정한다 — 금액을 나중에 다시 파싱할 여지를 남기기 위함이다.
 */
fun Int?.formatThousands(): String? = this?.let { value ->
    String.format(Locale.US, "%,d", value)
}

/**
 * **만원 단위** 금액을 부동산 관용 표기로 바꾼다. 실거래가 API가 만원 단위 정수를 준다.
 *
 * - `121500` → `12억 1,500`
 * - `120000` → `12억`
 * - `8700` → `8,700만`
 * - `0` → `0만`, 음수는 부호를 앞에 붙인다(증감 표기용)
 */
fun Long.formatManwonAsEokMan(): String {
    val sign = if (this < 0) "-" else ""
    val value = kotlin.math.abs(this)
    val eok = value / MAN_PER_EOK
    val man = value % MAN_PER_EOK

    return when {
        eok == 0L -> "$sign${String.format(Locale.US, "%,d", man)}만"
        man == 0L -> "$sign${eok}억"
        else -> "$sign${eok}억 ${String.format(Locale.US, "%,d", man)}"
    }
}

private const val MAN_PER_EOK = 10_000L
