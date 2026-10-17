package com.segundoaire.di

import com.segundoaire.data.coach.LocalCoachRepository
import com.segundoaire.data.local.DatabaseCleaner
import com.segundoaire.data.local.RoomDatabaseCleaner
import com.segundoaire.data.locale.AppLanguageRepositoryImpl
import com.segundoaire.data.permission.AccessibilityStatusRepositoryImpl
import com.segundoaire.data.permission.OverlayPermissionRepositoryImpl
import com.segundoaire.data.preferences.BlockRepositoryImpl
import com.segundoaire.data.preferences.UserPreferencesRepositoryImpl
import com.segundoaire.data.preferences.UserProfileRepositoryImpl
import com.segundoaire.data.repository.AppResetRepositoryImpl
import com.segundoaire.data.repository.AppUsageRepositoryImpl
import com.segundoaire.data.repository.BlockSessionRepositoryImpl
import com.segundoaire.data.repository.BlockedAppRepositoryImpl
import com.segundoaire.data.repository.InterceptionEventRepositoryImpl
import com.segundoaire.data.tree.InstallTimeSource
import com.segundoaire.data.tree.PackageInstallTimeSource
import com.segundoaire.data.tree.TreeRepositoryImpl
import com.segundoaire.domain.repository.AccessibilityStatusRepository
import com.segundoaire.domain.repository.AppLanguageRepository
import com.segundoaire.domain.repository.AppResetRepository
import com.segundoaire.domain.repository.AppUsageRepository
import com.segundoaire.domain.repository.BlockRepository
import com.segundoaire.domain.repository.BlockSessionRepository
import com.segundoaire.domain.repository.BlockedAppRepository
import com.segundoaire.domain.repository.CoachRepository
import com.segundoaire.domain.repository.InterceptionEventRepository
import com.segundoaire.domain.repository.OverlayPermissionRepository
import com.segundoaire.domain.repository.TreeRepository
import com.segundoaire.domain.repository.UserPreferencesRepository
import com.segundoaire.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindAppLanguageRepository(
        impl: AppLanguageRepositoryImpl
    ): AppLanguageRepository

    @Binds
    @Singleton
    abstract fun bindOverlayPermissionRepository(
        impl: OverlayPermissionRepositoryImpl
    ): OverlayPermissionRepository

    @Binds
    @Singleton
    abstract fun bindAccessibilityStatusRepository(
        impl: AccessibilityStatusRepositoryImpl
    ): AccessibilityStatusRepository

    @Binds
    @Singleton
    abstract fun bindBlockedAppRepository(
        impl: BlockedAppRepositoryImpl
    ): BlockedAppRepository

    @Binds
    @Singleton
    abstract fun bindBlockRepository(
        impl: BlockRepositoryImpl
    ): BlockRepository

    @Binds
    @Singleton
    abstract fun bindInterceptionEventRepository(
        impl: InterceptionEventRepositoryImpl
    ): InterceptionEventRepository

    @Binds
    @Singleton
    abstract fun bindAppResetRepository(
        impl: AppResetRepositoryImpl
    ): AppResetRepository

    @Binds
    @Singleton
    abstract fun bindDatabaseCleaner(
        impl: RoomDatabaseCleaner
    ): DatabaseCleaner

    @Binds
    @Singleton
    abstract fun bindBlockSessionRepository(
        impl: BlockSessionRepositoryImpl
    ): BlockSessionRepository

    @Binds
    @Singleton
    abstract fun bindTreeRepository(
        impl: TreeRepositoryImpl
    ): TreeRepository

    @Binds
    @Singleton
    abstract fun bindInstallTimeSource(
        impl: PackageInstallTimeSource
    ): InstallTimeSource

    @Binds
    @Singleton
    abstract fun bindCoachRepository(
        impl: LocalCoachRepository
    ): CoachRepository

    @Binds
    @Singleton
    abstract fun bindAppUsageRepository(
        impl: AppUsageRepositoryImpl
    ): AppUsageRepository
}
