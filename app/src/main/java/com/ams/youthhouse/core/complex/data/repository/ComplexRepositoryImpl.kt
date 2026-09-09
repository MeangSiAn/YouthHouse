package com.ams.youthhouse.core.complex.data.repository

import com.ams.youthhouse.core.network.safeApiCall
import com.ams.youthhouse.core.complex.data.api.MosstisAptApi
import com.ams.youthhouse.core.complex.data.local.FavoriteComplexDao
import com.ams.youthhouse.core.complex.data.local.RecentComplexDao
import com.ams.youthhouse.core.complex.data.local.RecentComplexEntity
import com.ams.youthhouse.core.complex.data.local.FavoriteComplexEntity
import com.ams.youthhouse.core.complex.data.mapper.toDomain
import com.ams.youthhouse.core.complex.domain.model.AptComplex
import com.ams.youthhouse.core.complex.domain.model.ComplexDetail
import com.ams.youthhouse.core.complex.domain.model.FavoriteComplex
import com.ams.youthhouse.core.complex.domain.model.RecentComplex
import com.ams.youthhouse.core.complex.domain.repository.ComplexRepository
import com.ams.youthhouse.core.complex.domain.repository.FavoriteComplexRepository
import com.ams.youthhouse.core.complex.domain.repository.RecentComplexRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplexRepositoryImpl @Inject constructor(
    private val api: MosstisAptApi,
) : ComplexRepository {

    override suspend fun search(query: String): List<AptComplex> = safeApiCall {
        api.searchComplexes(query = query, limit = SEARCH_LIMIT).results
            .filter { it.kaptCode.isNotBlank() }
            .map { it.toDomain() }
    }

    override suspend fun getDetail(kaptCode: String, months: Int): ComplexDetail = safeApiCall {
        api.getComplexDetail(kaptCode = kaptCode, months = months).toDomain()
    }

    private companion object {
        const val SEARCH_LIMIT = 20
    }
}

@Singleton
class FavoriteComplexRepositoryImpl @Inject constructor(
    private val dao: FavoriteComplexDao,
) : FavoriteComplexRepository {

    override val favorites: Flow<List<FavoriteComplex>> =
        dao.observeAll().map { entities ->
            entities.map { FavoriteComplex(it.kaptCode, it.name, it.regionLabel) }
        }

    override val favoriteCodes: Flow<Set<String>> =
        dao.observeAll().map { entities -> entities.map { it.kaptCode }.toSet() }

    override suspend fun toggle(complex: FavoriteComplex) {
        if (dao.exists(complex.kaptCode)) {
            dao.deleteByCode(complex.kaptCode)
        } else {
            dao.insert(
                FavoriteComplexEntity(
                    kaptCode = complex.kaptCode,
                    name = complex.name,
                    regionLabel = complex.regionLabel,
                    savedAtMillis = System.currentTimeMillis(),
                ),
            )
        }
    }
}

@Singleton
class RecentComplexRepositoryImpl @Inject constructor(
    private val dao: RecentComplexDao,
) : RecentComplexRepository {

    override val recents: Flow<List<RecentComplex>> =
        dao.observeRecent(MAX_RECENTS).map { entities ->
            entities.map { RecentComplex(it.kaptCode, it.name, it.regionLabel) }
        }

    override suspend fun record(complex: RecentComplex) {
        dao.upsert(
            RecentComplexEntity(
                kaptCode = complex.kaptCode,
                name = complex.name,
                regionLabel = complex.regionLabel,
                viewedAtMillis = System.currentTimeMillis(),
            ),
        )
        // 표시 한도보다 넉넉히 남긴다 — 맨 앞 몇 개를 지웠을 때 줄이 비지 않도록.
        dao.trimTo(MAX_RECENTS * 2)
    }

    private companion object {
        /** 한 줄에 가로로 놓이는 수. 더 늘리면 스크롤해야 보이고, 그건 검색이 할 일이다. */
        const val MAX_RECENTS = 5
    }
}
