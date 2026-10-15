package com.segundoaire.core.profile

import com.segundoaire.di.ApplicationScope
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.GetUserProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Perfil del usuario en memoria para el GlassWall. Mostrar el overlay está en el
 * camino crítico del servicio, así que el nombre y el objetivo se leen de
 * `profile.value`: DataStore se observa desde que se crea esta clase (al conectar
 * el servicio), nunca en el momento de interceptar.
 */
@Singleton
class UserProfileCache @Inject constructor(
    getUserProfile: GetUserProfileUseCase,
    @ApplicationScope scope: CoroutineScope
) {
    // Si DataStore falla, el overlay sigue saliendo con el mensaje sin nombre
    val profile: StateFlow<UserProfile> = getUserProfile()
        .catch { emit(UserProfile()) }
        .stateIn(scope, SharingStarted.Eagerly, UserProfile())
}
