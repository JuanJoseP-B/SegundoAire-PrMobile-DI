package com.segundoaire.presentation.onboarding.sound

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Da a la pantalla el [SoundManager] de la aplicación. No guarda estado propio. */
@HiltViewModel
class OnboardingFeedbackViewModel @Inject constructor(
    val sound: SoundManager
) : ViewModel()
