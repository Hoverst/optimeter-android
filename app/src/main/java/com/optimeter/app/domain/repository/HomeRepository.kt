package com.optimeter.app.domain.repository

import com.optimeter.app.domain.model.Home
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    suspend fun addHome(home: Home): Result<Unit>
    suspend fun removeHome(homeId: String): Result<Unit>
    fun getHomes(userId: String): Flow<List<Home>>
}
