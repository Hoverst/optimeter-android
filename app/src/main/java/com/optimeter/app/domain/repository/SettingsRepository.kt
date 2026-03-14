package com.optimeter.app.domain.repository

import com.optimeter.app.domain.model.ThemeConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeConfig: Flow<ThemeConfig>
    val pushNotificationsEnabled: Flow<Boolean>
    val notificationDay: Flow<Int>
    val languageCode: Flow<String>

    suspend fun setThemeConfig(config: ThemeConfig)
    suspend fun setPushNotificationsEnabled(enabled: Boolean)
    suspend fun setNotificationDay(day: Int)
    suspend fun setLanguageCode(code: String)
}
