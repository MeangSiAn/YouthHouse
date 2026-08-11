package com.ams.myjeonse.app.di

import com.ams.myjeonse.BuildConfig
import com.ams.myjeonse.core.network.NetworkConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 앱 빌드 설정(BuildConfig)을 core 계층이 쓰는 순수 설정 객체로 변환해 주입한다.
 * BuildConfig 참조를 app 패키지에 가둬 두기 위한 모듈이다.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkConfigModule {

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig = NetworkConfig(
        baseUrl = BuildConfig.DATA_GO_KR_BASE_URL,
        serviceKey = BuildConfig.DATA_GO_KR_SERVICE_KEY,
        isLoggingEnabled = BuildConfig.DEBUG,
    )
}
