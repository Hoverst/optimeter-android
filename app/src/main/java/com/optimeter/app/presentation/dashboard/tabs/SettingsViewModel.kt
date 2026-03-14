package com.optimeter.app.presentation.dashboard.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optimeter.app.domain.model.ThemeConfig
import com.optimeter.app.domain.repository.AuthRepository
import com.optimeter.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUserEmail: String?
        get() = authRepository.currentUserEmail
    
    val currentUserId: String?
        get() = authRepository.currentUserId

    val themeConfig: StateFlow<ThemeConfig> = settingsRepository.themeConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeConfig.FOLLOW_SYSTEM
        )

    val pushNotificationsEnabled: StateFlow<Boolean> = settingsRepository.pushNotificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val notificationDay: StateFlow<Int> = settingsRepository.notificationDay
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 25
        )

    val languageCode: StateFlow<String> = settingsRepository.languageCode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "en"
        )

    fun setThemeConfig(config: ThemeConfig) {
        viewModelScope.launch {
            settingsRepository.setThemeConfig(config)
        }
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPushNotificationsEnabled(enabled)
        }
    }

    fun setNotificationDay(day: Int) {
        viewModelScope.launch {
            settingsRepository.setNotificationDay(day)
        }
    }

    fun setLanguageCode(code: String) {
        viewModelScope.launch {
            settingsRepository.setLanguageCode(code)
        }
    }
}
