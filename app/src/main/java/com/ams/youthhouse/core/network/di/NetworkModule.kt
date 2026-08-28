package com.ams.youthhouse.core.network.di

import com.ams.youthhouse.core.network.NetworkConfig
import com.ams.youthhouse.core.network.interceptor.ServiceKeyInterceptor
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 15L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    /**
     * 켤지 말지는 [provideOkHttpClient]가 정한다. 여기서 레벨까지 나누면
     * "끄는 결정"이 두 군데로 갈라진다.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        networkConfig: NetworkConfig,
        serviceKeyInterceptor: ServiceKeyInterceptor,
        // Lazy로 받아야 release에서 인터셉터가 아예 만들어지지 않는다.
        loggingInterceptor: Lazy<HttpLoggingInterceptor>,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(serviceKeyInterceptor)
        // 레벨을 NONE으로 두더라도 인터셉터를 끼우면 매 요청이 체인을 한 단계 더 지난다.
        // 아무것도 찍지 않을 거면 아예 넣지 않는다.
        .apply { if (networkConfig.isLoggingEnabled) addInterceptor(loggingInterceptor.get()) }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        networkConfig: NetworkConfig,
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(networkConfig.baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
