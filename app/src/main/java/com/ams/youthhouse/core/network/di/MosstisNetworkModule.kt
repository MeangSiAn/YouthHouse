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

/** 자체 백엔드(data.mosstis.com)로 가는 클라이언트. 인증 헤더가 붙는 쪽이다. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MosstisApi

@Module
@InstallIn(SingletonComponent::class)
object MosstisNetworkModule {

    /**
     * 기존 클라이언트에서 파생시킨다(`newBuilder`) — 커넥션 풀·디스패처를 공유하고
     * 인증 헤더만 얹는다.
     *
     * 예전에는 base 체인을 비우고 다시 쌓았다. data.go.kr용 serviceKey 인터셉터가
     * 거기 있어 남의 키가 이 서버로 새어 나가지 않게 해야 했기 때문이다.
     * 그 인터셉터가 사라진 지금은 base가 로깅만 갖고 있어 그대로 물려받으면 된다.
     */
    @MosstisApi
    @Provides
    @Singleton
    fun provideMosstisOkHttpClient(
        baseClient: OkHttpClient,
        apiKeyInterceptor: ApiKeyHeaderInterceptor,
    ): OkHttpClient = baseClient.newBuilder()
        .addInterceptor(apiKeyInterceptor)
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
