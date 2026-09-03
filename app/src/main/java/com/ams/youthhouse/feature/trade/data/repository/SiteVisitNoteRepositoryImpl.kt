package com.ams.youthhouse.feature.trade.data.repository

import com.ams.youthhouse.feature.trade.data.local.SiteVisitNoteDao
import com.ams.youthhouse.feature.trade.data.local.SiteVisitNoteEntity
import com.ams.youthhouse.feature.trade.data.mapper.toDomain
import com.ams.youthhouse.feature.trade.data.mapper.toEntity
import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote
import com.ams.youthhouse.feature.trade.domain.repository.SiteVisitNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteVisitNoteRepositoryImpl @Inject constructor(
    private val dao: SiteVisitNoteDao,
) : SiteVisitNoteRepository {

    override val notes: Flow<List<SiteVisitNote>> =
        dao.observeAll().map { entities -> entities.map(SiteVisitNoteEntity::toDomain) }

    override fun observe(kaptCode: String): Flow<SiteVisitNote?> =
        dao.observeByCode(kaptCode).map { it?.toDomain() }

    override suspend fun save(note: SiteVisitNote) {
        dao.upsert(note.toEntity(updatedAtMillis = System.currentTimeMillis()))
    }

    override suspend fun delete(kaptCode: String) {
        dao.deleteByCode(kaptCode)
    }
}
