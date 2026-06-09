package com.cecar.smartbookmobile.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    private val tokenStorage: SecureTokenStorage,
) {
    private val _isAuthenticated = MutableStateFlow(tokenStorage.getToken() != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        _isAuthenticated.value = tokenStorage.getToken() != null
    }

    fun onLogin(token: String) {
        tokenStorage.saveToken(token)
        _isAuthenticated.value = true
    }

    fun onLogout() {
        tokenStorage.clearToken()
        _isAuthenticated.value = false
    }

    fun onUnauthorized() {
        onLogout()
    }
}
