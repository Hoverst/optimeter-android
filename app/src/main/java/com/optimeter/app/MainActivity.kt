package com.optimeter.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.optimeter.app.domain.model.ThemeConfig
import com.optimeter.app.domain.repository.SettingsRepository
import com.optimeter.app.presentation.navigation.OptimeterNavGraph
import com.optimeter.app.ui.theme.OptimeterTheme
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeConfig by settingsRepository.themeConfig.collectAsState(initial = ThemeConfig.FOLLOW_SYSTEM)

            // Read the initial language from AppCompatDelegate (which persists
            // the locale across Activity recreations) so that the very first
            // frame already uses the correct language — not "en".
            val appLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
            val systemLang = if (!appLocales.isEmpty) {
                appLocales[0]?.language ?: "en"
            } else {
                resources.configuration.locales[0].language
            }
            val languageCode by settingsRepository.languageCode.collectAsState(initial = systemLang)

            val isDarkTheme = when (themeConfig) {
                ThemeConfig.DARK -> true
                ThemeConfig.LIGHT -> false
                ThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            }

            com.optimeter.app.ui.locale.LocaleAwareContent(languageCode = languageCode) {
                OptimeterTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                    val navController = rememberNavController()
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        OptimeterNavGraph(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
