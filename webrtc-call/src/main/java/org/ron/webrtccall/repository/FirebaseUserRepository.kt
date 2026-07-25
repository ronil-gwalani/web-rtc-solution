/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import org.ron.webrtccall.data.PreferenceProvider

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore,
    private val preferenceProvider: PreferenceProvider
) : UserRepository {

    override suspend fun registerUser(userId: String, userName: String): Result<Unit> {
        return try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            val userData = mapOf(
                "userId" to userId,
                "userName" to userName,
                "fcmToken" to fcmToken,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId).set(userData).await()
            preferenceProvider.saveUser(userId, userName)
            preferenceProvider.saveFcmToken(fcmToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTargetUserToken(targetUserId: String): Result<String> {
        return try {
            val targetDoc = firestore.collection("users").document(targetUserId).get().await()
            val targetToken = targetDoc.getString("fcmToken") 
                ?: return Result.failure(Exception("User not found or no token"))
            Result.success(targetToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
