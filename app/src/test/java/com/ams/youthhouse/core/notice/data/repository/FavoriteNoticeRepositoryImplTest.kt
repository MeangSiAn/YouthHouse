package com.ams.youthhouse.core.notice.data.repository

import com.ams.youthhouse.core.notice.data.local.FavoriteNoticeDao
import com.ams.youthhouse.core.notice.data.local.FavoriteNoticeEntity
import com.ams.youthhouse.core.notice.domain.model.Notice
import com.ams.youthhouse.core.notice.domain.model.NoticeAddress
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticePeriod
import com.ams.youthhouse.core.notice.domain.model.NoticePrice
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeKey
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
    fun `키는 분야와 공고 ID로 만들어진다`() = runTest {
        repository.toggle(notice("21096", NoticeCategory.RENTAL))
        repository.toggle(notice("21096", NoticeCategory.SALE))

        val keys = repository.favoriteKeys.first()

        // 같은 pblancId라도 분야가 다르면 서로 다른 찜이다 (두 API의 ID 시퀀스가 별개라서)
        assertEquals(
            setOf(
                FavoriteNoticeKey(NoticeCategory.RENTAL, "21096"),
                FavoriteNoticeKey(NoticeCategory.SALE, "21096"),
            ),
            keys,
        )
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

/** 정렬(savedAtMillis DESC)까지 실제 DAO와 같은 규약으로 흉내 낸다. */
private class FakeFavoriteNoticeDao : FavoriteNoticeDao {

    private val rows = MutableStateFlow<Map<String, FavoriteNoticeEntity>>(emptyMap())

    override fun observeAll(): Flow<List<FavoriteNoticeEntity>> =
        rows.map { it.values.sortedByDescending(FavoriteNoticeEntity::savedAtMillis) }

    override fun observeKeys(): Flow<List<String>> = rows.map { it.keys.toList() }

    override suspend fun exists(key: String): Boolean = key in rows.value

    override suspend fun insert(entity: FavoriteNoticeEntity) {
        rows.value = rows.value + (entity.key to entity)
    }

    override suspend fun deleteByKey(key: String) {
        rows.value = rows.value - key
    }
}
