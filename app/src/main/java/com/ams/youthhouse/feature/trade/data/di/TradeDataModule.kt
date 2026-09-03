package com.ams.youthhouse.feature.trade.data.di

import android.content.Context
import androidx.room.Room
import com.ams.youthhouse.core.network.di.MosstisApi
import com.ams.youthhouse.feature.trade.data.api.MosstisAptApi
import com.ams.youthhouse.feature.trade.data.local.FavoriteComplexDao
import com.ams.youthhouse.feature.trade.data.local.TradeDatabase
import com.ams.youthhouse.feature.trade.data.repository.ComplexRepositoryImpl
import com.ams.youthhouse.feature.trade.data.repository.FavoriteComplexRepositoryImpl
import com.ams.youthhouse.feature.trade.domain.repository.ComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.FavoriteComplexRepository
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
object TradeApiModule {

    @Provides
    @Singleton
    fun provideMosstisAptApi(@MosstisApi retrofit: Retrofit): MosstisAptApi =
        retrofit.create(MosstisAptApi::class.java)

    @Provides
    @Singleton
    fun provideTradeDatabase(
        @ApplicationContext context: Context,
    ): TradeDatabase = Room.databaseBuilder(
        context,
        TradeDatabase::class.java,
        "trade.db",
    ).build()

    @Provides
    fun provideFavoriteComplexDao(database: TradeDatabase): FavoriteComplexDao =
        database.favoriteComplexDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TradeRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindComplexRepository(impl: ComplexRepositoryImpl): ComplexRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteComplexRepository(
        impl: FavoriteComplexRepositoryImpl,
    ): FavoriteComplexRepository
}
