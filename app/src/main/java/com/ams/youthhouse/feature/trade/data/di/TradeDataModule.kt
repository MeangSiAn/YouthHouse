package com.ams.youthhouse.feature.trade.data.di

import android.content.Context
import androidx.room.Room
import com.ams.youthhouse.core.network.di.MosstisApi
import com.ams.youthhouse.feature.trade.data.api.MosstisAptApi
import com.ams.youthhouse.feature.trade.data.local.FavoriteComplexDao
import com.ams.youthhouse.feature.trade.data.local.RecentComplexDao
import com.ams.youthhouse.feature.trade.data.local.SiteVisitNoteDao
import com.ams.youthhouse.feature.trade.data.local.TradeDatabase
import com.ams.youthhouse.feature.trade.data.repository.ComplexRepositoryImpl
import com.ams.youthhouse.feature.trade.data.repository.FavoriteComplexRepositoryImpl
import com.ams.youthhouse.feature.trade.data.repository.RecentComplexRepositoryImpl
import com.ams.youthhouse.feature.trade.data.repository.SiteVisitNoteRepositoryImpl
import com.ams.youthhouse.feature.trade.domain.repository.ComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.FavoriteComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.RecentComplexRepository
import com.ams.youthhouse.feature.trade.domain.repository.SiteVisitNoteRepository
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
    )
        // 아직 스토어에 나간 적 없는 DB다. 마이그레이션 대신 비우는 게 맞고,
        // 첫 출시 이후에는 이 호출을 제거하고 Migration을 작성해야 한다.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    fun provideFavoriteComplexDao(database: TradeDatabase): FavoriteComplexDao =
        database.favoriteComplexDao()

    @Provides
    fun provideSiteVisitNoteDao(database: TradeDatabase): SiteVisitNoteDao =
        database.siteVisitNoteDao()

    @Provides
    fun provideRecentComplexDao(database: TradeDatabase): RecentComplexDao =
        database.recentComplexDao()
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

    @Binds
    @Singleton
    abstract fun bindSiteVisitNoteRepository(
        impl: SiteVisitNoteRepositoryImpl,
    ): SiteVisitNoteRepository

    @Binds
    @Singleton
    abstract fun bindRecentComplexRepository(
        impl: RecentComplexRepositoryImpl,
    ): RecentComplexRepository
}
