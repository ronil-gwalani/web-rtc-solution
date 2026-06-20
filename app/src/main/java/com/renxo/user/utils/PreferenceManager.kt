package com.renxo.user.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.renxo.user.screens.Language
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import okio.Path.Companion.toPath

class PreferenceManager(preferenceName: String) {

    suspend fun clearAllPreferences() {
        preferenceManager.edit { preferences ->
            preferences.clear()
        }
    }
    suspend fun saveLanguage(languageCode: String) {
        setValue(AppConstants.Preferences.LANGUAGE_KEY, languageCode)

    }

    suspend fun getLanguage(): String {
        return getString(AppConstants.Preferences.LANGUAGE_KEY)?:"en-US"
    }
    private val preferenceManager: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(produceFile = { (if (preferenceName.contains(".preferences_pb")) preferenceName else "$preferenceName.preferences_pb").toPath() })
    }

    suspend fun setValue(key: String, value: Any) {
        preferenceManager.edit {
            when (value) {
                is String -> {
                    it[stringPreferencesKey(key)] = value
                }

                is Int -> {
                    it[intPreferencesKey(key)] = value
                }

                is Boolean -> {
                    it[booleanPreferencesKey(key)] = value
                }

                is Double -> {
                    it[doublePreferencesKey(key)] = value
                }

                else -> {
                    it[stringPreferencesKey(key)] = value.toString()
                }
            }
        }
    }

    suspend fun saveAuthToken(value: String) {
        setValue(AppConstants.Preferences.AUTH_TOKEN, value)
    }

    suspend fun getAuthToken(): String? {
        return getString(AppConstants.Preferences.AUTH_TOKEN)
    }


    suspend fun clearUser() {
        removeKey(AppConstants.Preferences.USER_ID)
    }

    suspend fun setUser(userId: String) {
        setValue(AppConstants.Preferences.USER_ID, userId)
    }

    suspend fun clearAuthToken() {
        removeKey(AppConstants.Preferences.AUTH_TOKEN)
        removeKey(AppConstants.Preferences.MAIN_SERVER_IP)
    }

    suspend fun getUserId(): String? {
        return getString(AppConstants.Preferences.USER_ID)
    }

    suspend fun removeKey(key: String) {
        preferenceManager.edit {
            it.remove(stringPreferencesKey(key))
        }
    }

    suspend fun getString(key: String): String? {
        return preferenceManager.data.first()[stringPreferencesKey(key)]
    }

    suspend fun getInt(key: String, default: Int = 0): Int {
        return preferenceManager.data.first()[intPreferencesKey(key)] ?: default
    }

    suspend fun getBoolean(key: String, default: Boolean = false): Boolean {
        return preferenceManager.data.first()[booleanPreferencesKey(key)] ?: default
    }

    suspend fun getDouble(key: String, default: Double = 0.0): Double {
        return preferenceManager.data.first()[doublePreferencesKey(key)] ?: default
    }


    suspend fun saveMainUrl(value: String) {
        setValue(AppConstants.Preferences.MAIN_SERVER_IP, value)
    }

    suspend fun getMainUrl(): String? {
        return getString(AppConstants.Preferences.MAIN_SERVER_IP)
    }

    suspend fun saveAuthUrl(value: String) {
        setValue(AppConstants.Preferences.AUTH_SERVER_IP, value)
    }

    suspend fun saveDeviceId(value: String) {
        setValue(AppConstants.Preferences.DEVICE_ID, value)
    }

    suspend fun getDeviceId(): String? {
        return getString(AppConstants.Preferences.DEVICE_ID)
    }

    suspend fun getAuthUrl(): String? {
        return getString(AppConstants.Preferences.AUTH_SERVER_IP)
    }

}