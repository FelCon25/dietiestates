package it.unina.dietiestates.core.data.tokens

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import it.unina.dietiestates.core.domain.Tokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext

class TokenManager(context: Context) {
    private val _tokenCleared = MutableSharedFlow<Unit>(replay = 0)

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secure_auth_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun saveTokens(access: String, refresh: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString("access_token", access).putString("refresh_token", refresh).apply()
    }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString("access_token", null)
    }

    suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString("refresh_token", null)
    }

    suspend fun clearTokens() {
        withContext(Dispatchers.IO) {
            println("Tokens cleared!")
            prefs.edit().remove("access_token").remove("refresh_token").apply()
        }
        _tokenCleared.emit(Unit)
    }

    suspend fun onTokenCleared(block: () -> Unit) {
        _tokenCleared.collect { block() }
    }
}
