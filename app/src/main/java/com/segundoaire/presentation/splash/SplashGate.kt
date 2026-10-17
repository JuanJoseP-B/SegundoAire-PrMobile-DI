package com.segundoaire.presentation.splash

/**
 * Cuánto retiene el splash del sistema. Se queda mientras DataStore no entrega el
 * tema ni el destino inicial, para que no haya un fondo vacío ni un parpadeo de la
 * pantalla equivocada. Si algo tarda de más, se suelta pasado [MaxWaitMillis]: la
 * app no puede quedarse en blanco por una lectura lenta.
 */
object SplashGate {
    const val MaxWaitMillis = 1000L

    fun keepOnScreen(themeLoaded: Boolean, destinationLoaded: Boolean, elapsedMillis: Long): Boolean =
        !(themeLoaded && destinationLoaded) && elapsedMillis < MaxWaitMillis
}
