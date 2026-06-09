package com.cecar.smartbookmobile.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cecar.smartbookmobile.core.model.DashboardSale
import com.cecar.smartbookmobile.core.ui.LoadingContent
import com.cecar.smartbookmobile.core.ui.theme.CdiBlueDark
import com.cecar.smartbookmobile.core.ui.theme.CdiRed
import com.cecar.smartbookmobile.data.LocalAppContainer
import com.cecar.smartbookmobile.features.common.ViewModelFactory
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    onAddClient: () -> Unit = {},
    onRegisterBook: () -> Unit = {},
    onNewSale: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val vm: DashboardViewModel = viewModel(factory = ViewModelFactory { DashboardViewModel(container.repository) })
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingContent()
        return
    }

    val metrics = listOf(
        StatItem(
            title = "Total Clientes",
            value = state.data.totalClientes.toString(),
            accent = CdiRed,
            icon = Icons.Outlined.PersonOutline,
        ),
        StatItem(
            title = "Libros Registrados",
            value = state.data.totalLibros.toString(),
            accent = CdiBlueDark,
            icon = Icons.AutoMirrored.Outlined.MenuBook,
        ),
        StatItem(
            title = "Ventas Mes",
            value = state.data.cantVentasMes.toString(),
            accent = CdiRed,
            icon = Icons.Outlined.ShoppingBag,
        ),
        StatItem(
            title = "Ingresos Mes",
            value = currency(state.data.totalVentasMes),
            accent = CdiBlueDark,
            icon = Icons.Outlined.AttachMoney,
        ),
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                metrics.forEach { metric ->
                    DashboardMetricCard(item = metric)
                }
            }
        }

        item {
            SalesTodayCard(sales = state.data.ventasHoy)
        }

        item {
            GeneralInfoCard(
                onAddClient = onAddClient,
                onRegisterBook = onRegisterBook,
                onNewSale = onNewSale,
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(item: StatItem) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(item.accent),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = item.accent.copy(alpha = 0.12f),
                    modifier = Modifier.size(58.dp),
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = Color(0xFF111111),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SalesTodayCard(sales: List<DashboardSale>) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Últimas ventas de hoy", style = MaterialTheme.typography.headlineSmall)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                ) {
                    Text(
                        text = sales.size.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (sales.isEmpty()) {
                Text(
                    "No hay ventas registradas hoy.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                sales.take(4).forEach { sale ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Factura #${sale.numeroRecibo}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = formatSaleTime(sale.fecha),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = currency(sale.total),
                                color = CdiRed,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeneralInfoCard(
    onAddClient: () -> Unit,
    onRegisterBook: () -> Unit,
    onNewSale: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Información general", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Bienvenido al panel administrativo de SmartBook. Desde aquí podrás gestionar todos los módulos del sistema.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CdiRed.copy(alpha = 0.04f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CdiRed.copy(alpha = 0.2f)),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Acciones rápidas",
                        color = CdiRed,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        QuickActionChip(
                            text = "Agregar cliente",
                            color = CdiRed,
                            onClick = onAddClient,
                        )
                        QuickActionChip(
                            text = "Registrar libro",
                            color = CdiBlueDark,
                            onClick = onRegisterBook,
                        )
                        QuickActionChip(
                            text = "Nueva venta",
                            color = CdiRed,
                            onClick = onNewSale,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = color,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

private data class StatItem(
    val title: String,
    val value: String,
    val accent: Color,
    val icon: ImageVector,
)

private fun currency(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(value)

private fun formatSaleTime(raw: String): String {
    return try {
        val dateTime = LocalDateTime.parse(raw)
        dateTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale("es", "CO"))).lowercase(Locale("es", "CO"))
    } catch (_: Exception) {
        raw
    }
}
