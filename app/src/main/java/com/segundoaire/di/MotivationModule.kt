package com.segundoaire.di

import android.content.Context
import com.segundoaire.data.motivation.LocalMotivationalMessageProvider
import com.segundoaire.data.motivation.messageBankRes
import com.segundoaire.domain.provider.MotivationalMessageProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MotivationModule {

    @Provides
    @Singleton
    fun provideMotivationalMessageProvider(
        @ApplicationContext context: Context
    ): MotivationalMessageProvider = LocalMotivationalMessageProvider(
        bank = { goal -> context.resources.getStringArray(goal.messageBankRes()).toList() }
    )
}
