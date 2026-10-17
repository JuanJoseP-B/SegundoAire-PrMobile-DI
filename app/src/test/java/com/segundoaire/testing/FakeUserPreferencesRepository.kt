package com.segundoaire.testing

import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Preferencias en memoria; el test marca el onboarding directamente o con [completeOnboarding]. */
class FakeUserPreferencesRepository(
    onboardingCompleted: Boolean = false,
    demoMode: Boolean = false,
    themeMode: ThemeMode = ThemeMode.SYSTEM
) : UserPreferencesRepository {
    private val completed = MutableStateFlow(onboardingCompleted)
    override val isOnboardingCompleted: Flow<Boolean> = completed

    override suspend fun completeOnboarding() {
        completed.value = true
    }

    val planted = MutableStateFlow<Long?>(null)
    override val plantedAtMillis: Flow<Long?> = planted

    override suspend fun markPlanted(atMillis: Long) {
        if (planted.value == null) planted.value = atMillis
    }

    val sound = MutableStateFlow(true)
    override val isSoundEnabled: Flow<Boolean> = sound

    override suspend fun setSoundEnabled(enabled: Boolean) {
        sound.value = enabled
    }

    private val demo = MutableStateFlow(demoMode)
    override val isDemoMode: Flow<Boolean> = demo

    override suspend fun setDemoMode(enabled: Boolean) {
        demo.value = enabled
    }

    private val theme = MutableStateFlow(themeMode)
    override val themeMode: Flow<ThemeMode> = theme

    override suspend fun setThemeMode(mode: ThemeMode) {
        theme.value = mode
    }
}
