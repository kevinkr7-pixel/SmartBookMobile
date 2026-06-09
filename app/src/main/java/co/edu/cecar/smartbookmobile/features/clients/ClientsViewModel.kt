package co.edu.cecar.smartbookmobile.features.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.cecar.smartbookmobile.core.model.Client
import co.edu.cecar.smartbookmobile.core.util.AppResult
import co.edu.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClientsUiState(
    val isLoading: Boolean = false,
    val clients: List<Client> = emptyList(),
    val search: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class ClientsViewModel(
    private val repository: SmartBookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ClientsUiState(isLoading = true))
    val state: StateFlow<ClientsUiState> = _state.asStateFlow()

    init {
        loadClients()
    }

    fun onSearchChange(value: String) {
        _state.value = _state.value.copy(search = value)
    }

    fun loadClients() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getClients(_state.value.search)) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoading = false, clients = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun saveClient(client: Client) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.getClient(client.identificacion)
            val saveResult = if (result is AppResult.Success) {
                repository.updateClient(client.identificacion, client)
            } else {
                repository.createClient(client)
            }

            when (saveResult) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        successMessage = "Cliente guardado correctamente.",
                    )
                    loadClients()
                }

                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = saveResult.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }
}
