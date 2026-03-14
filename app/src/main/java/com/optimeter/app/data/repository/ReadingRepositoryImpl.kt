package com.optimeter.app.data.repository

import com.optimeter.app.data.remote.api.OptimeterApiService
import com.optimeter.app.data.remote.dto.CreateReadingRequestDto
import com.optimeter.app.domain.model.MeterReading
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ReadingRepositoryImpl @Inject constructor(
    private val api: OptimeterApiService
) : ReadingRepository {

    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

    override suspend fun addReading(reading: MeterReading): Result<Unit> {
        return try {
            val request = CreateReadingRequestDto(
                homeId = reading.homeId,
                utility = reading.type.name.lowercase(),
                value = reading.value,
                readingAt = isoFormatter.format(
                    Instant.ofEpochMilli(reading.readingDate).atOffset(ZoneOffset.UTC)
                )
            )
            api.addReading(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReading(readingId: String): Result<Unit> {
        return try {
            api.deleteReading(readingId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getReadings(homeId: String): Flow<List<MeterReading>> {
        return flow {
            val readings = api.getReadings(homeId = homeId, utility = null).map { dto ->
                MeterReading(
                    id = dto.id,
                    homeId = dto.homeId,
                    userId = "", // can be populated once backend adds auth
                    type = dto.utility.uppercase().let { utility ->
                        MeterType.values().firstOrNull { it.name == utility } ?: MeterType.ELECTRICITY
                    },
                    value = dto.value,
                    readingDate = try {
                        Instant.parse(dto.readingAt).toEpochMilli()
                    } catch (_: Exception) {
                        System.currentTimeMillis()
                    },
                    imageUrl = null
                )
            }
            emit(readings)
        }
    }

    override fun getReadingsByType(homeId: String, type: MeterType): Flow<List<MeterReading>> {
        return flow {
            val readings = api.getReadings(
                homeId = homeId,
                utility = type.name.lowercase()
            ).map { dto ->
                MeterReading(
                    id = dto.id,
                    homeId = dto.homeId,
                    userId = "",
                    type = type,
                    value = dto.value,
                    readingDate = try {
                        Instant.parse(dto.readingAt).toEpochMilli()
                    } catch (_: Exception) {
                        System.currentTimeMillis()
                    },
                    imageUrl = null
                )
            }
            emit(readings)
        }
    }
}
