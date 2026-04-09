package com.optimeter.app.domain.repository

import com.optimeter.app.domain.model.Home
import com.optimeter.app.domain.model.MeterReading
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    suspend fun addHome(home: Home): Result<Unit>
    suspend fun removeHome(homeId: String): Result<Unit>
    fun getHomes(): Flow<List<Home>>
    
    // Reading operations
    suspend fun saveReading(reading: MeterReading): Result<MeterReading>
    fun getLatestReadings(homeId: String): Flow<List<MeterReading>>
}