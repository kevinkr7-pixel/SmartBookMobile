package co.edu.cecar.smartbookmobile.features.users

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
import androidx.compose.material3.Switch
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
import co.edu.cecar.smartbookmobile.core.model.User
import co.edu.cecar.smartbookmobile.core.ui.LoadingContent
import co.edu.cecar.smartbookmobile.core.ui.SearchField
import co.edu.cecar.smartbookmobile.data.LocalAppContainer
import co.edu.cecar.smartbookmobile.features.common.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen() {
    val container = LocalAppContainer.current
    val vm: UsersViewModel = viewModel(factory = ViewModelFactory { UsersViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()
    var formUser by remember { mutableStateOf<User?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                vm.clearMessages()
                formUser = User()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo usuario")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SearchField(value = state.search, onValueChange = vm::onSearchChange, label = "Buscar por nombre")
                Button(onClick = vm::loadUsers, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Buscar")
                }
            }

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
                    items(state.users) { user ->
                        Card(onClick = { formUser = user }, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(user.nombres, style = MaterialTheme.typography.titleMedium)
                                Text("ID: ${user.identificacion}")
                                Text(user.email)
                                Text("Rol: ${user.rol}")
                                Text(if (user.isActive()) "Estado: Activo" else "Estado: Inactivo")
                            }
                        }
                    }
                }
            }
        }
    }

    if (formUser != null) {
        UserFormDialog(
            user = formUser!!,
            onDismiss = { formUser = null },
            onSave = { payload ->
                vm.saveUser(
                    id = payload.id,
                    identificacion = payload.identificacion,
                    nombres = payload.nombres,
                    email = payload.email,
                    rol = payload.rol,
                    activo = payload.activo,
                    password = payload.password,
                )
                if (payload.id != null) {
                    vm.changeStatus(payload.id, payload.activo)
                }
                formUser = null
            },
        )
    }
}

private data class UserFormData(
    val id: Int?,
    val identificacion: String,
    val nombres: String,
    val email: String,
    val rol: Int,
    val activo: Boolean,
    val password: String,
)

@Composable
private fun UserFormDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (UserFormData) -> Unit,
) {
    var idText by remember(user) { mutableStateOf(user.id?.toString().orEmpty()) }
    var identificacion by remember(user) { mutableStateOf(user.identificacion) }
    var nombres by remember(user) { mutableStateOf(user.nombres) }
    var email by remember(user) { mutableStateOf(user.email) }
    var rolText by remember(user) { mutableStateOf(if (user.rol.equals("Admin", ignoreCase = true)) "1" else "2") }
    var activo by remember(user) { mutableStateOf(user.isActive()) }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user.id == null) "Nuevo Usuario" else "Editar Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = idText, onValueChange = { idText = it }, label = { Text("ID (opcional)") })
                OutlinedTextField(value = identificacion, onValueChange = { identificacion = it }, label = { Text("Identificación") })
                OutlinedTextField(value = nombres, onValueChange = { nombres = it }, label = { Text("Nombres") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = rolText, onValueChange = { rolText = it }, label = { Text("Rol (1=Admin, 2=Vendedor)") })
                if (user.id == null) {
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Activo")
                    Switch(checked = activo, onCheckedChange = { activo = it })
                }
            }
        },
        confirmButton = {
            Button(
                enabled = identificacion.isNotBlank() && nombres.isNotBlank() && email.isNotBlank() && (user.id != null || password.isNotBlank()),
                onClick = {
                    onSave(
                        UserFormData(
                            id = idText.toIntOrNull(),
                            identificacion = identificacion.trim(),
                            nombres = nombres.trim(),
                            email = email.trim(),
                            rol = rolText.toIntOrNull() ?: 2,
                            activo = activo,
                            password = password,
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
