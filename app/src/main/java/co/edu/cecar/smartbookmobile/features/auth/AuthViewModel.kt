package co.edu.cecar.smartbookmobile.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.cecar.smartbookmobile.core.security.SessionManager
import co.edu.cecar.smartbookmobile.core.util.AppResult
import co.edu.cecar.smartbookmobile.core.util.isEmailValid
import co.edu.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class AuthViewModel(
    private val repository: SmartBookRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun setEmail(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null)
    }

    fun setPassword(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun login() {
        val current = _state.value
        if (!current.email.isEmailValid() || current.password.isBlank()) {
            _state.value = current.copy(errorMessage = "Correo o contraseña inválidos.")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = repository.login(email = current.email.trim(), password = current.password)) {
                is AppResult.Success -> {
                    val token = result.data.token ?: result.data.accessToken
                    if (token.isNullOrBlank()) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "No se recibió token de autenticación.",
                        )
                    } else {
                        sessionManager.onLogin(token)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            successMessage = "Inicio de sesión exitoso.",
                        )
                    }
                }

                is AppResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                }

                AppResult.Loading -> Unit
            }
        }
    }

    fun requestPasswordReset(email: String) {
        if (!email.isEmailValid()) {
            _state.value = _state.value.copy(errorMessage = "Ingresa un correo válido.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = repository.requestPasswordReset(email.trim())) {
                is AppResult.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Solicitud enviada. Revisa tu correo.",
                )

                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun resetPassword(code: String, newPassword: String) {
        if (code.isBlank() || newPassword.length < 8) {
            _state.value = _state.value.copy(errorMessage = "Completa los datos correctamente.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = repository.resetPassword(code.trim(), newPassword)) {
                is AppResult.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Contraseña restablecida correctamente.",
                )

                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }

    fun logout() {
        sessionManager.onLogout()
    }
}
