package com.optimeter.app.data.repository

import com.optimeter.app.data.remote.api.OptimeterApiService
import com.optimeter.app.domain.model.Home
import com.optimeter.app.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

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
        // Backend currently returns homes for all users.
        return flow {
            val homes = api.getHomes().map { dto ->
                Home(
                    id = dto.id,
                    userId = "",
                    name = dto.name,
                    address = dto.address,
                    // createdAt comes as ISO-8601 string; we keep the
                    // original default if parsing fails.
                    createdAt = System.currentTimeMillis()
                )
            }
            emit(homes)
        }
    }
}
