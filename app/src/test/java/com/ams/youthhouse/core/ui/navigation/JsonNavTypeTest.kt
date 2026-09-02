package com.ams.youthhouse.core.ui.navigation

import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.presentation.model.NoticeScheduleStage
import com.ams.youthhouse.core.notice.presentation.model.NoticeStatus
import com.ams.youthhouse.core.notice.presentation.model.ScheduleStageKind
import com.ams.youthhouse.core.notice.presentation.model.ScheduleStageState
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.notice.presentation.model.previewNotice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 상세 화면이 모델을 통째로 Navigation 인자로 받으므로, 이 왕복 변환이 깨지면 상세 진입이 죽는다.
 *
 * okio Base64를 쓰기 때문에 Robolectric 없이 순수 JVM에서 돈다.
 */
class JsonNavTypeTest {

    private val navType = JsonNavType(NoticeUiModel.serializer())

    @Test
    fun `한글과 특수문자를 포함한 모델이 왕복 변환된다`() {
        val original = sampleNotice()

        val restored = navType.parseValue(navType.serializeAsValue(original))

        assertEquals(original, restored)
    }

    @Test
    fun `null 필드가 있는 모델이 왕복 변환된다`() {
        val original = sampleNotice().copy(
            statusName = null,
            complexName = null,
            deposit = null,
            contact = null,
            detailUrl = null,
        )

        val restored = navType.parseValue(navType.serializeAsValue(original))

        assertEquals(original, restored)
    }

    /** 라우트 문자열에 들어가므로 URI에 안전한 문자만 나와야 한다. */
    @Test
    fun `인코딩 결과는 URL 안전 문자만 포함한다`() {
        val encoded = navType.serializeAsValue(sampleNotice())

        assertTrue("URL 안전하지 않은 문자 포함: $encoded", encoded.matches(Regex("^[A-Za-z0-9_-]+$")))
    }

    private fun sampleNotice() = NoticeUiModel(
        // 실데이터 원형까지 함께 실려 왕복돼야 한다 — 찜이 source에 의존한다.
        source = previewNotice(),
        category = NoticeCategory.RENTAL,
        title = "[울산권] 2026년 매입임대주택 입주자 모집 공고(1순위일반 & 2순위) #특수/문자?=+",
        status = NoticeStatus.URGENT,
        statusLabel = "D-2",
        statusName = "일반공고",
        supplyTypeName = "매입임대",
        houseTypeName = "다가구주택",
        supplyInstitutionName = "LH",
        complexName = "행복주택 1단지",
        regionName = "울산광역시 중구",
        fullAddress = "울산광역시 중구 종가로 1",
        noticeDate = "2026.07.09",
        applyPeriod = "2026.08.10 ~ 2026.08.11",
        winnerAnnounceDate = "2026.10.02",
        deposit = "12,000,000",
        monthlyRent = "150,000",
        downPayment = "1,200,000",
        interimPayment = "2,400,000",
        balance = "8,400,000",
        totalHouseholdCount = "120",
        supplyCount = "3",
        heatingMethodName = "개별난방",
        contact = "LH 콜센터 : 1600-1004 (평일 : 09:00 ~ 18:00)",
        detailUrl = "https://m.myhome.go.kr/hws/portal/sch/selectRsdtRcritNtcDetailView.do?pblancId=20955",
        applyUrl = "https://apply.lh.or.kr/lhapply/apply/wt/wrtanc/selectWrtancInfo.do?panId=2015122300020502",
        scheduleStages = listOf(
            NoticeScheduleStage(ScheduleStageKind.ANNOUNCED, ScheduleStageState.DONE, "2026.07.09"),
            NoticeScheduleStage(
                kind = ScheduleStageKind.APPLY,
                state = ScheduleStageState.CURRENT,
                dateText = "2026.08.10 ~ 2026.08.11",
            ),
        ),
    )
}
