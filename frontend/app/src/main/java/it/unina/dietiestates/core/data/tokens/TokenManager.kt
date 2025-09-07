package it.unina.dietiestates.core.data.tokens

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import it.unina.dietiestates.core.domain.Tokens
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

class TokenManager(private val context: Context) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit { preferences ->
            preferences[accessTokenKey] = access
            preferences[refreshTokenKey] = refresh
        }
    }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data.first()[accessTokenKey]
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.first()[refreshTokenKey]
    }

    suspend fun clearTokens() {
        println("Tokens cleared!")
        context.dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
        }
    }

    suspend fun onTokenCleared(block: () -> Unit){
        context.dataStore.data.map { prefs ->
            val access = prefs[accessTokenKey]
            val refresh = prefs[refreshTokenKey]
            if (access != null && refresh != null) Tokens(access, refresh) else null
        }.collect { tokens ->
            if (tokens == null){
                block()
            }
        }
    }
}