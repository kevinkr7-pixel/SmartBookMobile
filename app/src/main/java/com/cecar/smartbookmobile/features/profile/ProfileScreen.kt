package com.cecar.smartbookmobile.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cecar.smartbookmobile.core.security.SessionManager
import com.cecar.smartbookmobile.core.ui.LoadingContent
import com.cecar.smartbookmobile.data.LocalAppContainer
import com.cecar.smartbookmobile.features.common.ViewModelFactory

@Composable
fun ProfileScreen() {
    val container = LocalAppContainer.current
    val sessionManager: SessionManager = container.sessionManager
    val vm: ProfileViewModel = viewModel(factory = ViewModelFactory { ProfileViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingContent(modifier = Modifier.padding(24.dp))
        return
    }

    if (!state.errorMessage.isNullOrBlank()) {
        Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        return
    }

    val profile = state.profile ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineSmall)
        Text("Nombre: ${profile.nombres}")
        Text("Email: ${profile.email}")
        Text("Rol: ${profile.rol}")

        Button(onClick = { sessionManager.onLogout() }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Cerrar sesión")
        }
    }
}
