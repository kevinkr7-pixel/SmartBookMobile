package co.edu.cecar.smartbookmobile.features.incomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.cecar.smartbookmobile.core.model.CreateIncomeDto
import co.edu.cecar.smartbookmobile.core.model.Income
import co.edu.cecar.smartbookmobile.core.util.AppResult
import co.edu.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IncomesUiState(
    val isLoading: Boolean = false,
    val incomes: List<Income> = emptyList(),
    val lots: List<String> = emptyList(),
    val selectedLot: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class IncomesViewModel(
    private val repository: SmartBookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(IncomesUiState(isLoading = true))
    val state: StateFlow<IncomesUiState> = _state.asStateFlow()

    init {
        loadLots()
        loadIncomes()
    }

    fun setSelectedLot(lot: String) {
        _state.value = _state.value.copy(selectedLot = lot)
    }

    fun loadLots() {
        viewModelScope.launch {
            when (val result = repository.getIncomeLots()) {
                is AppResult.Success -> _state.value = _state.value.copy(lots = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun loadIncomes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getIncomes(_state.value.selectedLot)) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoading = false, incomes = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun createIncome(payload: CreateIncomeDto) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = repository.createIncome(payload)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, successMessage = "Ingreso registrado correctamente.")
                    loadIncomes()
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
