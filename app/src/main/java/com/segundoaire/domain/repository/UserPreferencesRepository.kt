package com.segundoaire.domain.repository

import com.segundoaire.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun completeOnboarding()

    /**
     * Instante (ms desde epoch) en que se plantó el árbol, es decir, en que se completó el
     * onboarding. `null` en instalaciones de antes de la Fase 17, que no lo guardaron.
     */
    val plantedAtMillis: Flow<Long?>

    /** Guarda el instante de plantado solo si no había uno: repetir el onboarding no lo cambia. */
    suspend fun markPlanted(atMillis: Long)

    /** Sonidos y vibración de la app (Perfil). Encendidos por defecto. */
    val isSoundEnabled: Flow<Boolean>

    suspend fun setSoundEnabled(enabled: Boolean)

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
