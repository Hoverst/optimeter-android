package com.optimeter.app.domain.model

data class Device(
    val id: String,
    val name: String,
    val meterType: MeterType,
    val isOnline: Boolean,
    val batteryPercentage: Int
)
