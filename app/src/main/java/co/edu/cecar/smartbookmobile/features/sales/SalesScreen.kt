package co.edu.cecar.smartbookmobile.features.sales

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.cecar.smartbookmobile.core.model.Book
import co.edu.cecar.smartbookmobile.core.model.InventoryItem
import co.edu.cecar.smartbookmobile.core.model.Lot
import co.edu.cecar.smartbookmobile.core.model.SaleItemRequest
import co.edu.cecar.smartbookmobile.core.model.SaleRequest
import co.edu.cecar.smartbookmobile.core.ui.LoadingContent
import co.edu.cecar.smartbookmobile.core.ui.SearchField
import co.edu.cecar.smartbookmobile.core.ui.theme.CdiBlueDark
import co.edu.cecar.smartbookmobile.core.ui.theme.CdiRed
import co.edu.cecar.smartbookmobile.data.LocalAppContainer
import co.edu.cecar.smartbookmobile.features.common.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onSaleClick: (Int) -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: SalesViewModel = viewModel(factory = ViewModelFactory { SalesViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()
    var openForm by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage, openForm) {
        if (openForm && !state.successMessage.isNullOrBlank()) {
            openForm = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    vm.clearMessages()
                    vm.loadFormCatalog()
                    openForm = true
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva venta")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SearchField(value = state.search, onValueChange = vm::onSearchChange, label = "Buscar por cliente")
                Button(onClick = vm::loadSales, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Buscar")
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }
                if (!state.successMessage.isNullOrBlank()) {
                    Text(state.successMessage ?: "", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (state.isListLoading) {
                LoadingContent(modifier = Modifier.padding(24.dp))
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.sales.size) { index ->
                        val sale = state.sales[index]
                        Card(onClick = { onSaleClick(sale.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Venta #${sale.id}", style = MaterialTheme.typography.titleMedium)
                                Text("Recibo: ${sale.numeroRecibo}")
                                Text("Comprobante: ${sale.numeroComprobante.orEmpty()}")
                                Text("Cliente: ${sale.clienteNombre}")
                                Text("Fecha: ${sale.fecha}")
                                Text("Total: ${currency(sale.total)}")
                            }
                        }
                    }
                }
            }
        }
    }

    if (openForm) {
        SaleFormDialog(
            books = state.books,
            inventory = state.inventory,
            lots = state.lots,
            pricesByBook = state.pricesByBook,
            isCatalogLoading = state.isCatalogLoading,
            isSubmitting = state.isSubmittingSale,
            errorMessage = state.errorMessage,
            onDismiss = {
                if (!state.isSubmittingSale) {
                    openForm = false
                }
            },
            onRetryCatalog = vm::loadFormCatalog,
            onSave = vm::createSale,
        )
    }
}

@Composable
fun SaleDetailScreen(
    saleId: Int,
) {
    val container = LocalAppContainer.current
    val vm: SaleDetailViewModel = viewModel(factory = ViewModelFactory { SaleDetailViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(saleId) {
        vm.load(saleId)
    }

    if (state.isLoading) {
        LoadingContent(modifier = Modifier.padding(24.dp))
        return
    }

    if (!state.errorMessage.isNullOrBlank()) {
        Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        return
    }

    val detail = state.detail ?: return

    androidx.compose.foundation.lazy.LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Venta #${detail.id}", style = MaterialTheme.typography.headlineSmall)
            Text("Recibo: ${detail.numeroRecibo}")
            Text("Comprobante: ${detail.numeroComprobante.orEmpty()}")
            Text("Cliente: ${detail.clienteNombre}")
            Text("Fecha: ${detail.fecha}")
            Text("Total: ${currency(detail.total)}")
        }
    }
}

private data class SaleDraftItem(
    val id: Int,
    val bookId: Int? = null,
    val lotCode: String = "",
    val quantity: String = "1",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleFormDialog(
    books: List<Book>,
    inventory: List<InventoryItem>,
    lots: List<Lot>,
    pricesByBook: Map<String, SaleBookPrice>,
    isCatalogLoading: Boolean,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onRetryCatalog: () -> Unit,
    onSave: (SaleRequest) -> Unit,
) {
    var clientId by remember { mutableStateOf("") }
    var comprobante by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var searchBook by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var nextId by remember { mutableIntStateOf(1) }
    val items = remember { mutableStateListOf<SaleDraftItem>() }

    val filteredBooks = remember(searchBook, books) {
        if (searchBook.isBlank()) books
        else books.filter { it.nombre.contains(searchBook, ignoreCase = true) }
    }

    fun addItem() {
        val defaultBookId = filteredBooks.firstOrNull()?.id ?: books.firstOrNull()?.id
        val defaultLot = defaultBookId?.let { getLotsForBook(it, books, inventory, lots).firstOrNull() }.orEmpty()
        items.add(
            SaleDraftItem(
                id = nextId,
                bookId = defaultBookId,
                lotCode = defaultLot,
                quantity = "1",
            ),
        )
        nextId += 1
    }

    LaunchedEffect(books, lots) {
        if (books.isNotEmpty() && lots.isNotEmpty() && items.isEmpty()) {
            addItem()
        }
    }

    fun updateItem(itemId: Int, updater: (SaleDraftItem) -> SaleDraftItem) {
        val index = items.indexOfFirst { it.id == itemId }
        if (index >= 0) {
            items[index] = updater(items[index])
        }
    }

    val total = items.sumOf { item ->
        val selectedBook = books.firstOrNull { it.id == item.bookId }
        val price = selectedBook?.effectiveSalePrice(pricesByBook)?.valorVentaPublico ?: 0.0
        val quantity = item.quantity.toIntOrNull() ?: 0
        price * quantity
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = CdiRed,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            "Registrar Venta (POS)",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Complete los datos de la venta",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                if (isCatalogLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text("Cargando libros y lotes...", modifier = Modifier.padding(top = 12.dp))
                        TextButton(onClick = onRetryCatalog) { Text("Reintentar") }
                    }
                } else {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        BoxWithConstraints {
                            val wideLayout = maxWidth >= 920.dp
                            if (wideLayout) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    ClientDataSection(
                                        clientId = clientId,
                                        onClientChange = {
                                            clientId = it
                                            localError = null
                                        },
                                        comprobante = comprobante,
                                        onComprobanteChange = { comprobante = it },
                                        observaciones = observaciones,
                                        onObservacionesChange = { observaciones = it },
                                        modifier = Modifier.weight(1f),
                                    )
                                    ItemsSection(
                                        books = books,
                                        inventory = inventory,
                                        lots = lots,
                                        pricesByBook = pricesByBook,
                                        filteredBooks = filteredBooks,
                                        searchBook = searchBook,
                                        onSearchBookChange = { searchBook = it },
                                        items = items,
                                        onAddItem = {
                                            addItem()
                                            localError = null
                                        },
                                        onUpdateItem = ::updateItem,
                                        onRemoveItem = { itemId -> items.removeAll { it.id == itemId } },
                                        modifier = Modifier.weight(1.6f),
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    ClientDataSection(
                                        clientId = clientId,
                                        onClientChange = {
                                            clientId = it
                                            localError = null
                                        },
                                        comprobante = comprobante,
                                        onComprobanteChange = { comprobante = it },
                                        observaciones = observaciones,
                                        onObservacionesChange = { observaciones = it },
                                    )
                                    ItemsSection(
                                        books = books,
                                        inventory = inventory,
                                        lots = lots,
                                        pricesByBook = pricesByBook,
                                        filteredBooks = filteredBooks,
                                        searchBook = searchBook,
                                        onSearchBookChange = { searchBook = it },
                                        items = items,
                                        onAddItem = {
                                            addItem()
                                            localError = null
                                        },
                                        onUpdateItem = ::updateItem,
                                        onRemoveItem = { itemId -> items.removeAll { it.id == itemId } },
                                    )
                                }
                            }
                        }

                        Surface(
                            color = CdiRed,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    "Total a pagar:",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    currency(total),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        }
                        if (!localError.isNullOrBlank()) {
                            Text(localError ?: "", color = MaterialTheme.colorScheme.error)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                enabled = !isSubmitting,
                            ) {
                                Text("Cancelar")
                            }
                            Button(
                                onClick = {
                                    val normalizedItems = items.mapNotNull { item ->
                                        val qty = item.quantity.toIntOrNull()
                                        val bookId = item.bookId
                                        if (bookId == null || item.lotCode.isBlank() || qty == null || qty <= 0) {
                                            null
                                        } else {
                                            SaleItemRequest(
                                                libroId = bookId,
                                                lote = item.lotCode.trim(),
                                                cantidad = qty,
                                            )
                                        }
                                    }

                                    if (clientId.isBlank()) {
                                        localError = "Debes ingresar la identificación del cliente."
                                        return@Button
                                    }
                                    if (normalizedItems.isEmpty()) {
                                        localError = "Agrega al menos un item válido para registrar la venta."
                                        return@Button
                                    }

                                    localError = null
                                    onSave(
                                        SaleRequest(
                                            identificacionCliente = clientId.trim(),
                                            numeroComprobante = comprobante.trim().ifBlank { null },
                                            observaciones = observaciones.trim().ifBlank { null },
                                            items = normalizedItems,
                                        ),
                                    )
                                },
                                enabled = !isSubmitting,
                                modifier = Modifier.padding(start = 10.dp),
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(end = 8.dp),
                                    )
                                }
                                Text("Registrar venta")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientDataSection(
    clientId: String,
    onClientChange: (String) -> Unit,
    comprobante: String,
    onComprobanteChange: (String) -> Unit,
    observaciones: String,
    onObservacionesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = CdiRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = null,
                        tint = CdiRed,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                Text(
                    "Datos del Cliente",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }

            OutlinedTextField(
                value = clientId,
                onValueChange = onClientChange,
                label = { Text("Identificación del cliente") },
                placeholder = { Text("Ej: 1052067315") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = comprobante,
                onValueChange = onComprobanteChange,
                label = { Text("Número de comprobante") },
                placeholder = { Text("Ej: 000123") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = observaciones,
                onValueChange = onObservacionesChange,
                label = { Text("Observaciones") },
                placeholder = { Text("Notas adicionales...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemsSection(
    books: List<Book>,
    inventory: List<InventoryItem>,
    lots: List<Lot>,
    pricesByBook: Map<String, SaleBookPrice>,
    filteredBooks: List<Book>,
    searchBook: String,
    onSearchBookChange: (String) -> Unit,
    items: List<SaleDraftItem>,
    onAddItem: () -> Unit,
    onUpdateItem: (Int, (SaleDraftItem) -> SaleDraftItem) -> Unit,
    onRemoveItem: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = CdiBlueDark.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = CdiBlueDark,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                Text(
                    "Items de Venta",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = searchBook,
                    onValueChange = onSearchBookChange,
                    label = { Text("Buscar libro") },
                    placeholder = { Text("Nombre del libro...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onAddItem, enabled = books.isNotEmpty() && lots.isNotEmpty()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Agregar", modifier = Modifier.padding(start = 6.dp))
                }
            }

            if (items.isEmpty()) {
                Text(
                    "No hay items. Agrega al menos uno para continuar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            items.forEach { item ->
                val selectedBook = books.firstOrNull { it.id == item.bookId }
                val lotsForBook = selectedBook?.id?.let { getLotsForBook(it, books, inventory, lots) }.orEmpty()
                val unitPrice = selectedBook?.effectiveSalePrice(pricesByBook)?.valorVentaPublico ?: 0.0
                val quantity = item.quantity.toIntOrNull() ?: 0
                val subtotal = unitPrice * quantity

                Card(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val wideItem = maxWidth >= 700.dp
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (wideItem) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    SelectorField(
                                        label = "Libro",
                                        options = filteredBooks.ifEmpty { books },
                                        selected = filteredBooks.firstOrNull { it.id == item.bookId }
                                            ?: books.firstOrNull { it.id == item.bookId },
                                        optionLabel = { it.nombre },
                                        onSelected = { selected ->
                                            val nextLot = getLotsForBook(selected.id ?: -1, books, inventory, lots).firstOrNull().orEmpty()
                                            onUpdateItem(item.id) { current ->
                                                current.copy(bookId = selected.id, lotCode = nextLot)
                                            }
                                        },
                                        modifier = Modifier.weight(1.6f),
                                    )

                                    SelectorField(
                                        label = "Lote",
                                        options = lotsForBook,
                                        selected = lotsForBook.firstOrNull { it == item.lotCode },
                                        optionLabel = { it },
                                        onSelected = { selected ->
                                            onUpdateItem(item.id) { current ->
                                                current.copy(lotCode = selected)
                                            }
                                        },
                                        modifier = Modifier.weight(1.2f),
                                    )

                                    OutlinedTextField(
                                        value = item.quantity,
                                        onValueChange = { value ->
                                            onUpdateItem(item.id) { current -> current.copy(quantity = value.filter { ch -> ch.isDigit() }) }
                                        },
                                        label = { Text("Cantidad") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(0.8f),
                                    )

                                    Column(
                                        modifier = Modifier.weight(0.8f),
                                    ) {
                                        Text("Subtotal", style = MaterialTheme.typography.labelLarge)
                                        Text(
                                            currency(subtotal),
                                            color = CdiRed,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            } else {
                                SelectorField(
                                    label = "Libro",
                                    options = filteredBooks.ifEmpty { books },
                                    selected = filteredBooks.firstOrNull { it.id == item.bookId }
                                        ?: books.firstOrNull { it.id == item.bookId },
                                    optionLabel = { it.nombre },
                                    onSelected = { selected ->
                                        val nextLot = getLotsForBook(selected.id ?: -1, books, inventory, lots).firstOrNull().orEmpty()
                                        onUpdateItem(item.id) { current -> current.copy(bookId = selected.id, lotCode = nextLot) }
                                    },
                                )
                                SelectorField(
                                    label = "Lote",
                                    options = lotsForBook,
                                    selected = lotsForBook.firstOrNull { it == item.lotCode },
                                    optionLabel = { it },
                                    onSelected = { selected ->
                                        onUpdateItem(item.id) { current -> current.copy(lotCode = selected) }
                                    },
                                )
                                OutlinedTextField(
                                    value = item.quantity,
                                    onValueChange = { value ->
                                        onUpdateItem(item.id) { current -> current.copy(quantity = value.filter { ch -> ch.isDigit() }) }
                                    },
                                    label = { Text("Cantidad") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text("Subtotal", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    currency(subtotal),
                                    color = CdiRed,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            if (selectedBook != null && unitPrice <= 0.0) {
                                Text(
                                    "Este libro no tiene valor de venta público registrado. Actualiza el libro o registra un ingreso con precio para calcular el total.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }

                            TextButton(onClick = { onRemoveItem(item.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = null, tint = CdiRed)
                                Text("Eliminar item", color = CdiRed, modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
private fun <T> SelectorField(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected?.let(optionLabel).orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Seleccione") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 220.dp, max = 480.dp),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun currency(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(value)

private fun Book.effectiveSalePrice(fallbackPrices: Map<String, SaleBookPrice>): SaleBookPrice {
    val ownCompra = valorCompra ?: 0.0
    val ownVenta = valorVentaPublico ?: 0.0
    if (ownCompra > 0.0 || ownVenta > 0.0) {
        return SaleBookPrice(valorCompra = ownCompra, valorVentaPublico = ownVenta)
    }
    return fallbackPrices[salePriceKey()] ?: SaleBookPrice(valorCompra = ownCompra, valorVentaPublico = ownVenta)
}

private fun getLotsForBook(
    bookId: Int,
    books: List<Book>,
    inventory: List<InventoryItem>,
    allLots: List<Lot>,
): List<String> {
    val selectedBook = books.firstOrNull { it.id == bookId } ?: return emptyList()
    val fromInventory = inventory
        .filter {
            it.nombreLibro.equals(selectedBook.nombre, ignoreCase = true) &&
                it.nivelLibro.equals(selectedBook.nivel, ignoreCase = true) &&
                it.edicionLibro.equals(selectedBook.edicion, ignoreCase = true) &&
                it.stockDisponible > 0
        }
        .map { it.lote }
        .distinct()

    if (fromInventory.isNotEmpty()) return fromInventory

    val fallback = listOfNotNull(selectedBook.lote).filter { it.isNotBlank() }
    if (fallback.isNotEmpty()) return fallback

    return allLots.map { it.lote }.distinct()
}
