package org.ron.webrtccall.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class PreferenceManager(private val context: Context) : PreferenceProvider {

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val IS_REGISTERED = booleanPreferencesKey("is_registered")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
        private val OAUTH_TOKEN = stringPreferencesKey("oauth_token")
    }

    override val userId: Flow<String?> = context.userPrefsDataStore.data.map { it[USER_ID] }
    override val userName: Flow<String?> = context.userPrefsDataStore.data.map { it[USER_NAME] }
    override val isRegistered: Flow<Boolean> = context.userPrefsDataStore.data.map { it[IS_REGISTERED] ?: false }
    override val fcmToken: Flow<String?> = context.userPrefsDataStore.data.map { it[FCM_TOKEN] }

    override suspend fun saveUser(id: String, name: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[USER_ID] = id
            prefs[USER_NAME] = name
            prefs[IS_REGISTERED] = true
        }
    }

    override suspend fun saveFcmToken(token: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[FCM_TOKEN] = token
        }
    }
    
    override suspend fun clearSavedOAuthToken() {
        context.userPrefsDataStore.edit { prefs ->
            prefs.remove(OAUTH_TOKEN)
        }
    }
    
    override suspend fun saveOAuthToken(token: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[OAUTH_TOKEN] = token
        }
    }
    
    override suspend fun getOAuthToken(): String {
        return context.userPrefsDataStore.data
            .map { preferences ->
                preferences[OAUTH_TOKEN] ?: ""
            }
            .first()
    }
}
