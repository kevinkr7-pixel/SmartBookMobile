package co.edu.cecar.smartbookmobile.features.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.cecar.smartbookmobile.core.ui.LoadingContent
import co.edu.cecar.smartbookmobile.data.LocalAppContainer
import co.edu.cecar.smartbookmobile.features.common.ViewModelFactory

@Composable
fun InventoryScreen() {
    val container = LocalAppContainer.current
    val vm: InventoryViewModel = viewModel(factory = ViewModelFactory { InventoryViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()

    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
            Button(onClick = vm::loadInventory) { Text("Actualizar") }
        }

        if (!state.errorMessage.isNullOrBlank()) {
            Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (state.isLoading) {
            LoadingContent(modifier = Modifier.padding(24.dp))
        } else {
            val lowStock = state.inventory.count { it.stockDisponible <= 5 }
            val totalStock = state.inventory.sumOf { it.stockDisponible }

            Text(
                text = "Stock total: $totalStock | Libros en bajo stock: $lowStock",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium,
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.inventory) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(item.nombreLibro, style = MaterialTheme.typography.titleMedium)
                            Text("Nivel: ${item.nivelLibro} | Tipo: ${item.tipoLibro}")
                            Text("Edición: ${item.edicionLibro}")
                            Text("Lote: ${item.lote}")
                            Text("Ingresado: ${item.cantidadIngresada} | Vendido: ${item.cantidadVendida}")
                            Text("Stock disponible: ${item.stockDisponible}")
                            if (item.stockDisponible <= 5) {
                                Text("Bajo stock", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
