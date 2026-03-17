package com.optimeter.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isUserLoggedIn: Flow<Boolean>
    val currentUserEmail: String?
    val currentUserId: String?
    
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun registerWithEmail(email: String, password: String): Result<Unit>
    suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit>
    suspend fun deleteUserDataFromFirestore(uid: String): Result<Unit>
    suspend fun deleteCurrentUserFromAuth(): Result<Unit>
    suspend fun signOut()
}
