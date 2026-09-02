package com.ams.youthhouse.core.notice.data.repository

import com.ams.youthhouse.core.notice.data.local.FavoriteNoticeDao
import com.ams.youthhouse.core.notice.data.local.FavoriteNoticeEntity
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeAddress
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticePeriod
import com.ams.youthhouse.core.notice.domain.model.NoticePrice
import com.ams.youthhouse.core.notice.domain.repository.toFavoriteKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteNoticeRepositoryImplTest {

    private val dao = FakeFavoriteNoticeDao()
    private val repository = FavoriteNoticeRepositoryImpl(dao)

    @Test
    fun `토글 한 번이면 저장되고 두 번이면 사라진다`() = runTest {
        val notice = notice("21096")

        repository.toggle(notice)
        assertEquals(listOf("21096"), repository.favorites.first().map { it.pblancId })

        repository.toggle(notice)
        assertTrue(repository.favorites.first().isEmpty())
    }

    @Test
    fun `스냅숏이 도메인 모델로 온전히 복원된다`() = runTest {
        val notice = notice("21096")

        repository.toggle(notice)

        assertEquals(notice, repository.favorites.first().single())
    }

    @Test
    fun `같은 공고라도 시군구가 다른 행은 별개의 찜이다`() = runTest {
        // 이 API는 한 공고를 시군구별 행으로 쪼개 내려보낸다.
        // 영통구 행을 찜했는데 기흥구 행까지 켜지면 안 된다.
        repository.toggle(notice("21096", districtName = "수원시 영통구"))

        val keys = repository.favoriteKeys.first()

        assertTrue(keys.single().districtName == "수원시 영통구")
        assertTrue(notice("21096", districtName = "용인시 기흥구").toFavoriteKey() !in keys)
    }

    @Test
    fun `같은 행을 다시 토글하면 그 행만 해제된다`() = runTest {
        repository.toggle(notice("21096", districtName = "수원시 영통구"))
        repository.toggle(notice("21096", districtName = "용인시 기흥구"))

        repository.toggle(notice("21096", districtName = "수원시 영통구"))

        val remaining = repository.favorites.first()
        assertEquals(listOf("용인시 기흥구"), remaining.map { it.address.districtName })
    }

    @Test
    fun `분야가 다르면 같은 pblancId라도 별개의 찜이다`() = runTest {
        // 두 API의 ID 시퀀스가 별개라 미래 충돌을 배제할 수 없다.
        repository.toggle(notice("21096", NoticeCategory.RENTAL))
        repository.toggle(notice("21096", NoticeCategory.SALE))

        assertEquals(2, repository.favoriteKeys.first().size)
    }

    @Test
    fun `깨진 스냅숏은 그 한 건만 건너뛴다`() = runTest {
        repository.toggle(notice("valid"))
        dao.insert(
            FavoriteNoticeEntity(
                key = "RENTAL:broken",
                category = "RENTAL",
                pblancId = "broken",
                noticeJson = "{not-json",
                savedAtMillis = 0L,
            ),
        )

        assertEquals(listOf("valid"), repository.favorites.first().map { it.pblancId })
    }

    private fun notice(
        pblancId: String,
        category: NoticeCategory = NoticeCategory.RENTAL,
        districtName: String = "울주군",
    ): Notice = Notice(
        category = category,
        pblancId = pblancId,
        houseSn = 3,
        title = "공고 $pblancId",
        statusName = "일반공고",
        supplyInstitutionName = "LH",
        houseTypeName = "아파트",
        supplyTypeName = null,
        previousNoticeId = null,
        complexName = "단지",
        address = NoticeAddress("울산광역시", districtName, "울산 울주군 1", null, null, "pnu"),
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

/** 정렬(savedAtMillis DESC)까지 실제 DAO와 같은 규약으로 흉내 낸다. */
private class FakeFavoriteNoticeDao : FavoriteNoticeDao {

    private val rows = MutableStateFlow<Map<String, FavoriteNoticeEntity>>(emptyMap())

    override fun observeAll(): Flow<List<FavoriteNoticeEntity>> =
        rows.map { it.values.sortedByDescending(FavoriteNoticeEntity::savedAtMillis) }

    override suspend fun exists(key: String): Boolean = key in rows.value

    override suspend fun insert(entity: FavoriteNoticeEntity) {
        rows.value = rows.value + (entity.key to entity)
    }

    override suspend fun deleteByKey(key: String) {
        rows.value = rows.value - key
    }
}
