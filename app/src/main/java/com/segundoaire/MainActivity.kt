package com.segundoaire

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segundoaire.presentation.designsystem.theme.DarkSegundoAireColors
import com.segundoaire.presentation.designsystem.theme.LightSegundoAireColors
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.isDark
import com.segundoaire.presentation.navigation.AppRoot
import com.segundoaire.presentation.navigation.AppViewModel
import com.segundoaire.presentation.navigation.ThemeViewModel
import com.segundoaire.presentation.splash.SplashGate
import dagger.hilt.android.AndroidEntryPoint

// AppCompatActivity: setApplicationLocales cambia el idioma en caliente solo con ella
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    // Misma instancia que usa AppRoot: la actividad es su dueña
    private val appViewModel: AppViewModel by viewModels()

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
                    AppRoot(appViewModel)
                }
            }
        }
    }

    private companion object {
        // Mismo velo translúcido que el predeterminado de enableEdgeToEdge, con los colores del tema
        const val NAV_SCRIM_LIGHT_ALPHA = 0.9f
        const val NAV_SCRIM_DARK_ALPHA = 0.5f
    }
}
