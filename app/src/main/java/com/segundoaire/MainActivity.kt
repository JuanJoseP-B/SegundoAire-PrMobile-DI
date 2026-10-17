package com.segundoaire

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segundoaire.presentation.designsystem.theme.DarkSegundoAireColors
import com.segundoaire.presentation.designsystem.theme.LightSegundoAireColors
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.isDark
import com.segundoaire.presentation.feedback.LocalSoundManager
import com.segundoaire.presentation.navigation.AppRoot
import com.segundoaire.presentation.navigation.AppViewModel
import com.segundoaire.presentation.navigation.ThemeViewModel
import com.segundoaire.presentation.onboarding.sound.SoundManager
import com.segundoaire.presentation.splash.SplashGate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// AppCompatActivity: setApplicationLocales cambia el idioma en caliente solo con ella
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    // Misma instancia que usa AppRoot: la actividad es su dueña
    private val appViewModel: AppViewModel by viewModels()

    // Sonido de la app para las pantallas (LocalSoundManager)
    @Inject lateinit var sound: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Antes de super: cambia el tema de arranque por el de la app
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // El splash se queda hasta que DataStore entrega el tema y el destino inicial, máximo 1 s
        val startedAt = SystemClock.elapsedRealtime()
        splash.setKeepOnScreenCondition {
            SplashGate.keepOnScreen(
                themeLoaded = themeViewModel.themeMode.value != null,
                destinationLoaded = appViewModel.startDestination.value != null,
                elapsedMillis = SystemClock.elapsedRealtime() - startedAt
            )
        }
        enableEdgeToEdge()
        handleOpenPanel(intent)
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            // null = leyendo DataStore: no se pinta nada para no mostrar un tema que luego cambie
            themeMode?.let { mode ->
                val darkTheme = mode.isDark()

                // Los íconos de las barras del sistema deben seguir al tema elegido, no al del dispositivo
                DisposableEffect(darkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                            detectDarkMode = { darkTheme }
                        ),
                        navigationBarStyle = SystemBarStyle.auto(
                            LightSegundoAireColors.background.copy(alpha = NAV_SCRIM_LIGHT_ALPHA).toArgb(),
                            DarkSegundoAireColors.background.copy(alpha = NAV_SCRIM_DARK_ALPHA).toArgb(),
                            detectDarkMode = { darkTheme }
                        )
                    )
                    onDispose {}
                }

                SegundoAireTheme(darkTheme = darkTheme) {
                    CompositionLocalProvider(LocalSoundManager provides sound) {
                        AppRoot(appViewModel)
                    }
                }
            }
        }
    }

    // El GlassWall abre la actividad con "Volver a mi bloque": se muestra el Panel
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOpenPanel(intent)
    }

    private fun handleOpenPanel(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_PANEL, false) == true) {
            intent.removeExtra(EXTRA_OPEN_PANEL)
            appViewModel.requestOpenPanel()
        }
    }

    companion object {
        /** Extra del GlassWall: al abrir, mostrar el Panel. */
        const val EXTRA_OPEN_PANEL = "com.segundoaire.extra.OPEN_PANEL"

        // Mismo velo translúcido que el predeterminado de enableEdgeToEdge, con los colores del tema
        private const val NAV_SCRIM_LIGHT_ALPHA = 0.9f
        private const val NAV_SCRIM_DARK_ALPHA = 0.5f
    }
}
