package com.ams.youthhouse.core.notice.data.di

import com.ams.youthhouse.core.notice.data.api.NoticeApi
import com.ams.youthhouse.core.notice.data.repository.NoticeFilterRepositoryImpl
import com.ams.youthhouse.core.notice.data.repository.NoticeRepositoryImpl
import com.ams.youthhouse.core.notice.domain.repository.NoticeFilterRepository
import com.ams.youthhouse.core.notice.domain.repository.NoticeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
abstract class NoticeRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(impl: NoticeRepositoryImpl): NoticeRepository

    @Binds
    @Singleton
    abstract fun bindNoticeFilterRepository(
        impl: NoticeFilterRepositoryImpl,
    ): NoticeFilterRepository
}
