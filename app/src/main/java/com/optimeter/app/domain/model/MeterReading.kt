package com.optimeter.app.domain.model

data class MeterReading(
    val id: String = "",
    val homeId: String = "", // Associates this reading to a specific Home
    val userId: String = "", // For security rules validation
    val type: MeterType = MeterType.ELECTRICITY,
    val value: Double = 0.0,
    val readingDate: Long = System.currentTimeMillis(), // Formatted timestamp
    val imageUrl: String? = null // Optional URL if an OCR photo was attached
)
