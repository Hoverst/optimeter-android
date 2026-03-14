package com.optimeter.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.optimeter.app.domain.model.ThemeConfig
import com.optimeter.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_CONFIG = stringPreferencesKey("theme_config")
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        val NOTIFICATION_DAY = intPreferencesKey("notification_day")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    override val themeConfig: Flow<ThemeConfig> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            ThemeConfig.valueOf(preferences[PreferencesKeys.THEME_CONFIG] ?: ThemeConfig.FOLLOW_SYSTEM.name)
        }

    override val pushNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PUSH_NOTIFICATIONS] ?: true
        }

    override val notificationDay: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_DAY] ?: 25
        }

    override val languageCode: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] ?: "en"
        }

    override suspend fun setThemeConfig(config: ThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_CONFIG] = config.name
        }
    }

    override suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PUSH_NOTIFICATIONS] = enabled
        }
    }

    override suspend fun setNotificationDay(day: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_DAY] = day
        }
    }

    override suspend fun setLanguageCode(code: String) {
        // Save to DataStore (reactive)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = code
        }
        // Also save to SharedPreferences synchronously (commit, not apply!) so it is
        // guaranteed to be written before activity.recreate() calls attachBaseContext.
        context.getSharedPreferences("locale_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("language_code", code)
            .commit()   // synchronous — NOT apply()
    }
}
