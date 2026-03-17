package com.optimeter.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.optimeter.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val isUserLoggedIn: Flow<Boolean> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override val currentUserEmail: String?
        get() = firebaseAuth.currentUser?.email
        
    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteUserDataFromFirestore(uid: String): Result<Unit> {
        return try {
            // Delete the user's primary profile document.
            firestore.collection("users").document(uid).delete().await()

            // Best-effort cleanup of common collections that may store user data.
            deleteByQuery(collection = "homes", field = "userId", value = uid)
            deleteByQuery(collection = "readings", field = "userId", value = uid)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCurrentUserFromAuth(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("No authenticated user"))
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun deleteByQuery(collection: String, field: String, value: String) {
        // Delete in chunks to avoid batch limits.
        while (true) {
            val snapshot = firestore.collection(collection)
                .whereEqualTo(field, value)
                .limit(200)
                .get()
                .await()

            if (snapshot.isEmpty) return

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }
    }
}
