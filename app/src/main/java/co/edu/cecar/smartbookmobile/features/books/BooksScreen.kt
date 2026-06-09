package co.edu.cecar.smartbookmobile.features.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.cecar.smartbookmobile.core.model.Book
import co.edu.cecar.smartbookmobile.core.ui.LoadingContent
import co.edu.cecar.smartbookmobile.core.ui.SearchField
import co.edu.cecar.smartbookmobile.data.LocalAppContainer
import co.edu.cecar.smartbookmobile.features.common.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen() {
    val container = LocalAppContainer.current
    val vm: BooksViewModel = viewModel(factory = ViewModelFactory { BooksViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()
    var formBook by remember { mutableStateOf<Book?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                formBook = Book()
                vm.clearMessages()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar libro")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SearchField(value = state.search, onValueChange = vm::onSearchChange, label = "Buscar por nombre")
                Button(onClick = vm::loadBooks, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Buscar")
                }
                if (!state.errorMessage.isNullOrBlank()) {
                    Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }
                if (!state.successMessage.isNullOrBlank()) {
                    Text(state.successMessage ?: "", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (state.isLoading) {
                LoadingContent(modifier = Modifier.padding(24.dp))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.books) { book ->
                        Card(onClick = { formBook = book }, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(book.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("Nivel: ${book.nivel}")
                                Text("Tipo: ${book.tipoLabel()}")
                                Text("Edición: ${book.edicion}")
                                Text("Stock: ${book.stockTotal}")
                                Text("Lote: ${book.lote.orEmpty()}")
                            }
                        }
                    }
                }
            }
        }
    }

    if (formBook != null) {
        BookFormDialog(
            book = formBook!!,
            onDismiss = { formBook = null },
            onSave = {
                vm.saveBook(
                    bookId = it.id,
                    nombre = it.nombre,
                    nivel = it.nivel,
                    tipo = it.tipo,
                    edicion = it.edicion,
                    unidades = it.unidades,
                    lote = it.lote,
                    valorCompra = it.valorCompra,
                    valorVentaPublico = it.valorVentaPublico,
                )
                formBook = null
            },
        )
    }
}

private data class BookFormData(
    val id: Int?,
    val nombre: String,
    val nivel: String,
    val tipo: Int,
    val edicion: String,
    val unidades: Int,
    val lote: String,
    val valorCompra: Double,
    val valorVentaPublico: Double,
)

@Composable
private fun BookFormDialog(
    book: Book,
    onDismiss: () -> Unit,
    onSave: (BookFormData) -> Unit,
) {
    var idText by remember(book) { mutableStateOf(book.id?.toString().orEmpty()) }
    var nombre by remember(book) { mutableStateOf(book.nombre) }
    var nivel by remember(book) { mutableStateOf(book.nivel) }
    var tipo by remember(book) { mutableStateOf(book.tipoLabel().toIntOrNull()?.toString() ?: "1") }
    var edicion by remember(book) { mutableStateOf(book.edicion) }
    var lote by remember(book) { mutableStateOf(book.lote.orEmpty()) }
    var unidades by remember(book) { mutableStateOf(book.stockTotal.toString()) }
    var valorCompra by remember(book) { mutableStateOf(book.valorCompra?.toPlainInput().orEmpty()) }
    var valorVenta by remember(book) { mutableStateOf(book.valorVentaPublico?.toPlainInput().orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = { Text(if (book.id == null) "Nuevo Libro" else "Editar Libro") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = idText,
                    onValueChange = { idText = it.onlyDigits() },
                    label = { Text("ID (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = nivel, onValueChange = { nivel = it }, label = { Text("Nivel") })
                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it.onlyDigits().take(1) },
                    label = { Text("Tipo (1=StudentsBook, 2=Workbook)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(value = edicion, onValueChange = { edicion = it }, label = { Text("Edición") })
                OutlinedTextField(value = lote, onValueChange = { lote = it }, label = { Text("Lote") })
                OutlinedTextField(
                    value = unidades,
                    onValueChange = { unidades = it.onlyDigits() },
                    label = { Text("Unidades") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = valorCompra,
                    onValueChange = { valorCompra = it.decimalInput() },
                    label = { Text("Valor compra") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = valorVenta,
                    onValueChange = { valorVenta = it.decimalInput() },
                    label = { Text("Valor venta público") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                enabled = nombre.isNotBlank() && nivel.isNotBlank() && edicion.isNotBlank() && lote.isNotBlank(),
                onClick = {
                    onSave(
                        BookFormData(
                            id = idText.toIntOrNull(),
                            nombre = nombre.trim(),
                            nivel = nivel.trim(),
                            tipo = tipo.toIntOrNull() ?: 1,
                            edicion = edicion.trim(),
                            unidades = unidades.toIntOrNull() ?: 0,
                            lote = lote.trim(),
                            valorCompra = valorCompra.toDoubleOrNull() ?: 0.0,
                            valorVentaPublico = valorVenta.toDoubleOrNull() ?: 0.0,
                        ),
                    )
                },
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

private fun String.onlyDigits(): String = filter { it.isDigit() }

private fun String.decimalInput(): String {
    val normalized = replace(',', '.')
    val builder = StringBuilder()
    var hasDot = false
    normalized.forEach { char ->
        when {
            char.isDigit() -> builder.append(char)
            char == '.' && !hasDot -> {
                builder.append(char)
                hasDot = true
            }
        }
    }
    return builder.toString()
}

private fun Double.toPlainInput(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()
