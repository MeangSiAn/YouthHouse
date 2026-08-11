package com.ams.myjeonse.core.notice.data.repository

import com.ams.myjeonse.core.datastore.RegionPreferenceDataSource
import com.ams.myjeonse.core.notice.domain.model.NoticeRegion
import com.ams.myjeonse.core.notice.domain.repository.RegionPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionPreferenceRepositoryImpl @Inject constructor(
    private val dataSource: RegionPreferenceDataSource,
) : RegionPreferenceRepository {

    override val selectedRegion: Flow<NoticeRegion?> = dataSource.selectedRegionCode
        .map { code -> NoticeRegion.fromCode(code) }

    override suspend fun setSelectedRegion(region: NoticeRegion?) {
        dataSource.setSelectedRegionCode(region?.code)
    }
}
