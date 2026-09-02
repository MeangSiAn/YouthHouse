package com.ams.youthhouse.core.notice.data.di

import android.content.Context
import androidx.room.Room
import com.ams.youthhouse.core.notice.data.api.NoticeApi
import com.ams.youthhouse.core.notice.data.local.FavoriteNoticeDao
import com.ams.youthhouse.core.notice.data.local.NoticeDatabase
import com.ams.youthhouse.core.notice.data.repository.FavoriteNoticeRepositoryImpl
import com.ams.youthhouse.core.notice.data.repository.NoticeFilterRepositoryImpl
import com.ams.youthhouse.core.notice.data.repository.NoticeRepositoryImpl
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeRepository
import com.ams.youthhouse.core.notice.domain.repository.NoticeFilterRepository
import com.ams.youthhouse.core.notice.domain.repository.NoticeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NoticeApiModule {

    @Provides
    @Singleton
    fun provideNoticeApi(retrofit: Retrofit): NoticeApi = retrofit.create(NoticeApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object NoticeDatabaseModule {

    @Provides
    @Singleton
    fun provideNoticeDatabase(
        @ApplicationContext context: Context,
    ): NoticeDatabase = Room.databaseBuilder(
        context,
        NoticeDatabase::class.java,
        "notice.db",
    )
        // 아직 스토어에 나간 적 없는 DB다. 마이그레이션 대신 비우는 게 맞고,
        // 첫 출시 이후에는 이 호출을 제거하고 Migration을 작성해야 한다.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    fun provideFavoriteNoticeDao(database: NoticeDatabase): FavoriteNoticeDao =
        database.favoriteNoticeDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoticeRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(impl: NoticeRepositoryImpl): NoticeRepository

    @Binds
    @Singleton
    abstract fun bindNoticeFilterRepository(
        impl: NoticeFilterRepositoryImpl,
    ): NoticeFilterRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteNoticeRepository(
        impl: FavoriteNoticeRepositoryImpl,
    ): FavoriteNoticeRepository
}
