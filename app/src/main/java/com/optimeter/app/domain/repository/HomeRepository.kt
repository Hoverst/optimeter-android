package com.optimeter.app.domain.repository

import com.optimeter.app.domain.model.Home
import com.optimeter.app.domain.model.MeterReading
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    suspend fun addHome(home: Home): Result<Unit>
    suspend fun removeHome(homeId: String): Result<Unit>
    val refreshTrigger: kotlinx.coroutines.flow.SharedFlow<Unit>

    fun getHomes(): Flow<List<Home>>
    
    // Reading operations
    suspend fun saveReading(reading: MeterReading): Result<MeterReading>
    suspend fun deleteReading(readingId: String): Result<Unit>
    fun getLatestReadings(homeId: String): Flow<List<MeterReading>>
    fun getReadings(homeId: String): Flow<List<MeterReading>>
}
