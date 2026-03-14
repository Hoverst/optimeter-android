package com.optimeter.app.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.presentation.auth.AuthScreen
import com.optimeter.app.presentation.auth.AuthViewModel
import com.optimeter.app.presentation.auth.SplashScreen
import com.optimeter.app.presentation.dashboard.DashboardScreen
import com.optimeter.app.presentation.history.ReadingDetailScreen
import com.optimeter.app.presentation.iot.AddDeviceScreen
import com.optimeter.app.presentation.iot.IoTDevicesScreen
import com.optimeter.app.presentation.scan.ManualEntryScreen
import com.optimeter.app.presentation.scan.ScannerScreen
import com.optimeter.app.presentation.scan.ValidationScreen

@Composable
fun OptimeterNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            // Use null as initial so splash waits for the real Firebase auth state
            // before deciding where to navigate. This prevents jumping to Auth after recreate().
            val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState(initial = null)

            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                isUserLoggedIn = isUserLoggedIn
            )
        }
        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToScanner = { meterType ->
                    navController.navigate(Screen.Scanner.createRoute(meterType.name))
                },
                onNavigateToHistoryDetail = { readingId ->
                    navController.navigate(Screen.HistoryDetail.createRoute(readingId))
                },
                onNavigateToIoTDevices = {
                    navController.navigate(Screen.IoTDevices.route)
                },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Scanner.route) { backStackEntry ->
            val meterTypeStr = backStackEntry.arguments?.getString("meterType") ?: MeterType.GAS.name
            ScannerScreen(
                meterType = MeterType.valueOf(meterTypeStr),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToValidation = { digits, encodedPhotoPath ->
                    navController.navigate(Screen.Validation.createRoute(meterTypeStr, digits, encodedPhotoPath))
                },
                onNavigateToManual = {
                    navController.navigate(Screen.ManualEntry.createRoute(meterTypeStr))
                }
            )
        }
        composable(
            route = Screen.Validation.route,
            arguments = listOf(
                navArgument("meterType") { type = NavType.StringType },
                navArgument("digits") { type = NavType.StringType },
                navArgument("photoPath") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val meterTypeStr = backStackEntry.arguments?.getString("meterType") ?: MeterType.GAS.name
            val digits = backStackEntry.arguments?.getString("digits") ?: ""
            val photoPath = backStackEntry.arguments?.getString("photoPath")
            
            ValidationScreen(
                meterType = MeterType.valueOf(meterTypeStr),
                digits = digits,
                photoPath = Uri.decode(photoPath ?: ""),
                onRetake = { navController.popBackStack() },
                onSave = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ManualEntry.route) { backStackEntry ->
            val meterTypeStr = backStackEntry.arguments?.getString("meterType") ?: MeterType.GAS.name
            ManualEntryScreen(
                meterType = MeterType.valueOf(meterTypeStr),
                onSave = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Screen.HistoryDetail.route) { backStackEntry ->
            val readingId = backStackEntry.arguments?.getString("readingId") ?: ""
            ReadingDetailScreen(
                readingId = readingId,
                onNavigateBack = { navController.popBackStack() },
                onDelete = {
                    // Call repository to delete
                }
            )
        }
        composable(Screen.IoTDevices.route) {
            IoTDevicesScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddDeviceClick = { navController.navigate(Screen.AddDevice.route) }
            )
        }
        composable(Screen.AddDevice.route) {
            AddDeviceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}
