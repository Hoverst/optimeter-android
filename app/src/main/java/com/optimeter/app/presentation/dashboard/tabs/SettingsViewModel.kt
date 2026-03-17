package com.optimeter.app.presentation.dashboard.tabs

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optimeter.app.auth.AuthConstants
import com.optimeter.app.domain.model.ThemeConfig
import com.optimeter.app.domain.repository.AuthRepository
import com.optimeter.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

    suspend fun logout(context: Context): Result<Unit> {
        return try {
            // If the user used Google Sign-In, sign out from Google too.
            val googleClient = GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(AuthConstants.GOOGLE_WEB_CLIENT_ID)
                    .requestEmail()
                    .build()
            )
            runCatching { googleClient.signOut().await() }
            authRepository.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(context: Context): Result<Unit> {
        val uid = authRepository.currentUserId ?: return Result.failure(IllegalStateException("No authenticated user"))
        return try {
            // a) delete Firestore data first
            authRepository.deleteUserDataFromFirestore(uid).getOrThrow()
            // b) then delete auth user
            authRepository.deleteCurrentUserFromAuth().getOrThrow()

            // Sign out locally (also clears Google cached state)
            logout(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isRecentLoginRequiredError(t: Throwable): Boolean {
        return t is FirebaseAuthRecentLoginRequiredException
    }
}
