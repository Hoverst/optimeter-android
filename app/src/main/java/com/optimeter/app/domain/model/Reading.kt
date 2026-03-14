package com.optimeter.app.domain.model

data class Reading(
    val id: String,
    val meterType: MeterType,
    val value: Double,
    val timestamp: Long,
    val isManualEntry: Boolean,
    val photoUrl: String? = null,
    val note: String? = null
)
