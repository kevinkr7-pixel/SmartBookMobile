package co.edu.cecar.smartbookmobile.core.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecureTokenStorage(context: Context) {
    private val prefs: SharedPreferences
    private val _tokenFlow: MutableStateFlow<String?>

    init {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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

    private companion object {
        const val PREFS_NAME = "smartbook_session_prefs"
        const val KEY_TOKEN = "jwt_token"
    }
}
