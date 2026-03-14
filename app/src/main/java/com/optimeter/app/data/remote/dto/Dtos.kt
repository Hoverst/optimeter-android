package com.optimeter.app.data.remote.dto

data class HomeResponseDto(
    val id: String,
    val name: String,
    val address: String,
    val createdAt: String
)

data class CreateHomeRequestDto(
    val name: String,
    val address: String
)

data class MeterReadingResponseDto(
    val id: String,
    val homeId: String,
    val utility: String,
    val value: Double,
    val readingAt: String,
    val createdAt: String
)

data class CreateReadingRequestDto(
    val homeId: String,
    val utility: String,
    val value: Double,
    val readingAt: String
)

