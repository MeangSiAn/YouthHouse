package com.ams.youthhouse.core.complex.data.repository

import com.ams.youthhouse.core.complex.data.local.SiteVisitNoteDao
import com.ams.youthhouse.core.complex.data.local.SiteVisitNoteEntity
import com.ams.youthhouse.core.complex.domain.model.ComplexSnapshot
import com.ams.youthhouse.core.complex.domain.model.DefectStatus
import com.ams.youthhouse.core.complex.domain.model.ElevatorCondition
import com.ams.youthhouse.core.complex.domain.model.SiteVisitNote
import com.ams.youthhouse.core.complex.domain.model.VisitCriterion
import com.ams.youthhouse.core.complex.domain.model.VisitRatings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteVisitNoteRepositoryImplTest {

    private val dao = FakeSiteVisitNoteDao()
    private val repository = SiteVisitNoteRepositoryImpl(dao)

    @Test
    fun `저장한 노트가 도메인 모델로 온전히 복원된다`() = runTest {
        val note = note("A1")

        repository.save(note)

        val restored = repository.observe("A1").first()!!
        // 수정 시각은 저장소가 찍으므로 그것만 빼고 비교한다.
        assertEquals(note, restored.copy(updatedAtMillis = 0L))
    }

    @Test
    fun `같은 단지를 다시 저장하면 덮어쓴다`() = runTest {
        repository.save(note("A1", memo = "첫 방문"))
        repository.save(note("A1", memo = "두 번째 방문"))

        val notes = repository.notes.first()

        assertEquals(1, notes.size)
        assertEquals("두 번째 방문", notes.single().memo)
    }

    @Test
    fun `삭제하면 사라진다`() = runTest {
        repository.save(note("A1"))

        repository.delete("A1")

        assertNull(repository.observe("A1").first())
        assertTrue(repository.notes.first().isEmpty())
    }

    @Test
    fun `모르는 결함 상태 문자열은 미확인으로 복원된다`() = runTest {
        dao.upsert(entity("A1").copy(defectStatus = "LEGACY_VALUE"))

        assertEquals(DefectStatus.UNCHECKED, repository.observe("A1").first()!!.defectStatus)
    }

    @Test
    fun `범위 밖 점수는 안으로 접힌다`() = runTest {
        dao.upsert(entity("A1").copy(lightScore = 9, noiseScore = 0))

        val ratings = repository.observe("A1").first()!!.ratings

        assertEquals(VisitRatings.MAX_SCORE, ratings[VisitCriterion.LIGHT])
        assertEquals(VisitRatings.MIN_SCORE, ratings[VisitCriterion.NOISE])
    }

    private fun note(code: String, memo: String = "남향 채광 좋음") = SiteVisitNote(
        kaptCode = code,
        complexName = "관악푸르지오",
        regionLabel = "서울특별시 관악구 봉천동",
        visitedOn = "20260720",
        viewedUnit = "84㎡ · 12층 · 남향",
        ratings = VisitRatings(
            mapOf(VisitCriterion.LIGHT to 4, VisitCriterion.NOISE to 3, VisitCriterion.PARKING to 2),
        ),
        walkToStationMinutes = 8,
        elevatorCondition = ElevatorCondition.COMFORTABLE,
        defectStatus = DefectStatus.NONE,
        memo = memo,
        snapshot = ComplexSnapshot(
            builtYear = "2004",
            householdCount = 2104,
            subwayLabel = "2호선 · 서울대입구역 · 15~20분이내",
            referenceArea = 84.9,
            referenceAmount = 121_500,
        ),
        updatedAtMillis = 0L,
    )

    private fun entity(code: String) = SiteVisitNoteEntity(
        kaptCode = code,
        complexName = "관악푸르지오",
        regionLabel = "",
        visitedOn = "20260720",
        viewedUnit = "",
        lightScore = null,
        noiseScore = null,
        parkingScore = null,
        managementScore = null,
        walkToStationMinutes = null,
        elevatorCondition = ElevatorCondition.UNCHECKED.name,
        defectStatus = DefectStatus.NONE.name,
        memo = "",
        builtYear = null,
        householdCount = null,
        subwayLabel = null,
        referenceArea = null,
        referenceAmount = null,
        updatedAtMillis = 0L,
    )
}

/** 정렬(updatedAtMillis DESC)까지 실제 DAO와 같은 규약으로 흉내 낸다. */
private class FakeSiteVisitNoteDao : SiteVisitNoteDao {

    private val rows = MutableStateFlow<Map<String, SiteVisitNoteEntity>>(emptyMap())

    override fun observeAll(): Flow<List<SiteVisitNoteEntity>> =
        rows.map { it.values.sortedByDescending(SiteVisitNoteEntity::updatedAtMillis) }

    override fun observeByCode(kaptCode: String): Flow<SiteVisitNoteEntity?> =
        rows.map { it[kaptCode] }

    override suspend fun upsert(entity: SiteVisitNoteEntity) {
        rows.value = rows.value + (entity.kaptCode to entity)
    }

    override suspend fun deleteByCode(kaptCode: String) {
        rows.value = rows.value - kaptCode
    }
}
