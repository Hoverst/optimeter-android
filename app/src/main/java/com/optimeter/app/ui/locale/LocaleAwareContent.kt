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
 * The wrapper keeps the **Activity context** as the base of the ContextWrapper
 * (required by Hilt, Navigation, etc.) but overrides [getResources] and
 * [getAssets] so that resource look-ups use the correct locale.
 *
 * The `remember` block is keyed on [languageCode], [currentContext], AND
 * [currentConfiguration] so that locale changes triggered by
 * `AppCompatDelegate.setApplicationLocales()` also invalidate the cached
 * context and recompose everything with the new strings.
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

    val (localizedContext, localizedConfig) = remember(languageCode, currentContext, currentConfiguration) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(currentConfiguration)
        config.setLocale(locale)
        val newContext = currentContext.createConfigurationContext(config)

        // Keep the Activity as the base so Hilt / Navigation can find it,
        // but serve localized resources and assets.
        val wrapper = object : ContextWrapper(currentContext) {
            override fun getResources() = newContext.resources
            override fun getAssets() = newContext.assets
        }
        wrapper to config
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfig
    ) {
        content()
    }
}
