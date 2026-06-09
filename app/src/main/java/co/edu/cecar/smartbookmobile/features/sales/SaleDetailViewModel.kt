package co.edu.cecar.smartbookmobile.features.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.cecar.smartbookmobile.core.model.SaleDetail
import co.edu.cecar.smartbookmobile.core.util.AppResult
import co.edu.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SaleDetailUiState(
    val isLoading: Boolean = false,
    val detail: SaleDetail? = null,
    val errorMessage: String? = null,
)

class SaleDetailViewModel(
    private val repository: SmartBookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SaleDetailUiState())
    val state: StateFlow<SaleDetailUiState> = _state.asStateFlow()

    fun load(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getSaleDetail(id)) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoading = false, detail = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }
}
