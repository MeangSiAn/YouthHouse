package com.ams.youthhouse.core.backup.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 이 기기의 작성자 토큰. 무작위 UUID라 개인을 식별하지 않고, 서버로 가지도 않는다.
 *
 * 지금은 백업 파일에 실리기만 한다. 나중에 노트를 공개로 전환할 때 "기기를 바꿔도 같은
 * 사람"을 잇는 열쇠가 된다 — 그때 새로 만들면 예전 백업과 이어지지 않는다.
 */
@Singleton
class AuthorTokenDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    suspend fun getOrCreate(): String {
        dataStore.data.first()[AUTHOR_TOKEN]?.let { return it }
        val created = UUID.randomUUID().toString()
        dataStore.edit { preferences ->
            // 그 사이 다른 코루틴이 만들었으면 그것을 지킨다.
            if (preferences[AUTHOR_TOKEN] == null) preferences[AUTHOR_TOKEN] = created
        }
        return dataStore.data.first()[AUTHOR_TOKEN] ?: created
    }

    /** 백업에서 온 토큰. 이 기기에 이미 하나 있으면 바꾸지 않는다 — 두 기기를 합칠 때 어느 쪽이든 하나면 된다. */
    suspend fun setIfAbsent(token: String) {
        dataStore.edit { preferences ->
            if (preferences[AUTHOR_TOKEN] == null) preferences[AUTHOR_TOKEN] = token
        }
    }

    private companion object {
        val AUTHOR_TOKEN = stringPreferencesKey("author_token")
    }
}
