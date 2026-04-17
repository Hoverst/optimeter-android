package com.optimeter.app.data.remote.api

import com.optimeter.app.data.remote.dto.HomeResponseDto
import com.optimeter.app.data.remote.dto.CreateHomeRequestDto
import com.optimeter.app.data.remote.dto.CreateReadingRequestDto
import com.optimeter.app.data.remote.dto.MeterReadingResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OptimeterApiService {

    @GET("homes")
    suspend fun getHomes(): List<HomeResponseDto>

    @POST("homes")
    suspend fun addHome(
        @Body request: CreateHomeRequestDto
    ): HomeResponseDto

    @PUT("homes/{id}")
    suspend fun updateHome(
        @Path("id") id: String,
        @Body request: CreateHomeRequestDto
    ): HomeResponseDto

    @DELETE("homes/{id}")
    suspend fun deleteHome(
        @Path("id") id: String
    )

    @GET("readings")
    suspend fun getReadings(
        @Query("homeId") homeId: String,
        @Query("utility") utility: String? = null
    ): List<MeterReadingResponseDto>

    @GET("readings/latest")
    suspend fun getLatestReadings(
        @Query("homeId") homeId: String
    ): List<MeterReadingResponseDto>

    @POST("readings")
    suspend fun addReading(
        @Body request: CreateReadingRequestDto
    ): MeterReadingResponseDto

    @DELETE("readings/{id}")
    suspend fun deleteReading(
        @Path("id") id: String
    )
}