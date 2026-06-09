package co.edu.cecar.smartbookmobile.data

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import co.edu.cecar.smartbookmobile.core.network.SmartBookApiClient
import co.edu.cecar.smartbookmobile.core.security.AppPreferencesStore
import co.edu.cecar.smartbookmobile.core.security.SecureTokenStorage
import co.edu.cecar.smartbookmobile.core.security.SessionManager

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val tokenStorage: SecureTokenStorage = SecureTokenStorage(appContext)
    val sessionManager: SessionManager = SessionManager(tokenStorage)
    val preferencesStore: AppPreferencesStore = AppPreferencesStore(appContext)
    val apiClient: SmartBookApiClient = SmartBookApiClient(
        tokenProvider = { tokenStorage.getToken() },
        onUnauthorized = { sessionManager.onUnauthorized() },
    )
    val repository: SmartBookRepository = SmartBookRepository(apiClient)
}

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("AppContainer no inicializado")
}
