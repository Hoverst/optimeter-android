package com.optimeter.app.ui.locale

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Wraps [content] with a locale-configured [LocalContext] so that all
 * [androidx.compose.ui.res.stringResource] calls inside pick up the correct
 * translations — WITHOUT calling Activity.recreate().
 *
 * Navigation state, scroll positions and back stack are preserved.
 */
@Composable
fun LocaleAwareContent(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val currentContext = LocalContext.current
    val currentConfiguration = LocalConfiguration.current

    val localizedContext = remember(languageCode, currentContext) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(currentConfiguration)
        config.setLocale(locale)
        val newContext = currentContext.createConfigurationContext(config)

        object : ContextWrapper(currentContext) {
            override fun getResources() = newContext.resources
            override fun getAssets() = newContext.assets
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration
    ) {
        content()
    }
}
