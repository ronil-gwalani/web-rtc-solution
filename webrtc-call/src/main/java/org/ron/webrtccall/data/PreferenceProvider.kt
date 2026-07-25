/**
 * Created by Ronil Gwalani
 * 
 */
package org.ron.webrtccall.data

import kotlinx.coroutines.flow.Flow

interface PreferenceProvider {
    val userId: Flow<String?>
    val userName: Flow<String?>
    val isRegistered: Flow<Boolean>
    val fcmToken: Flow<String?>
    
    suspend fun saveUser(id: String, name: String)
    suspend fun saveFcmToken(token: String)
    suspend fun clearSavedOAuthToken()
    suspend fun saveOAuthToken(token: String)
    suspend fun getOAuthToken(): String
}
