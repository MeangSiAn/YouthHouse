package com.ams.youthhouse.core.common.di

import com.ams.youthhouse.core.common.time.SystemTodayProvider
import com.ams.youthhouse.core.common.time.TodayProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {

    @Binds
    @Singleton
    abstract fun bindTodayProvider(impl: SystemTodayProvider): TodayProvider
}
