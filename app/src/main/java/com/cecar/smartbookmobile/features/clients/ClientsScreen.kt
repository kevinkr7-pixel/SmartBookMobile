package com.cecar.smartbookmobile.features.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.cecar.smartbookmobile.core.model.Client
import com.cecar.smartbookmobile.core.ui.LoadingContent
import com.cecar.smartbookmobile.core.ui.SearchField
import com.cecar.smartbookmobile.data.LocalAppContainer
import com.cecar.smartbookmobile.features.common.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen() {
    val container = LocalAppContainer.current
    val vm: ClientsViewModel = viewModel(factory = ViewModelFactory { ClientsViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()
    var formClient by remember { mutableStateOf<Client?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                formClient = Client(identificacion = "", nombres = "")
                vm.clearMessages()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SearchField(value = state.search, onValueChange = vm::onSearchChange, label = "Buscar cliente")
                Button(onClick = vm::loadClients, modifier = Modifier.padding(top = 8.dp)) {
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
                    items(state.clients) { client ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { formClient = client },
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(client.nombres, style = MaterialTheme.typography.titleMedium)
                                Text("ID: ${client.identificacion}")
                                Text("Email: ${client.email.orEmpty()}")
                                Text("Celular: ${client.celular.orEmpty()}")
                                Text("Nacimiento: ${client.fechaNacimiento.orEmpty()}")
                            }
                        }
                    }
                }
            }
        }
    }

    if (formClient != null) {
        ClientFormDialog(
            client = formClient!!,
            onDismiss = { formClient = null },
            onSave = {
                vm.saveClient(it)
                formClient = null
            },
        )
    }
}

@Composable
private fun ClientFormDialog(
    client: Client,
    onDismiss: () -> Unit,
    onSave: (Client) -> Unit,
) {
    var identificacion by remember(client) { mutableStateOf(client.identificacion) }
    var nombres by remember(client) { mutableStateOf(client.nombres) }
    var email by remember(client) { mutableStateOf(client.email.orEmpty()) }
    var celular by remember(client) { mutableStateOf(client.celular.orEmpty()) }
    var fechaNacimiento by remember(client) { mutableStateOf(client.fechaNacimiento.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (client.identificacion.isBlank()) "Nuevo Cliente" else "Editar Cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = identificacion, onValueChange = { identificacion = it }, label = { Text("Identificación") })
                OutlinedTextField(value = nombres, onValueChange = { nombres = it }, label = { Text("Nombres") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = celular, onValueChange = { celular = it }, label = { Text("Celular") })
                OutlinedTextField(
                    value = fechaNacimiento,
                    onValueChange = { fechaNacimiento = it },
                    label = { Text("Fecha nacimiento (YYYY-MM-DD)") },
                )
            }
        },
        confirmButton = {
            Button(
                enabled = identificacion.isNotBlank() && nombres.isNotBlank(),
                onClick = {
                    onSave(
                        Client(
                            identificacion = identificacion.trim(),
                            nombres = nombres.trim(),
                            email = email.trim().ifBlank { null },
                            celular = celular.trim().ifBlank { null },
                            fechaNacimiento = fechaNacimiento.trim().ifBlank { null },
                        ),
                    )
                },
            ) {
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
