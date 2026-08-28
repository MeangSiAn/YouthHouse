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
