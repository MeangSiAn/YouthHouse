package com.ams.youthhouse.core.backup.data

import com.ams.youthhouse.core.backup.domain.model.BackupPayload
import com.ams.youthhouse.core.backup.domain.model.BackupSettings
import com.ams.youthhouse.core.backup.domain.repository.BackupFormatException
import com.ams.youthhouse.core.complex.domain.model.ComplexSnapshot
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.FavoriteComplex
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeAddress
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticePeriod
import com.ams.youthhouse.core.notice.domain.model.NoticePrice
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {

    @Test
    fun `내보낸 것을 다시 읽으면 그대로다`() {
        val payload = payload()

        val restored = BackupCodec.decode(BackupCodec.encode(payload, exportedAtMillis = 1L))

        assertEquals(payload, restored)
    }

    @Test
    fun `파일에는 iOS와 약속한 키 이름이 쓰인다`() {
        val text = BackupCodec.encode(payload(), exportedAtMillis = 1L)

        listOf(
            "\"app\": \"youthhouse\"",
            "\"format_version\": 1",
            "\"site_visit_notes\"",
            "\"favorite_notices\"",
            "\"favorite_complexes\"",
            "\"kapt_code\"",
            "\"visited_on\"",
            "\"scores\"",
            "\"region_code\"",
        ).forEach { key -> assertTrue("missing $key", key in text) }
    }

    @Test
    fun `다른 앱의 파일은 거부한다`() {
        assertThrows(BackupFormatException::class.java) {
            BackupCodec.decode("""{"app":"other","format_version":1,"exported_at":1}""")
        }
    }

    @Test
    fun `JSON이 아니면 형식 예외다`() {
        assertThrows(BackupFormatException::class.java) { BackupCodec.decode("not json") }
    }

    @Test
    fun `더 새 버전 파일은 거부한다`() {
        assertThrows(BackupFormatException::class.java) {
            BackupCodec.decode("""{"app":"youthhouse","format_version":99,"exported_at":1}""")
        }
    }

    @Test
    fun `모르는 키와 모르는 enum 이름은 관대하게 읽는다`() {
        val text = """
            {"app":"youthhouse","format_version":1,"exported_at":1,"future_key":true,
             "site_visit_notes":[{"kapt_code":"A1","complex_name":"단지","visited_on":"20260901",
               "scores":{"LIGHT":9,"UNKNOWN":3},"elevator":"WEIRD","defect":null,"extra":1}]}
        """.trimIndent()

        val payload = BackupCodec.decode(text)

        val note = payload.siteVisitNotes.single()
        assertEquals(VisitRatings.MAX_SCORE, note.ratings[VisitCriterion.LIGHT])
        assertEquals(1, note.ratings.scores.size)
        assertEquals(ElevatorCondition.UNCHECKED, note.elevatorCondition)
        assertEquals(DefectStatus.UNCHECKED, note.defectStatus)
        assertEquals(ComplexSnapshot.EMPTY, note.snapshot)
        assertNull(payload.settings)
    }

    @Test
    fun `설정은 코드와 이름으로 저장되고 모르는 값은 기본값이다`() {
        val text = """
            {"app":"youthhouse","format_version":1,"exported_at":1,
             "settings":{"region_code":"99","category":"NOPE","status_filter":"NOPE"}}
        """.trimIndent()

        val settings = BackupCodec.decode(text).settings!!

        assertNull(settings.region)
        assertEquals(NoticeCategory.RENTAL, settings.category)
        assertEquals(NoticeStatusFilter.Default, settings.statusFilter)
    }

    private fun payload() = BackupPayload(
        authorToken = "token-1",
        settings = BackupSettings(
            region = NoticeRegion.SEOUL,
            category = NoticeCategory.SALE,
            statusFilter = NoticeStatusFilter.ALL,
        ),
        favoriteNotices = listOf(notice("n1")),
        favoriteComplexes = listOf(FavoriteComplex("A15105302", "관악푸르지오", "서울특별시 관악구")),
        siteVisitNotes = listOf(
            SiteVisitNote(
                kaptCode = "A15105302",
                complexName = "관악푸르지오",
                regionLabel = "서울특별시 관악구 봉천동",
                visitedOn = "20260720",
                viewedUnit = "84㎡ · 12층",
                ratings = VisitRatings(mapOf(VisitCriterion.LIGHT to 4, VisitCriterion.PARKING to 2)),
                walkToStationMinutes = 8,
                elevatorCondition = ElevatorCondition.CROWDED,
                defectStatus = DefectStatus.NONE,
                memo = "남향 채광 좋음",
                snapshot = ComplexSnapshot("2004", 2104, "2호선 · 서울대입구역", 84.9, 121_500),
                updatedAtMillis = 1_700_000_000_000,
            ),
        ),
    )

    private fun notice(noticeId: String) = Notice(
        noticeId = noticeId,
        category = NoticeCategory.RENTAL,
        pblancId = "21096",
        houseSn = 3,
        title = "공고 $noticeId",
        statusName = "일반공고",
        supplyInstitutionName = "LH",
        houseTypeName = "아파트",
        supplyTypeName = null,
        previousNoticeId = null,
        complexName = "단지",
        address = NoticeAddress("울산광역시", "울주군", "울산 울주군 1", null, null, "pnu"),
        period = NoticePeriod("20260814", "20260818", "20261231", "20270131"),
        price = NoticePrice(null, 6_000_000, 56_588_000, 115_180_000, null),
        heatingMethodName = "개별난방",
        totalHouseholdCount = 362,
        supplyCount = 362,
        supplyHouseCount = null,
        contact = "1600-1004",
        noticeUrl = "https://apply.lh.or.kr",
        pcUrl = null,
        mobileUrl = null,
    )
}
