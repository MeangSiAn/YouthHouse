package com.ams.youthhouse.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 공고 목록 필터(지역·분야)를 원시 문자열로 읽고 쓴다.
 *
 * 지역은 enum 상수명이 아니라 **API 코드**("11")를 저장한다.
 * 코드는 서버 계약이라 바뀌지 않지만 enum 상수명은 리팩터링 한 번에 바뀌고,
 * 그러면 사용자가 저장해 둔 값이 조용히 깨진다.
 *
 * 지역의 "전체"는 키를 지우는 것으로 표현한다.
 * 분야는 항상 하나가 선택돼 있으므로 키가 없으면 호출부가 기본값을 정한다.
 */
@Singleton
class NoticeFilterPreferenceDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private val preferences: Flow<Preferences> = dataStore.data
        // 파일이 손상되면 기본값으로 복구한다. 그대로 두면 앱이 시작하지 못한다.
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }

    val selectedRegionCode: Flow<String?> = preferences.map { it[SELECTED_REGION_CODE] }

    val selectedCategoryName: Flow<String?> = preferences.map { it[SELECTED_CATEGORY] }

    suspend fun setSelectedRegionCode(code: String?) {
        dataStore.edit { preferences ->
            if (code == null) {
                preferences.remove(SELECTED_REGION_CODE)
            } else {
                preferences[SELECTED_REGION_CODE] = code
            }
        }
    }

    suspend fun setSelectedCategoryName(name: String) {
        dataStore.edit { preferences -> preferences[SELECTED_CATEGORY] = name }
    }

    private companion object {
        val SELECTED_REGION_CODE = stringPreferencesKey("selected_region_code")
        val SELECTED_CATEGORY = stringPreferencesKey("selected_notice_category")
    }
}
