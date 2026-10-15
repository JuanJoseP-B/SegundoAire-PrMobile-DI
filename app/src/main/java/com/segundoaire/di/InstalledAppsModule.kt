package com.segundoaire.di

import com.segundoaire.data.apps.InstalledAppsRepositoryImpl
import com.segundoaire.data.apps.LauncherAppSource
import com.segundoaire.data.apps.PackageManagerLauncherAppSource
import com.segundoaire.domain.repository.InstalledAppsRepository
import com.segundoaire.presentation.setup.AppIconLoader
import com.segundoaire.presentation.setup.PackageManagerAppIconLoader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InstalledAppsModule {

    @Binds
    @Singleton
    abstract fun bindLauncherAppSource(impl: PackageManagerLauncherAppSource): LauncherAppSource

    @Binds
    @Singleton
    abstract fun bindInstalledAppsRepository(impl: InstalledAppsRepositoryImpl): InstalledAppsRepository

    @Binds
    @Singleton
    abstract fun bindAppIconLoader(impl: PackageManagerAppIconLoader): AppIconLoader
}
