package co.edu.cecar.smartbookmobile.features.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.cecar.smartbookmobile.core.model.RegisterUserDto
import co.edu.cecar.smartbookmobile.core.model.UpdateUserDto
import co.edu.cecar.smartbookmobile.core.model.User
import co.edu.cecar.smartbookmobile.core.util.AppResult
import co.edu.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsersUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val search: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class UsersViewModel(
    private val repository: SmartBookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UsersUiState(isLoading = true))
    val state: StateFlow<UsersUiState> = _state.asStateFlow()

    init {
        loadUsers()
    }

    fun onSearchChange(value: String) {
        _state.value = _state.value.copy(search = value)
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getUsers(_state.value.search)) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoading = false, users = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun saveUser(
        id: Int?,
        identificacion: String,
        nombres: String,
        email: String,
        rol: Int,
        activo: Boolean,
        password: String,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            val result = if (id == null) {
                repository.createUser(
                    RegisterUserDto(
                        identificacion = identificacion,
                        nombres = nombres,
                        email = email,
                        password = password,
                        rol = rol,
                    ),
                )
            } else {
                repository.updateUser(
                    id,
                    UpdateUserDto(
                        nombres = nombres,
                        email = email,
                        rol = rol,
                        activo = activo,
                    ),
                )
            }

            when (result) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, successMessage = "Usuario guardado correctamente.")
                    loadUsers()
                }

                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun changeStatus(id: Int, active: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = repository.updateUserStatus(id, active)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, successMessage = "Estado actualizado.")
                    loadUsers()
                }

                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }
}
