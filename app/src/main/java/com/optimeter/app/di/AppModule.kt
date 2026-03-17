package com.optimeter.app.di

import android.content.Context
import com.optimeter.app.data.remote.api.OptimeterApiService
import com.optimeter.app.domain.repository.SettingsRepository
import com.optimeter.app.data.repository.SettingsRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Tasks
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context = context

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(firebaseAuth: FirebaseAuth): Interceptor {
        return object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val original: Request = chain.request()
                
                // Try to get the current user's ID token
                val token = runBlocking {
                    try {
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            val task = user.getIdToken(true)
                            Tasks.await(task)?.token
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                // If no token, proceed without Authorization header
                if (token == null || token.isEmpty()) {
                    return chain.proceed(original)
                }

                // Add Authorization header
                val request = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                return chain.proceed(request)
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit {
        // When running on Android emulator, 10.0.2.2 targets the host machine.
        val baseUrl = "http://10.0.2.2:4000/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOptimeterApiService(
        retrofit: Retrofit
    ): OptimeterApiService = retrofit.create(OptimeterApiService::class.java)
}