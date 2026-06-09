package co.edu.cecar.smartbookmobile.features.lots

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
import co.edu.cecar.smartbookmobile.core.model.Lot
import co.edu.cecar.smartbookmobile.core.ui.LoadingContent
import co.edu.cecar.smartbookmobile.data.LocalAppContainer
import co.edu.cecar.smartbookmobile.features.common.ViewModelFactory

@Composable
fun LotsScreen() {
    val container = LocalAppContainer.current
    val vm: LotsViewModel = viewModel(factory = ViewModelFactory { LotsViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                vm.clearMessages()
                showForm = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar lote")
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
                    items(state.lots) { lot ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(lot.lote, style = MaterialTheme.typography.titleMedium)
                                Text(if (lot.actual) "Lote actual" else "Lote histórico")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        LotFormDialog(
            onDismiss = { showForm = false },
            onSave = {
                vm.createLot(it)
                showForm = false
            },
        )
    }
}

@Composable
private fun LotFormDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Lote") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(code.trim())
            }, enabled = code.isNotBlank()) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}
