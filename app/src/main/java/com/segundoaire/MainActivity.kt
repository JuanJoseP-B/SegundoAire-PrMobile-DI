package com.segundoaire

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.segundoaire.presentation.navigation.AppNavHost
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import dagger.hilt.android.AndroidEntryPoint

// AppCompatActivity: setApplicationLocales cambia el idioma en caliente solo con ella
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SegundoAireTheme {
                AppNavHost()
            }
        }
    }
}
