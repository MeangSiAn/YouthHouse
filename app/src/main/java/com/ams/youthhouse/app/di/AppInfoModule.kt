package com.ams.youthhouse.app.di

import com.ams.youthhouse.BuildConfig
import com.ams.youthhouse.core.common.AppInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** BuildConfig 참조를 app 패키지에 가둬 두기 위한 모듈. */
@Module
@InstallIn(SingletonComponent::class)
object AppInfoModule {

    @Provides
    @Singleton
    fun provideAppInfo(): AppInfo = AppInfo(
        versionName = BuildConfig.VERSION_NAME,
    )
}
