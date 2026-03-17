package com.optimeter.app.data.repository

import com.optimeter.app.data.remote.api.OptimeterApiService
import com.optimeter.app.domain.model.Home
import com.optimeter.app.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import android.util.Log

class HomeRepositoryImpl @Inject constructor(
    private val api: OptimeterApiService
) : HomeRepository {

    override suspend fun addHome(home: Home): Result<Unit> {
        return try {
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

    override suspend fun removeHome(homeId: String): Result<Unit> {
        return try {
            api.deleteHome(homeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getHomes(): Flow<List<Home>> {
        return flow {
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
                        // Parse createdAt from ISO-8601 string to milliseconds
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
        }
    }
}