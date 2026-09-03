package com.ams.youthhouse.feature.trade.domain.repository

import com.ams.youthhouse.feature.trade.domain.model.SiteVisitNote
import kotlinx.coroutines.flow.Flow

/** 임장노트 저장소. 기기 안에만 있고 서버로 가지 않는다. */
interface SiteVisitNoteRepository {

    /** 최근에 고친 것이 앞에 온다. */
    val notes: Flow<List<SiteVisitNote>>

    fun observe(kaptCode: String): Flow<SiteVisitNote?>

    /** 같은 단지의 노트가 있으면 덮어쓴다. 수정 시각은 저장소가 찍는다. */
    suspend fun save(note: SiteVisitNote)

    suspend fun delete(kaptCode: String)
}
