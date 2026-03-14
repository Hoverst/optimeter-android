package com.optimeter.app.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Scanner : Screen("scanner/{meterType}") {
        fun createRoute(meterType: String) = "scanner/$meterType"
    }
    object Validation : Screen("validation/{meterType}/{digits}?photoPath={photoPath}") {
        fun createRoute(meterType: String, digits: String, photoPath: String) =
            "validation/$meterType/$digits?photoPath=$photoPath"
    }
    object ManualEntry : Screen("manual_entry/{meterType}") {
        fun createRoute(meterType: String) = "manual_entry/$meterType"
    }
    object History : Screen("history")
    object HistoryDetail : Screen("history/{readingId}") {
        fun createRoute(readingId: String) = "history/$readingId"
    }
    object Settings : Screen("settings")
    object IoTDevices : Screen("iot_devices")
    object AddDevice : Screen("add_device")
    object Statistics : Screen("statistics")
}
