package co.edu.cecar.smartbookmobile.features.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.cecar.smartbookmobile.core.model.Book
import co.edu.cecar.smartbookmobile.core.model.CreateBookDto
import co.edu.cecar.smartbookmobile.core.model.IncomeDetail
import co.edu.cecar.smartbookmobile.core.model.Lot
import co.edu.cecar.smartbookmobile.core.model.UpdateBookDto
import co.edu.cecar.smartbookmobile.core.util.AppResult
import co.edu.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookPrice(
    val valorCompra: Double,
    val valorVentaPublico: Double,
)

data class BooksUiState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val lots: List<Lot> = emptyList(),
    val pricesByBook: Map<String, BookPrice> = emptyMap(),
    val search: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class BooksViewModel(
    private val repository: SmartBookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BooksUiState(isLoading = true))
    val state: StateFlow<BooksUiState> = _state.asStateFlow()

    init {
        loadLots()
        loadIncomePrices()
        loadBooks()
    }

    fun onSearchChange(value: String) {
        _state.value = _state.value.copy(search = value)
    }

    fun loadBooks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getBooks(_state.value.search)) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoading = false, books = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun loadLots() {
        viewModelScope.launch {
            when (val result = repository.getLots()) {
                is AppResult.Success -> _state.value = _state.value.copy(lots = result.data)
                is AppResult.Error -> _state.value = _state.value.copy(errorMessage = result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun loadIncomePrices() {
        viewModelScope.launch {
            val incomes = when (val result = repository.getIncomes()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> {
                    _state.value = _state.value.copy(errorMessage = result.message)
                    return@launch
                }
                AppResult.Loading -> emptyList()
            }

            val details = incomes
                .take(MAX_INCOME_DETAILS_FOR_PRICE_FALLBACK)
                .map { income ->
                    async {
                        when (val detail = repository.getIncomeDetail(income.id)) {
                            is AppResult.Success -> detail.data
                            else -> null
                        }
                    }
                }
                .awaitAll()
                .filterNotNull()

            _state.value = _state.value.copy(pricesByBook = details.toPriceMap())
        }
    }

    fun saveBook(
        bookId: Int?,
        nombre: String,
        nivel: String,
        tipo: Int,
        edicion: String,
        unidades: Int,
        lote: String,
        valorCompra: Double,
        valorVentaPublico: Double,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val saveResult = if (bookId != null) {
                repository.updateBook(
                    bookId,
                    UpdateBookDto(
                        nombre = nombre,
                        nivel = nivel,
                        tipo = tipo,
                        edicion = edicion,
                        unidades = unidades,
                        lote = lote.toIntOrNull(),
                        valorCompra = valorCompra,
                        valorVentaPublico = valorVentaPublico,
                    ),
                )
            } else {
                repository.createBook(
                    CreateBookDto(
                        nombre = nombre,
                        nivel = nivel,
                        tipo = tipo,
                        edicion = edicion,
                        unidades = unidades,
                        lote = lote.toIntOrNull() ?: 0,
                        valorCompra = valorCompra,
                        valorVentaPublico = valorVentaPublico,
                    ),
                )
            }

            when (saveResult) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        successMessage = "Libro guardado correctamente.",
                    )
                    loadIncomePrices()
                    loadBooks()
                }

                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, errorMessage = saveResult.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }

    companion object {
        const val MAX_INCOME_DETAILS_FOR_PRICE_FALLBACK = 80
    }
}

fun Book.priceKey(): String =
    priceKey(nombre = nombre, nivel = nivel, tipo = tipoLabel())

private fun List<IncomeDetail>.toPriceMap(): Map<String, BookPrice> {
    val prices = linkedMapOf<String, BookPrice>()
    forEach { detail ->
        val key = priceKey(detail.libroNombre, detail.nivel, detail.tipo)
        if (key.isNotBlank() && key !in prices) {
            prices[key] = BookPrice(
                valorCompra = detail.valorCompra,
                valorVentaPublico = detail.valorVentaPublico,
            )
        }
    }
    return prices
}

private fun priceKey(nombre: String, nivel: String, tipo: String): String =
    listOf(nombre.normalizeKey(), nivel.normalizeKey(), tipo.normalizeBookType().normalizeKey()).joinToString("|")

private fun String.normalizeBookType(): String =
    when (trim()) {
        "1" -> "StudentsBook"
        "2" -> "Workbook"
        else -> this
    }

private fun String.normalizeKey(): String =
    trim().lowercase()
