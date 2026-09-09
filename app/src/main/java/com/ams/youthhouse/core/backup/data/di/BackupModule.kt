package com.ams.youthhouse.core.backup.data.di

import com.ams.youthhouse.core.backup.data.repository.BackupRepositoryImpl
import com.ams.youthhouse.core.backup.domain.repository.BackupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
