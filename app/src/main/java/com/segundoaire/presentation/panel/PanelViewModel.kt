package com.segundoaire.presentation.panel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.GreetingPeriod
import com.segundoaire.domain.provider.MotivationalMessageProvider
import com.segundoaire.domain.usecase.CheckAccessibilityEnabledUseCase
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import com.segundoaire.domain.usecase.GetGreetingPeriodUseCase
import com.segundoaire.domain.usecase.GetUserProfileUseCase
import com.segundoaire.domain.usecase.ToggleAppBlockStatusUseCase
import com.segundoaire.presentation.setup.AppIconLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Panel: saludo, dato de impacto, mensaje de la IA simulada, apps vigiladas y
 * permisos pendientes, en un único [PanelUiState].
 */
@HiltViewModel
class PanelViewModel @Inject constructor(
    getBlockedApps: GetBlockedAppsUseCase,
    getUserProfile: GetUserProfileUseCase,
    private val getGreetingPeriod: GetGreetingPeriodUseCase,
    private val toggleAppBlockStatus: ToggleAppBlockStatusUseCase,
    private val checkOverlayPermission: CheckOverlayPermissionUseCase,
    private val checkAccessibilityEnabled: CheckAccessibilityEnabledUseCase,
    private val messageProvider: MotivationalMessageProvider,
    private val iconLoader: AppIconLoader
) : ViewModel() {

    // Lo que cambia al volver a la app: permisos concedidos en Ajustes y la hora del saludo
    private data class Environment(
        val hasOverlayPermission: Boolean,
        val isAccessibilityEnabled: Boolean,
        val greeting: GreetingPeriod
    )

    private data class Message(val text: String? = null, val isThinking: Boolean = false)

    private val profile = getUserProfile()
    private val environment = MutableStateFlow(readEnvironment())
    private val animatedPackages = MutableStateFlow<Set<String>>(emptySet())
    private val message = MutableStateFlow(Message(isThinking = true))

    val uiState: StateFlow<PanelUiState> = combine(
        getBlockedApps(),
        profile,
        environment,
        animatedPackages,
        message
    ) { apps, profile, environment, animated, message ->
        PanelUiState(
            isLoading = false,
            greeting = environment.greeting,
            name = profile.name,
            mainGoal = profile.mainGoal,
            apps = apps,
            animatedPackages = animated,
            message = message.text,
            isThinking = message.isThinking,
            hasOverlayPermission = environment.hasOverlayPermission,
            isAccessibilityEnabled = environment.isAccessibilityEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PanelUiState())

    init {
        requestMessage()
    }

    /** Al volver de Ajustes (ON_RESUME): relee los permisos y la hora del saludo. */
    fun onResume() {
        environment.value = readEnvironment()
    }

    fun onToggleApp(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch { toggleAppBlockStatus(packageName, isBlocked) }
    }

    /** La tarjeta ya mostró la entrada de estas apps; no se repite en la sesión. */
    fun onAppsAnimated(packageNames: Collection<String>) {
        animatedPackages.update { it + packageNames }
    }

    /** Pide otro mensaje. Se ignora mientras ya está pensando uno. */
    fun onNewMessage() {
        if (message.value.isThinking) return
        message.update { it.copy(isThinking = true) }
        requestMessage()
    }

    /** Ícono de la fila cuando entra en pantalla; `null` mientras no haya o no exista. */
    suspend fun loadIcon(packageName: String): ImageBitmap? = iconLoader.load(packageName)

    private fun requestMessage() {
        val previous = message.value.text
        viewModelScope.launch {
            val goal = profile.first().mainGoal
            val next = try {
                messageProvider.nextMessage(goal, previous)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Sin mensaje nuevo se conserva el anterior: la tarjeta no queda en blanco
                null
            }
            message.value = Message(text = next ?: previous, isThinking = false)
        }
    }

    private fun readEnvironment() = Environment(
        hasOverlayPermission = checkOverlayPermission(),
        isAccessibilityEnabled = checkAccessibilityEnabled(),
        greeting = getGreetingPeriod()
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
