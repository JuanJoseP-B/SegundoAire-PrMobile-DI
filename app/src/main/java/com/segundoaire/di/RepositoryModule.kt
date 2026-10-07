package com.segundoaire.di

import com.segundoaire.data.permission.AccessibilityStatusRepositoryImpl
import com.segundoaire.data.permission.OverlayPermissionRepositoryImpl
import com.segundoaire.data.preferences.UserPreferencesRepositoryImpl
import com.segundoaire.data.repository.BlockedAppRepositoryImpl
import com.segundoaire.domain.repository.AccessibilityStatusRepository
import com.segundoaire.domain.repository.BlockedAppRepository
import com.segundoaire.domain.repository.OverlayPermissionRepository
import com.segundoaire.domain.repository.UserPreferencesRepository
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
}
