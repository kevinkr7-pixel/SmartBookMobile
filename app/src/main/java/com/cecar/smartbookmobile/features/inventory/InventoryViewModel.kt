package com.cecar.smartbookmobile.features.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cecar.smartbookmobile.core.model.InventoryItem
import com.cecar.smartbookmobile.core.util.AppResult
import com.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InventoryUiState(
    val isLoading: Boolean = false,
    val inventory: List<InventoryItem> = emptyList(),
    val errorMessage: String? = null,
)

class InventoryViewModel(
    private val repository: SmartBookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InventoryUiState(isLoading = true))
    val state: StateFlow<InventoryUiState> = _state.asStateFlow()

    init {
        loadInventory()
    }

    fun loadInventory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getInventory()) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoading = false, inventory = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }
}
