package co.edu.cecar.smartbookmobile.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.cecar.smartbookmobile.core.model.UserProfile
import co.edu.cecar.smartbookmobile.core.util.AppResult
import co.edu.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val errorMessage: String? = null,
)

class ProfileViewModel(
    private val repository: SmartBookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState(isLoading = true))
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getProfile()) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoading = false, profile = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }
}
