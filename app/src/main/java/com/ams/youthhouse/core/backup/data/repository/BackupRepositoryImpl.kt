package com.ams.youthhouse.core.backup.data.repository

import android.content.Context
import android.net.Uri
import com.ams.youthhouse.core.backup.data.BackupCodec
import com.ams.youthhouse.core.backup.data.local.AuthorTokenDataSource
import com.ams.youthhouse.core.backup.domain.model.BackupPayload
import com.ams.youthhouse.core.backup.domain.model.BackupSettings
import com.ams.youthhouse.core.backup.domain.model.BackupSummary
import com.ams.youthhouse.core.backup.domain.repository.BackupFormatException
import com.ams.youthhouse.core.backup.domain.repository.BackupRepository
import com.ams.youthhouse.core.complex.domain.repository.FavoriteComplexRepository
import com.ams.youthhouse.core.complex.domain.repository.SiteVisitNoteRepository
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeRepository
import com.ams.youthhouse.core.notice.domain.repository.NoticeFilterRepository
import com.ams.youthhouse.core.notice.domain.repository.toFavoriteKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 파일 I/O만 여기서 한다. 무엇을 담고 어떻게 읽는지는 [BackupCodec].
 *
 * 다른 저장소의 도메인 인터페이스를 통해 모으고 되돌린다 — DAO를 직접 만지면 각 저장소가
 * 지키는 규약(키 형식, 수정 시각 찍기)을 우회하게 된다.
 */
@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoriteNoticeRepository: FavoriteNoticeRepository,
    private val favoriteComplexRepository: FavoriteComplexRepository,
    private val siteVisitNoteRepository: SiteVisitNoteRepository,
    private val noticeFilterRepository: NoticeFilterRepository,
    private val authorTokenDataSource: AuthorTokenDataSource,
) : BackupRepository {

    override suspend fun export(targetUri: String): BackupSummary {
        val payload = BackupPayload(
            authorToken = authorTokenDataSource.getOrCreate(),
            settings = BackupSettings(
                region = noticeFilterRepository.selectedRegion.first(),
                category = noticeFilterRepository.selectedCategory.first(),
                statusFilter = noticeFilterRepository.selectedStatusFilter.first(),
            ),
            favoriteNotices = favoriteNoticeRepository.favorites.first(),
            favoriteComplexes = favoriteComplexRepository.favorites.first(),
            siteVisitNotes = siteVisitNoteRepository.notes.first(),
        )
        val text = BackupCodec.encode(payload, exportedAtMillis = System.currentTimeMillis())

        withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openOutputStream(Uri.parse(targetUri), "wt")
                ?: throw IOException("백업 파일을 열 수 없습니다: $targetUri")
            stream.bufferedWriter().use { it.write(text) }
        }
        return payload.toSummary()
    }

    override suspend fun import(sourceUri: String): BackupSummary {
        val text = withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openInputStream(Uri.parse(sourceUri))
                ?: throw IOException("백업 파일을 열 수 없습니다: $sourceUri")
            // 사진 없는 텍스트 백업은 수십 KB다. 그보다 훨씬 크면 우리 파일이 아니다.
            stream.bufferedReader().use { reader ->
                val chars = CharArray(MAX_FILE_CHARS + 1)
                val read = reader.read(chars)
                if (read > MAX_FILE_CHARS) throw BackupFormatException("백업 파일이 너무 큽니다")
                if (read <= 0) "" else String(chars, 0, read)
            }
        }
        val payload = BackupCodec.decode(text)

        // 노트는 단지당 하나라 덮어쓰기가 곧 합치기다.
        payload.siteVisitNotes.forEach { siteVisitNoteRepository.save(it) }

        // 찜은 토글밖에 없으므로, 이미 있는 것을 건드리지 않도록 없는 것만 누른다.
        val existingNoticeKeys = favoriteNoticeRepository.favoriteKeys.first()
        payload.favoriteNotices
            .filter { it.toFavoriteKey() !in existingNoticeKeys }
            .distinctBy { it.toFavoriteKey() }
            .forEach { favoriteNoticeRepository.toggle(it) }

        val existingComplexCodes = favoriteComplexRepository.favoriteCodes.first()
        payload.favoriteComplexes
            .filter { it.kaptCode !in existingComplexCodes }
            .distinctBy { it.kaptCode }
            .forEach { favoriteComplexRepository.toggle(it) }

        payload.settings?.let { settings ->
            noticeFilterRepository.setSelectedRegion(settings.region)
            noticeFilterRepository.setSelectedCategory(settings.category)
            noticeFilterRepository.setSelectedStatusFilter(settings.statusFilter)
        }
        payload.authorToken?.let { authorTokenDataSource.setIfAbsent(it) }

        return payload.toSummary()
    }

    private fun BackupPayload.toSummary() = BackupSummary(
        noteCount = siteVisitNotes.size,
        favoriteNoticeCount = favoriteNotices.size,
        favoriteComplexCount = favoriteComplexes.size,
    )

    private companion object {
        const val MAX_FILE_CHARS = 8 * 1024 * 1024
    }
}
