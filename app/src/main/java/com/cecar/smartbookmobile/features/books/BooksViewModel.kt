package com.cecar.smartbookmobile.features.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cecar.smartbookmobile.core.model.Book
import com.cecar.smartbookmobile.core.model.CreateBookDto
import com.cecar.smartbookmobile.core.model.UpdateBookDto
import com.cecar.smartbookmobile.core.util.AppResult
import com.cecar.smartbookmobile.data.SmartBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BooksUiState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
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
                        lote = lote,
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
}
