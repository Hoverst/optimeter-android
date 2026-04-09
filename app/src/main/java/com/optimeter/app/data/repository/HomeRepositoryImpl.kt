package com.optimeter.app.data.repository

import com.optimeter.app.data.remote.api.OptimeterApiService
import com.optimeter.app.domain.model.Home
import com.optimeter.app.domain.model.MeterReading
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.domain.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import android.util.Log

class HomeRepositoryImpl @Inject constructor(
    private val api: OptimeterApiService
) : HomeRepository {

    override suspend fun addHome(home: Home): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.addHome(
                request = com.optimeter.app.data.remote.dto.CreateHomeRequestDto(
                    name = home.name,
                    address = home.address
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeHome(homeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.deleteHome(homeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getHomes(): Flow<List<Home>> = flow {
        try {
            Log.d("HomeRepository", "Fetching homes from API...")
            val response = api.getHomes()
            Log.d("HomeRepository", "Raw API response type: ${response::class.simpleName}, size: ${response.size}")
            response.forEachIndexed { index, dto ->
                Log.d("HomeRepository", "DTO[$index]: id=${dto.id}, userId='${dto.userId}', name='${dto.name}', address='${dto.address}', createdAt='${dto.createdAt}'")
            }
            
            val homes = response.map { dto ->
                Log.d("HomeRepository", "Mapping DTO: $dto")
                Home(
                    id = dto.id,
                    userId = dto.userId,
                    name = dto.name,
                    address = dto.address,
                    createdAt = try {
                        val parsed = java.time.Instant.parse(dto.createdAt).toEpochMilli()
                        Log.d("HomeRepository", "Parsed createdAt: ${dto.createdAt} -> $parsed")
                        parsed
                    } catch (e: Exception) {
                        Log.w("HomeRepository", "Failed to parse createdAt: ${dto.createdAt}", e)
                        System.currentTimeMillis()
                    }
                )
            }
            Log.d("HomeRepository", "Emitting ${homes.size} homes: $homes")
            emit(homes)
        } catch (e: Exception) {
            Log.e("HomeRepository", "Error fetching homes", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun saveReading(reading: MeterReading): Result<MeterReading> = withContext(Dispatchers.IO) {
        try {
            val response = api.addReading(
                request = com.optimeter.app.data.remote.dto.CreateReadingRequestDto(
                    homeId = reading.homeId,
                    utility = reading.type.name.lowercase(),
                    value = reading.value,
                    readingAt = Instant.ofEpochMilli(reading.readingDate).toString()
                )
            )
            val savedReading = MeterReading(
                id = response.id,
                homeId = response.homeId,
                userId = reading.userId,
                type = MeterType.valueOf(response.utility.uppercase()),
                value = response.value,
                readingDate = Instant.parse(response.readingAt).toEpochMilli(),
                imageUrl = reading.imageUrl
            )
            Result.success(savedReading)
        } catch (e: Exception) {
            Log.e("HomeRepository", "Error saving reading", e)
            Result.failure(e)
        }
    }

    override fun getLatestReadings(homeId: String): Flow<List<MeterReading>> = flow {
        try {
            Log.d("HomeRepository", "Fetching latest readings for homeId: $homeId")
            val response = api.getLatestReadings(homeId)
            Log.d("HomeRepository", "Raw latest readings response: ${response.size} items")
            
            val readings = response.map { dto ->
                Log.d("HomeRepository", "Mapping reading DTO: $dto")
                MeterReading(
                    id = dto.id,
                    homeId = dto.homeId,
                    userId = "", // Will be populated if needed
                    type = MeterType.valueOf(dto.utility.uppercase()),
                    value = dto.value,
                    readingDate = Instant.parse(dto.readingAt).toEpochMilli(),
                    imageUrl = null
                )
            }
            Log.d("HomeRepository", "Emitting ${readings.size} latest readings")
            emit(readings)
        } catch (e: Exception) {
            Log.e("HomeRepository", "Error fetching latest readings", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)
}