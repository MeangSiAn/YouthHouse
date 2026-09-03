package com.ams.youthhouse.core.common.format

import org.junit.Assert.assertEquals
import org.junit.Test

class EokManFormatTest {

    @Test
    fun `억과 만이 섞이면 둘 다 표기한다`() {
        assertEquals("12억 1,500", 121_500L.formatManwonAsEokMan())
    }

    @Test
    fun `만 단위가 0이면 억만 표기한다`() {
        assertEquals("12억", 120_000L.formatManwonAsEokMan())
    }

    @Test
    fun `1억 미만은 만 접미사를 붙인다`() {
        assertEquals("8,700만", 8_700L.formatManwonAsEokMan())
    }

    @Test
    fun `0도 만 접미사로 표기한다`() {
        assertEquals("0만", 0L.formatManwonAsEokMan())
    }

    @Test
    fun `음수는 부호를 앞에 둔다 - 증감 표기용`() {
        assertEquals("-800만", (-800L).formatManwonAsEokMan())
        assertEquals("-1억 500", (-10_500L).formatManwonAsEokMan())
    }
}
