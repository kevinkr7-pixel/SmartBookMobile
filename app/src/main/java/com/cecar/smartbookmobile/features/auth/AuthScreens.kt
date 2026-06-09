package com.cecar.smartbookmobile.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cecar.smartbookmobile.R
import com.cecar.smartbookmobile.data.LocalAppContainer
import com.cecar.smartbookmobile.features.common.ViewModelFactory

@Composable
fun LoginScreen(
    onNavigateForgotPassword: () -> Unit,
    onNavigateResetPassword: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: AuthViewModel = viewModel(
        factory = ViewModelFactory { AuthViewModel(container.repository, container.sessionManager) },
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CdiLogo(height = 72.dp)
            }
            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::setEmail,
                        label = { Text("Correo") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = vm::setPassword,
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = if (passwordVisible) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    },
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (!state.errorMessage.isNullOrBlank()) {
                        Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                    }

                    if (!state.successMessage.isNullOrBlank()) {
                        Text(state.successMessage ?: "", color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = vm::login,
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (state.isLoading) "Validando..." else "Entrar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onNavigateForgotPassword, modifier = Modifier.fillMaxWidth()) {
                Text("Olvidé mi contraseña")
            }
            TextButton(onClick = onNavigateResetPassword, modifier = Modifier.fillMaxWidth()) {
                Text("Restablecer con código")
            }
            Text(
                "v1.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CdiLogo(height: Dp) {
    Image(
        painter = painterResource(id = R.drawable.cdi_logo_2022),
        contentDescription = "CDI Centro de Idiomas",
        contentScale = ContentScale.Fit,
        modifier = Modifier.height(height),
    )
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: AuthViewModel = viewModel(
        factory = ViewModelFactory { AuthViewModel(container.repository, container.sessionManager) },
    )
    val state by vm.state.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Solicitar restablecimiento", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        vm.clearMessage()
                    },
                    label = { Text("Correo") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (!state.errorMessage.isNullOrBlank()) {
                    Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }

                if (!state.successMessage.isNullOrBlank()) {
                    Text(state.successMessage ?: "", color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = { vm.requestPasswordReset(email) },
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.isLoading) "Enviando..." else "Enviar solicitud")
                }

                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: AuthViewModel = viewModel(
        factory = ViewModelFactory { AuthViewModel(container.repository, container.sessionManager) },
    )
    val state by vm.state.collectAsStateWithLifecycle()

    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        vm.clearMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Restablecer contraseña", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        vm.clearMessage()
                    },
                    label = { Text("Código") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        vm.clearMessage()
                    },
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (!state.errorMessage.isNullOrBlank()) {
                    Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }

                if (!state.successMessage.isNullOrBlank()) {
                    Text(state.successMessage ?: "", color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = { vm.resetPassword(code = code, newPassword = newPassword) },
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.isLoading) "Restableciendo..." else "Restablecer")
                }

                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver")
                }
            }
        }
    }
}
