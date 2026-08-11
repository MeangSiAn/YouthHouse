package com.ams.myjeonse.core.notice.data.di

import com.ams.myjeonse.core.notice.data.api.NoticeApi
import com.ams.myjeonse.core.notice.data.repository.NoticeRepositoryImpl
import com.ams.myjeonse.core.notice.data.repository.RegionPreferenceRepositoryImpl
import com.ams.myjeonse.core.notice.domain.repository.NoticeRepository
import com.ams.myjeonse.core.notice.domain.repository.RegionPreferenceRepository
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
    abstract fun bindRegionPreferenceRepository(
        impl: RegionPreferenceRepositoryImpl,
    ): RegionPreferenceRepository
}
