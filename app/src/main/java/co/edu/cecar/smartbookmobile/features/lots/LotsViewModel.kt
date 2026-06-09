package co.edu.cecar.smartbookmobile.features.lots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.cecar.smartbookmobile.core.model.Lot
import co.edu.cecar.smartbookmobile.core.util.AppResult
import co.edu.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LotsUiState(
    val isLoading: Boolean = false,
    val lots: List<Lot> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class LotsViewModel(
    private val repository: SmartBookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LotsUiState(isLoading = true))
    val state: StateFlow<LotsUiState> = _state.asStateFlow()

    init {
        loadLots()
    }

    fun loadLots() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getLots()) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoading = false, lots = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun createLot(lotCode: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = repository.createLot(lotCode)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, successMessage = "Lote registrado correctamente.")
                    loadLots()
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
