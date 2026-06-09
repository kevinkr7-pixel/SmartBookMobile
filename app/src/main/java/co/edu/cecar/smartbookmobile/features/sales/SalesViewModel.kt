package co.edu.cecar.smartbookmobile.features.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.cecar.smartbookmobile.core.model.Book
import co.edu.cecar.smartbookmobile.core.model.InventoryItem
import co.edu.cecar.smartbookmobile.core.model.Lot
import co.edu.cecar.smartbookmobile.core.model.SaleRequest
import co.edu.cecar.smartbookmobile.core.model.SaleSummary
import co.edu.cecar.smartbookmobile.core.util.AppResult
import co.edu.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class SalesUiState(
    val isListLoading: Boolean = false,
    val isCatalogLoading: Boolean = false,
    val isSubmittingSale: Boolean = false,
    val sales: List<SaleSummary> = emptyList(),
    val books: List<Book> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val lots: List<Lot> = emptyList(),
    val search: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class SalesViewModel(
    private val repository: SmartBookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SalesUiState(isListLoading = true))
    val state: StateFlow<SalesUiState> = _state.asStateFlow()

    init {
        loadSales()
    }

    fun onSearchChange(value: String) {
        _state.value = _state.value.copy(search = value)
    }

    fun loadSales() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isListLoading = true, errorMessage = null)
            when (val result = repository.getSales(_state.value.search)) {
                is AppResult.Success -> _state.value = _state.value.copy(isListLoading = false, sales = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(isListLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun loadFormCatalog() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCatalogLoading = true, errorMessage = null)
            val booksDeferred = async { repository.getBooks() }
            val lotsDeferred = async { repository.getLots() }
            val inventoryDeferred = async { repository.getInventory() }

            val booksResult = booksDeferred.await()
            val lotsResult = lotsDeferred.await()
            val inventoryResult = inventoryDeferred.await()

            val books = when (booksResult) {
                is AppResult.Success -> booksResult.data
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isCatalogLoading = false,
                        errorMessage = booksResult.message,
                    )
                    return@launch
                }

                AppResult.Loading -> emptyList()
            }

            val lots = when (lotsResult) {
                is AppResult.Success -> lotsResult.data
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isCatalogLoading = false,
                        errorMessage = lotsResult.message,
                    )
                    return@launch
                }

                AppResult.Loading -> emptyList()
            }

            val inventory = when (inventoryResult) {
                is AppResult.Success -> inventoryResult.data
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isCatalogLoading = false,
                        errorMessage = inventoryResult.message,
                    )
                    return@launch
                }

                AppResult.Loading -> emptyList()
            }

            _state.value = _state.value.copy(
                isCatalogLoading = false,
                books = books,
                inventory = inventory,
                lots = lots,
            )
        }
    }

    fun createSale(request: SaleRequest) {
        if (request.identificacionCliente.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Debes ingresar la identificación del cliente.")
            return
        }
        if (request.items.isEmpty() || request.items.any { it.libroId <= 0 || it.lote.isBlank() || it.cantidad <= 0 }) {
            _state.value = _state.value.copy(errorMessage = "Completa correctamente los items de la venta.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmittingSale = true, errorMessage = null, successMessage = null)
            when (val result = repository.createSale(request)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(isSubmittingSale = false, successMessage = "Venta registrada correctamente.")
                    loadSales()
                }

                is AppResult.Error -> _state.value = _state.value.copy(isSubmittingSale = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }
}
