package com.segundoaire.domain.repository

import com.segundoaire.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun completeOnboarding()

    /**
     * Modo demo: Progreso lee datos de ejemplo en vez del historial real. Solo cambia
     * lo que se lee; el historial real sigue registrándose y no se toca.
     */
    val isDemoMode: Flow<Boolean>

    suspend fun setDemoMode(enabled: Boolean)

    /** Tema de la app. [ThemeMode.SYSTEM] es el predeterminado y sigue al del dispositivo. */
    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
