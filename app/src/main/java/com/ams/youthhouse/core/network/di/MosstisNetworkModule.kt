package com.ams.youthhouse.core.network.di

import com.ams.youthhouse.core.network.MosstisConfig
import com.ams.youthhouse.core.network.interceptor.ApiKeyHeaderInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/** 자체 백엔드(data.mosstis.com)로 가는 클라이언트를 data.go.kr 것과 구분한다. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MosstisApi

@Module
@InstallIn(SingletonComponent::class)
object MosstisNetworkModule {

    /**
     * 기존 클라이언트에서 파생시킨다(`newBuilder`) — 커넥션 풀·디스패처를 공유하고
     * 인터셉터 구성만 갈린다. data.go.kr용 serviceKey 인터셉터는 호스트가 달라
     * 붙어도 무해하지만, 남의 키를 다른 서버에 흘리지 않도록 비우고 다시 쌓는다.
     */
    @MosstisApi
    @Provides
    @Singleton
    fun provideMosstisOkHttpClient(
        baseClient: OkHttpClient,
        apiKeyInterceptor: ApiKeyHeaderInterceptor,
    ): OkHttpClient = baseClient.newBuilder()
        .apply { interceptors().clear() }
        .addInterceptor(apiKeyInterceptor)
        .apply {
            // 로깅 인터셉터는 debug에서만 base 체인에 존재한다. 있으면 되살린다.
            baseClient.interceptors
                .filterIsInstance<okhttp3.logging.HttpLoggingInterceptor>()
                .forEach(::addInterceptor)
        }
        .build()

    @MosstisApi
    @Provides
    @Singleton
    fun provideMosstisRetrofit(
        config: MosstisConfig,
        @MosstisApi okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(config.baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
