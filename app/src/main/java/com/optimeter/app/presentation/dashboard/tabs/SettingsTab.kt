package com.optimeter.app.presentation.dashboard.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import kotlinx.coroutines.launch
import com.optimeter.app.R
import com.optimeter.app.domain.model.ThemeConfig
import com.optimeter.app.domain.model.Home
import com.optimeter.app.presentation.dashboard.tabs.HomeViewModel

@Composable
fun SettingsTab(
    onNavigateToIoTDevices: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val themeConfig by viewModel.themeConfig.collectAsState()
    val pushEnabled by viewModel.pushNotificationsEnabled.collectAsState()
    val notificationDay by viewModel.notificationDay.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState()

    val userEmail = viewModel.currentUserEmail ?: "Not signed in"
    val userId = viewModel.currentUserId ?: ""
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showReminderDayDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddHomeDialog by remember { mutableStateOf(false) }
    var showDeleteHomeDialog by remember { mutableStateOf<Home?>(null) }
    val homeUiState by homeViewModel.uiState.collectAsState()

    // --- Dialogs ---
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    listOf(
                        ThemeConfig.FOLLOW_SYSTEM to "System Default",
                        ThemeConfig.LIGHT to "Light",
                        ThemeConfig.DARK to "Dark"
                    ).forEach { (config, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeConfig(config)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeConfig == config,
                                onClick = {
                                    viewModel.setThemeConfig(config)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Choose Language") },
            text = {
                Column {
                    listOf(
                        "en" to "English",
                        "uk" to "Українська",
                        "tr" to "Türkçe",
                        "de" to "Deutsch",
                        "fr" to "Français",
                        "es" to "Español",
                        "ar" to "العربية"
                    ).forEach { (code, lang) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguageCode(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = languageCode == code,
                                onClick = {
                                    // Just update DataStore via ViewModel (async, for persistence)
                                    viewModel.setLanguageCode(code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(lang, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showReminderDayDialog) {
        var dayText by remember { mutableStateOf(notificationDay.toString()) }
        AlertDialog(
            onDismissRequest = { showReminderDayDialog = false },
            title = { Text("Set Reminder Day") },
            text = {
                Column {
                    Text("Enter a day (1-28) for monthly reminder:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { if (it.length <= 2) dayText = it },
                        label = { Text("Day of Month") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val day = dayText.toIntOrNull()?.coerceIn(1, 28) ?: notificationDay
                    viewModel.setNotificationDay(day)
                    showReminderDayDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderDayDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure? This will permanently delete your account and all your data. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        scope.launch {
                            try {
                                viewModel.deleteAccount(context)
                                onDeleteAccount()
                            } catch (e: Exception) {
                                val msg = if (viewModel.isRecentLoginRequiredError(e)) {
                                    "Please log out and log back in to verify your identity before deleting."
                                } else {
                                    e.message ?: "Delete account failed"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showAddHomeDialog) {
        var newHomeName by remember { mutableStateOf("") }
        var newHomeAddress by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddHomeDialog = false },
            title = { Text("Add New Home") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newHomeName,
                        onValueChange = { newHomeName = it },
                        label = { Text("Home Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newHomeAddress,
                        onValueChange = { newHomeAddress = it },
                        label = { Text("Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        homeViewModel.addHome(
                            name = newHomeName,
                            address = newHomeAddress
                        )
                        showAddHomeDialog = false
                    },
                    enabled = newHomeName.isNotBlank() && newHomeAddress.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHomeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteHomeDialog != null) {
        val home = showDeleteHomeDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteHomeDialog = null },
            title = { Text("Delete Home?") },
            text = { Text("Are you sure you want to delete ${home.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteHomeDialog = null
                        scope.launch {
                            try {
                                homeViewModel.removeHome(home.id)
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: "Delete failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteHomeDialog = null }) { Text("Cancel") }
            }
        )
    }

    // --- Main Content ---
    val themeLabel = when (themeConfig) {
        ThemeConfig.FOLLOW_SYSTEM -> stringResource(R.string.system_default)
        ThemeConfig.LIGHT -> stringResource(R.string.light)
        ThemeConfig.DARK -> stringResource(R.string.dark)
    }

    val languageLabel = when (languageCode) {
        "en" -> "English"
        "uk" -> "Українська"
        "tr" -> "Türkçe"
        "de" -> "Deutsch"
        "fr" -> "Français"
        "es" -> "Español"
        "ar" -> "العربية"
        else -> languageCode.uppercase()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp), // DashboardScreen already handles bottom padding
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // My Homes Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Homes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                OutlinedButton(
                    onClick = { showAddHomeDialog = true },
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("+ Add Home", fontWeight = FontWeight.Medium)
                }
            }
        }

        // Active Home Card (first home if available)
        item {
            val activeHome = homeUiState.homes.firstOrNull()
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Home Icon Box
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(com.optimeter.app.ui.theme.Chart1.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = null,
                                tint = com.optimeter.app.ui.theme.Chart1,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    activeHome?.name ?: "My Home",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Active Badge
                                Box(
                                    modifier = Modifier
                                        .background(com.optimeter.app.ui.theme.Chart1.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "Active",
                                        color = com.optimeter.app.ui.theme.Chart1,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                activeHome?.address ?: "123 Main Street, Apt 4B",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Delete Button
                    IconButton(
                        onClick = {
                            activeHome?.takeIf { it.id.isNotEmpty() }?.let { home ->
                                showDeleteHomeDialog = home
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp))
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // About Card
        item {
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Utility Meter Reader v1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Track your utility consumption with ease",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { SettingsSectionTitle(stringResource(R.string.my_iot_devices)) }
        item {
            SettingsItem(
                icon = Icons.Default.DevicesOther,
                title = stringResource(R.string.my_devices),
                subtitle = stringResource(R.string.manage_smart_meters),
                onClick = onNavigateToIoTDevices
            )
        }

        item { SettingsSectionTitle(stringResource(R.string.appearance_localization)) }
        item {
            SettingsItem(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.theme),
                subtitle = themeLabel,
                onClick = { showThemeDialog = true }
            )
        }
        item {
            SettingsItem(
                icon = Icons.Default.Language,
                title = stringResource(R.string.language),
                subtitle = languageLabel,
                onClick = { showLanguageDialog = true }
            )
        }

        item { SettingsSectionTitle(stringResource(R.string.notifications)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.push_notifications), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.push_notifications_subtitle), style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = pushEnabled,
                    onCheckedChange = { viewModel.setPushNotificationsEnabled(it) }
                )
            }
        }
        item {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.reminder_date),
                subtitle = stringResource(R.string.reminder_day_subtitle, notificationDay),
                onClick = { showReminderDayDialog = true }
            )
        }

        item { SettingsSectionTitle(stringResource(R.string.about)) }
        item {
            SettingsItem(
                icon = Icons.Default.Policy,
                title = stringResource(R.string.privacy_policy),
                subtitle = stringResource(R.string.privacy_policy_subtitle),
                onClick = { /* Could open WebView or browser Intent */ }
            )
        }
        item {
            SettingsItem(
                icon = Icons.Default.Policy,
                title = stringResource(R.string.terms_of_use),
                subtitle = stringResource(R.string.terms_subtitle),
                onClick = { /* Could open WebView or browser Intent */ }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            viewModel.logout(context)
                            onLogout()
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message ?: "Logout failed", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.log_out))
            }
        }

        item {
            Button(
                onClick = { showDeleteConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.delete_account))
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp, top = 12.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}