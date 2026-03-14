package com.optimeter.app.domain.repository

import com.optimeter.app.domain.model.MeterReading
import com.optimeter.app.domain.model.MeterType
import kotlinx.coroutines.flow.Flow

interface ReadingRepository {
    suspend fun addReading(reading: MeterReading): Result<Unit>
    suspend fun deleteReading(readingId: String): Result<Unit>
    fun getReadings(homeId: String): Flow<List<MeterReading>>
    fun getReadingsByType(homeId: String, type: MeterType): Flow<List<MeterReading>>
}
