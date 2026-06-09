package co.edu.cecar.smartbookmobile.features.incomes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.cecar.smartbookmobile.core.model.CreateIncomeDto
import co.edu.cecar.smartbookmobile.core.ui.LoadingContent
import co.edu.cecar.smartbookmobile.data.LocalAppContainer
import co.edu.cecar.smartbookmobile.features.common.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomesScreen() {
    val container = LocalAppContainer.current
    val vm: IncomesViewModel = viewModel(factory = ViewModelFactory { IncomesViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                vm.clearMessages()
                showForm = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo ingreso")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (!state.errorMessage.isNullOrBlank()) {
                Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            if (!state.successMessage.isNullOrBlank()) {
                Text(state.successMessage ?: "", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp))
            }

            if (state.isLoading) {
                LoadingContent(modifier = Modifier.padding(24.dp))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.incomes) { income ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Ingreso #${income.id}", style = MaterialTheme.typography.titleMedium)
                                Text("Fecha: ${income.fecha}")
                                Text("Lote: ${income.codigoLote}")
                                Text("Unidades: ${income.unidades}")
                                Text("Compra: ${currency(income.valorCompra)}")
                                Text("Venta público: ${currency(income.valorVentaPublico)}")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        IncomeFormDialog(
            defaultLot = state.lots.firstOrNull().orEmpty(),
            onDismiss = { showForm = false },
            onSave = {
                vm.createIncome(it)
                showForm = false
            },
        )
    }
}

@Composable
private fun IncomeFormDialog(
    defaultLot: String,
    onDismiss: () -> Unit,
    onSave: (CreateIncomeDto) -> Unit,
) {
    var libroId by remember { mutableStateOf("") }
    var unidades by remember { mutableStateOf("1") }
    var lote by remember { mutableStateOf(defaultLot) }
    var valorCompra by remember { mutableStateOf("0") }
    var valorVentaPublico by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo ingreso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = libroId, onValueChange = { libroId = it }, label = { Text("Libro ID") })
                OutlinedTextField(value = unidades, onValueChange = { unidades = it }, label = { Text("Unidades") })
                OutlinedTextField(value = lote, onValueChange = { lote = it }, label = { Text("Lote") })
                OutlinedTextField(value = valorCompra, onValueChange = { valorCompra = it }, label = { Text("Valor compra") })
                OutlinedTextField(value = valorVentaPublico, onValueChange = { valorVentaPublico = it }, label = { Text("Valor venta público") })
            }
        },
        confirmButton = {
            Button(
                enabled = libroId.isNotBlank() && lote.isNotBlank(),
                onClick = {
                    onSave(
                        CreateIncomeDto(
                            libroId = libroId.toIntOrNull() ?: 0,
                            unidades = unidades.toIntOrNull() ?: 1,
                            lote = lote.trim(),
                            valorCompra = valorCompra.toDoubleOrNull() ?: 0.0,
                            valorVentaPublico = valorVentaPublico.toDoubleOrNull() ?: 0.0,
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

private fun currency(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(value)
