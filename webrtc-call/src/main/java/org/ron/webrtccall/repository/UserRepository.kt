package org.ron.webrtccall.repository

interface UserRepository {
    suspend fun registerUser(userId: String, userName: String): Result<Unit>
    suspend fun getTargetUserToken(targetUserId: String): Result<String>
}
