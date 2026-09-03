package com.ams.youthhouse.core.notice.data.repository

import com.ams.youthhouse.core.datastore.NoticeFilterPreferenceDataSource
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import com.ams.youthhouse.core.notice.domain.repository.NoticeFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeFilterRepositoryImpl @Inject constructor(
    private val dataSource: NoticeFilterPreferenceDataSource,
) : NoticeFilterRepository {

    override val selectedRegion: Flow<NoticeRegion?> = dataSource.selectedRegionCode
        .map { code -> NoticeRegion.fromCode(code) }

    override val selectedCategory: Flow<NoticeCategory> = dataSource.selectedCategoryName
        .map { name ->
            // 모르는 값(구버전 잔재 등)은 기본 분야로 폴백한다. 예외를 던지면 목록이 통째로 죽는다.
            NoticeCategory.entries.firstOrNull { it.name == name } ?: NoticeCategory.RENTAL
        }

    override val selectedStatusFilter: Flow<NoticeStatusFilter> =
        dataSource.selectedStatusFilterName.map { name -> NoticeStatusFilter.fromName(name) }

    override suspend fun setSelectedRegion(region: NoticeRegion?) {
        dataSource.setSelectedRegionCode(region?.code)
    }

    override suspend fun setSelectedCategory(category: NoticeCategory) {
        dataSource.setSelectedCategoryName(category.name)
    }

    override suspend fun setSelectedStatusFilter(filter: NoticeStatusFilter) {
        dataSource.setSelectedStatusFilterName(filter.name)
    }
}
