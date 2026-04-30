package com.optimeter.app.presentation.dashboard.tabs

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import kotlinx.coroutines.launch
import com.optimeter.app.R
import com.optimeter.app.domain.model.ThemeConfig
import com.optimeter.app.domain.model.Home
import androidx.compose.ui.graphics.Color
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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showReminderDayDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddHomeDialog by remember { mutableStateOf(false) }
    var showDeleteHomeDialog by remember { mutableStateOf<Home?>(null) }
    var homeToEdit by remember { mutableStateOf<Home?>(null) }
    val homeUiState by homeViewModel.uiState.collectAsState()

    // --- Dialogs ---
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.choose_theme)) },
            text = {
                Column {
                    listOf(
                        ThemeConfig.FOLLOW_SYSTEM to stringResource(R.string.system_default),
                        ThemeConfig.LIGHT to stringResource(R.string.light),
                        ThemeConfig.DARK to stringResource(R.string.dark)
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
                TextButton(onClick = { showThemeDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.choose_language)) },
            text = {
                Column {
                    listOf(
                        "en" to "English",
                        "uk" to "Українська"
                    ).forEach { (code, lang) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguageCode(code)
                                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                                        androidx.core.os.LocaleListCompat.forLanguageTags(code)
                                    )
                                    showLanguageDialog = false
                                    
                                    val activity = (context as? android.app.Activity) ?: (context as? android.content.ContextWrapper)?.baseContext as? android.app.Activity
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = languageCode == code,
                                onClick = {
                                    viewModel.setLanguageCode(code)
                                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                                        androidx.core.os.LocaleListCompat.forLanguageTags(code)
                                    )
                                    showLanguageDialog = false
                                    
                                    val activity = (context as? android.app.Activity) ?: (context as? android.content.ContextWrapper)?.baseContext as? android.app.Activity
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(lang, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showReminderDayDialog) {
        var dayText by remember { mutableStateOf(notificationDay.toString()) }
        AlertDialog(
            onDismissRequest = { showReminderDayDialog = false },
            title = { Text(stringResource(R.string.set_reminder_day)) },
            text = {
                Column {
                    Text(stringResource(R.string.reminder_day_hint))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { if (it.length <= 2) dayText = it },
                        label = { Text(stringResource(R.string.day_of_month)) },
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
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showReminderDayDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.dialog_logout_title)) },
            text = { Text(stringResource(R.string.dialog_logout_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            try {
                                viewModel.logout(context)
                                onLogout()
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: "Logout failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_logout_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        var confirmEmailInput by remember { mutableStateOf("") }
        val actualEmail = viewModel.currentUserEmail ?: ""
        val isEmailMatch = confirmEmailInput.trim().equals(actualEmail, ignoreCase = true)
        
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_account_title)) },
            text = { 
                Column {
                    Text(stringResource(R.string.dialog_delete_account_message) + "\n\n" + stringResource(R.string.dialog_type_to_confirm, actualEmail))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmEmailInput,
                        onValueChange = { confirmEmailInput = it },
                        label = { Text(stringResource(R.string.dialog_delete_account_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
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
                    },
                    enabled = isEmailMatch,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showAddHomeDialog) {
        var newHomeName by remember { mutableStateOf("") }
        var newHomeAddress by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddHomeDialog = false },
            title = { Text(stringResource(R.string.dialog_add_home_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newHomeName,
                        onValueChange = { newHomeName = it },
                        label = { Text(stringResource(R.string.dialog_add_home_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newHomeAddress,
                        onValueChange = { newHomeAddress = it },
                        label = { Text(stringResource(R.string.dialog_add_home_address)) },
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
                    Text(stringResource(R.string.action_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHomeDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (homeToEdit != null) {
        val home = homeToEdit!!
        var editHomeName by remember(home) { mutableStateOf(home.name) }
        var editHomeAddress by remember(home) { mutableStateOf(home.address) }
        AlertDialog(
            onDismissRequest = { homeToEdit = null },
            title = { Text(stringResource(R.string.dialog_edit_home_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editHomeName,
                        onValueChange = { editHomeName = it },
                        label = { Text(stringResource(R.string.dialog_add_home_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editHomeAddress,
                        onValueChange = { editHomeAddress = it },
                        label = { Text(stringResource(R.string.dialog_add_home_address)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        homeViewModel.updateHome(
                            home.copy(
                                name = editHomeName,
                                address = editHomeAddress
                            )
                        )
                        homeToEdit = null
                    },
                    enabled = editHomeName.isNotBlank() && editHomeAddress.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { homeToEdit = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showDeleteHomeDialog != null) {
        val home = showDeleteHomeDialog!!
        var confirmHomeInput by remember { mutableStateOf("") }
        val isHomeMatch = confirmHomeInput.trim() == home.name.trim()
        
        val ctx = LocalContext.current
        Log.d("LOCALE_CHECK", ctx.resources.configuration.locales[0].toString())

        AlertDialog(
            onDismissRequest = { showDeleteHomeDialog = null },
            title = { Text(stringResource(R.string.dialog_delete_home_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.dialog_delete_home_message, home.name) + "\n\n" + stringResource(R.string.dialog_type_to_confirm, home.name))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmHomeInput,
                        onValueChange = { confirmHomeInput = it },
                        label = { Text(stringResource(R.string.dialog_delete_home_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
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
                    },
                    enabled = isHomeMatch,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteHomeDialog = null }) { Text(stringResource(R.string.action_cancel)) }
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
        else -> languageCode.uppercase()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Account Profile Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.account),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.currentUserEmail ?: "Unknown User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // My Homes Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.my_homes),
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
                    Text(stringResource(R.string.add_home_button), fontWeight = FontWeight.Medium)
                }
            }
        }

        // Homes List / Empty State
        item {
            val homes = homeUiState.homes
            if (homes.isEmpty()) {
                // Empty state: No homes
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_homes_yet),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_homes_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Display all homes in a vertical list
                homes.forEach { home ->
                    val isActive = home.id == homeUiState.selectedHomeId
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { homeViewModel.selectHome(home.id) }
                                        .background(Color.Transparent)
                                        .then(
                                            if (isActive) {
                                                Modifier.border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(8.dp))
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Home,
                                        contentDescription = "Select Home",
                                        tint = if (isActive) Color.White else Color(0xFF9E9E9E),
                                        modifier = Modifier.size(if (isActive) 28.dp else 24.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { homeToEdit = home },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                        .size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit Home",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = home.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = home.address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Delete button for each home
                                IconButton(
                                    onClick = {
                                        home.takeIf { it.id.isNotEmpty() }?.let { h ->
                                            showDeleteHomeDialog = h
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

                    Spacer(modifier = Modifier.height(8.dp))
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
                        stringResource(R.string.about),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Optimeter v1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.track_utility_consumption),
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
                onClick = {
                    Toast.makeText(context, "This feature will be added in future updates", Toast.LENGTH_SHORT).show()
                },
                enabled = false
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
                subtitle = stringResource(R.string.terms_of_use),
                onClick = { /* Could open WebView or browser Intent */ }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            OutlinedButton(
                onClick = { showLogoutDialog = true },
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
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
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.38f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            modifier = Modifier.size(24.dp),
            tint = androidx.compose.material3.LocalContentColor.current.copy(alpha = alpha)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium,
                color = androidx.compose.material3.LocalContentColor.current.copy(alpha = alpha)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
        )
    }
}
