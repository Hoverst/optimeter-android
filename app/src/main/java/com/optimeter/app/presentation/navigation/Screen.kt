package com.optimeter.app.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Scanner : Screen("scanner/{meterType}?homeId={homeId}") {
        fun createRoute(meterType: String, homeId: String? = null): String {
            return if (homeId != null) {
                "scanner/$meterType?homeId=$homeId"
            } else {
                "scanner/$meterType"
            }
        }
    }
    object Validation : Screen("validation/{meterType}/{digits}?photoPath={photoPath}&homeId={homeId}") {
        fun createRoute(meterType: String, digits: String, photoPath: String, homeId: String? = null): String {
            return if (homeId != null) {
                "validation/$meterType/$digits?photoPath=$photoPath&homeId=$homeId"
            } else {
                "validation/$meterType/$digits?photoPath=$photoPath"
            }
        }
    }
    object ManualEntry : Screen("manual_entry/{meterType}?homeId={homeId}") {
        fun createRoute(meterType: String, homeId: String? = null): String {
            return if (homeId != null) {
                "manual_entry/$meterType?homeId=$homeId"
            } else {
                "manual_entry/$meterType"
            }
        }
    }
    object History : Screen("history?meterType={meterType}&homeId={homeId}") {
        fun createRoute(meterType: String? = null, homeId: String? = null): String {
            return buildString {
                append("history")
                if (meterType != null || homeId != null) {
                    append("?")
                    val params = mutableListOf<String>()
                    if (meterType != null) params.add("meterType=$meterType")
                    if (homeId != null) params.add("homeId=$homeId")
                    append(params.joinToString("&"))
                }
            }
        }
    }
    object HistoryDetail : Screen("history/{readingId}") {
        fun createRoute(readingId: String) = "history/$readingId"
    }
    object Settings : Screen("settings")
    object IoTDevices : Screen("iot_devices")
    object AddDevice : Screen("add_device")
    object Statistics : Screen("statistics")
}