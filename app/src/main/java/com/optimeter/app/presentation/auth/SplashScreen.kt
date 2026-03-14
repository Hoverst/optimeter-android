package com.optimeter.app.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.optimeter.app.R

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    isUserLoggedIn: Boolean?   // null = still loading from Firebase
) {
    // Only navigate once Firebase has confirmed auth state (non-null).
    // If logged in → go directly to Dashboard (no delay, important after recreate).
    // If not logged in → show splash logo for 1.5s then go to Auth.
    LaunchedEffect(isUserLoggedIn) {
        when (isUserLoggedIn) {
            true -> onNavigateToDashboard()        // immediate — user already authenticated
            false -> {
                kotlinx.coroutines.delay(1500)     // only delay on fresh launch
                onNavigateToAuth()
            }
            null -> { /* still waiting for Firebase; do nothing */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Optimeter Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}
