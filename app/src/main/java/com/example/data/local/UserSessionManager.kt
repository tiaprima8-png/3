package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userSessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session_preferences")

data class UserSessionData(
    val token: String,
    val userId: String,
    val namaLengkap: String,
    val username: String,
    val role: String,
    val wilayah: String,
    val kasieAtasan: String = "",
    val status: String = "Aktif",
    val isLoggedIn: Boolean = false
) {
    val isManager: Boolean get() = role.equals("MANAGER", ignoreCase = true)
    val isSuperAdmin: Boolean get() = role.equals("SUPER ADMIN", ignoreCase = true) || role.equals("SUPERADMIN", ignoreCase = true)
    val isOperator: Boolean get() = role.equals("OPERATOR", ignoreCase = true) || role.equals("operator", ignoreCase = true)
    val isExecutive: Boolean get() = isManager || isSuperAdmin
    val isKasie: Boolean get() = role.equals("KASIE", ignoreCase = true) || (!isExecutive && !isOperator)
    val isAktif: Boolean get() = status.equals("Aktif", ignoreCase = true) || status.equals("Active", ignoreCase = true)
}

/**
 * Manages persistent user session and authentication token in DataStore Preferences.
 */
class UserSessionManager(private val context: Context) {

    companion object {
        val KEY_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_NAMA_LENGKAP = stringPreferencesKey("nama_lengkap")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_WILAYAH = stringPreferencesKey("wilayah")
        val KEY_KASIE_ATASAN = stringPreferencesKey("kasie_atasan")
        val KEY_STATUS = stringPreferencesKey("status")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val sessionFlow: Flow<UserSessionData?> = context.userSessionDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false
            val username = prefs[KEY_USERNAME]
            if (!isLoggedIn || username.isNullOrBlank()) {
                null
            } else {
                UserSessionData(
                    token = prefs[KEY_TOKEN] ?: "",
                    userId = prefs[KEY_USER_ID] ?: "1",
                    namaLengkap = prefs[KEY_NAMA_LENGKAP] ?: username,
                    username = username,
                    role = prefs[KEY_ROLE] ?: "KASIE",
                    wilayah = prefs[KEY_WILAYAH] ?: "Geodesi 1",
                    kasieAtasan = prefs[KEY_KASIE_ATASAN] ?: "",
                    status = prefs[KEY_STATUS] ?: "Aktif",
                    isLoggedIn = true
                )
            }
        }

    suspend fun saveSession(
        token: String,
        userId: String,
        namaLengkap: String,
        username: String,
        role: String,
        wilayah: String,
        kasieAtasan: String = "",
        status: String = "Aktif"
    ) {
        context.userSessionDataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = userId
            prefs[KEY_NAMA_LENGKAP] = namaLengkap
            prefs[KEY_USERNAME] = username
            prefs[KEY_ROLE] = role
            prefs[KEY_WILAYAH] = wilayah
            prefs[KEY_KASIE_ATASAN] = kasieAtasan
            prefs[KEY_STATUS] = status
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun getSession(): UserSessionData? {
        return sessionFlow.firstOrNull()
    }

    suspend fun getCurrentUserWilayah(): String? {
        return getSession()?.wilayah
    }

    suspend fun clearSession() {
        context.userSessionDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
