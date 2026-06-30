package com.latido.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.latido.app.ui.LatidoApp
import com.latido.app.ui.theme.LatidoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * AppCompatActivity (not plain ComponentActivity) so per-app language switching via
 * AppCompatDelegate.setApplicationLocales works on every supported Android version.
 * It still hosts Compose through setContent.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LatidoTheme {
                LatidoApp()
            }
        }
    }
}
