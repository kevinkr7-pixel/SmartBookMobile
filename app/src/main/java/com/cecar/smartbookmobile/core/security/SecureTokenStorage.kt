package com.cecar.smartbookmobile.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecureTokenStorage(context: Context) {
    private val prefs: SharedPreferences
    private val _tokenFlow: MutableStateFlow<String?>

    init {
        prefs = createReadablePreferences(context)
        _tokenFlow = MutableStateFlow(readTokenSafely())
    }

    val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    fun getToken(): String? = _tokenFlow.value

    fun saveToken(token: String) {
        runCatching {
            prefs.edit().putString(KEY_TOKEN, token).apply()
        }
        _tokenFlow.value = token
    }

    fun clearToken() {
        runCatching {
            prefs.edit().remove(KEY_TOKEN).apply()
        }
        _tokenFlow.value = null
    }

    private fun readTokenSafely(): String? {
        return runCatching {
            prefs.getString(KEY_TOKEN, null)
        }.getOrNull()
    }

    private fun createReadablePreferences(context: Context): SharedPreferences {
        val encrypted = createEncryptedPreferences(context)
        if (encrypted != null && canRead(encrypted)) return encrypted

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        val recreatedEncrypted = createEncryptedPreferences(context)
        if (recreatedEncrypted != null && canRead(recreatedEncrypted)) return recreatedEncrypted

        return context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun createEncryptedPreferences(context: Context): SharedPreferences? {
        return runCatching {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }.getOrNull()
    }

    private fun canRead(sharedPreferences: SharedPreferences): Boolean =
        runCatching { sharedPreferences.getString(KEY_TOKEN, null) }.isSuccess

    private companion object {
        const val PREFS_NAME = "smartbook_secure_prefs"
        const val FALLBACK_PREFS_NAME = "smartbook_session_prefs"
        const val KEY_TOKEN = "jwt_token"
    }
}
