package com.cecar.smartbookmobile.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cecar.smartbookmobile.R
import com.cecar.smartbookmobile.core.security.SessionManager
import com.cecar.smartbookmobile.core.ui.theme.CdiBlueDark
import com.cecar.smartbookmobile.core.ui.theme.CdiRed
import com.cecar.smartbookmobile.core.ui.theme.SmartBookTheme
import com.cecar.smartbookmobile.data.AppContainer
import com.cecar.smartbookmobile.data.LocalAppContainer
import com.cecar.smartbookmobile.features.auth.ForgotPasswordScreen
import com.cecar.smartbookmobile.features.auth.LoginScreen
import com.cecar.smartbookmobile.features.auth.ResetPasswordScreen
import com.cecar.smartbookmobile.features.books.BooksScreen
import com.cecar.smartbookmobile.features.clients.ClientsScreen
import com.cecar.smartbookmobile.features.dashboard.DashboardScreen
import com.cecar.smartbookmobile.features.incomes.IncomesScreen
import com.cecar.smartbookmobile.features.inventory.InventoryScreen
import com.cecar.smartbookmobile.features.lots.LotsScreen
import com.cecar.smartbookmobile.features.profile.ProfileScreen
import com.cecar.smartbookmobile.features.sales.SaleDetailScreen
import com.cecar.smartbookmobile.features.sales.SalesScreen
import com.cecar.smartbookmobile.features.users.UsersScreen
import kotlinx.coroutines.launch

@Composable
fun SmartBookRoot() {
    val context = LocalContext.current
    val appContainer = remember { AppContainer(context) }

    CompositionLocalProvider(LocalAppContainer provides appContainer) {
        SmartBookTheme {
            val isAuthenticated by appContainer.sessionManager.isAuthenticated.collectAsState()

            if (isAuthenticated) {
                PrivateArea(sessionManager = appContainer.sessionManager)
            } else {
                PublicArea()
            }
        }
    }
}

@Composable
private fun PublicArea() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoute.Login.route) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onNavigateForgotPassword = { navController.navigate(AppRoute.ForgotPassword.route) },
                onNavigateResetPassword = { navController.navigate(AppRoute.ResetPassword.route) },
            )
        }
        composable(AppRoute.ForgotPassword.route) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoute.ResetPassword.route) {
            ResetPasswordScreen(onBack = { navController.popBackStack() })
        }
    }
}

private data class DrawerItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PrivateArea(
    sessionManager: SessionManager,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val navEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navEntry?.destination?.route
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val adminLabel = if (screenWidthDp < 380) "Admin CDI" else "Administrador CDI"

    val drawerItems = listOf(
        DrawerItem(AppRoute.Dashboard.route, "Dashboard", { Icon(Icons.Outlined.Dashboard, contentDescription = null) }),
        DrawerItem(AppRoute.Lots.route, "Lotes", { Icon(Icons.Outlined.Inventory, contentDescription = null) }),
        DrawerItem(AppRoute.Clients.route, "Clientes", { Icon(Icons.Outlined.Person, contentDescription = null) }),
        DrawerItem(AppRoute.Books.route, "Libros", { Icon(Icons.Outlined.Book, contentDescription = null) }),
        DrawerItem(AppRoute.Inventory.route, "Inventario", { Icon(Icons.Outlined.Inventory, contentDescription = null) }),
        DrawerItem(AppRoute.Sales.route, "Ventas", { Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null) }),
        DrawerItem(AppRoute.Incomes.route, "Ingresos", { Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null) }),
        DrawerItem(AppRoute.Users.route, "Usuarios", { Icon(Icons.Outlined.Group, contentDescription = null) }),
        DrawerItem(AppRoute.Profile.route, "Perfil", { Icon(Icons.Outlined.AccountCircle, contentDescription = null) }),
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cdi_logo_2022),
                        contentDescription = "CDI",
                        modifier = Modifier.height(28.dp),
                    )
                }

                drawerItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationDrawerItem(
                        label = {
                            Text(
                                item.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            )
                        },
                        selected = selected,
                        icon = item.icon,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = CdiRed,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        onClick = {
                            navController.navigateSingleTop(item.route)
                            scope.launch { drawerState.close() }
                        },
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = {
                        Text(
                            "Cerrar sesión",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    selected = false,
                    icon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = CdiRed,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedContainerColor = CdiRed,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    onClick = {
                        scope.launch { drawerState.close() }
                        sessionManager.onLogout()
                    },
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Abrir menú",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    },
                    title = {
                        Text(
                            text = currentRoute.toTitle(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    actions = {
                        Surface(
                            shape = CircleShape,
                            color = CdiBlueDark.copy(alpha = 0.1f),
                            modifier = Modifier.padding(end = 10.dp),
                        ) {
                            Text(
                                text = adminLabel,
                                color = CdiBlueDark,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                PrivateNavHost(navController = navController)
            }
        }
    }
}

@Composable
private fun PrivateNavHost(
    navController: NavHostController,
) {
    NavHost(navController = navController, startDestination = AppRoute.Dashboard.route) {
        composable(AppRoute.Dashboard.route) {
            DashboardScreen(
                onAddClient = { navController.navigateSingleTop(AppRoute.Clients.route) },
                onRegisterBook = { navController.navigateSingleTop(AppRoute.Books.route) },
                onNewSale = { navController.navigateSingleTop(AppRoute.Sales.route) },
            )
        }
        composable(AppRoute.Clients.route) { ClientsScreen() }
        composable(AppRoute.Books.route) { BooksScreen() }
        composable(AppRoute.Sales.route) {
            SalesScreen(
                onSaleClick = { id -> navController.navigate(AppRoute.SaleDetail.create(id)) },
            )
        }
        composable(AppRoute.Lots.route) { LotsScreen() }
        composable(AppRoute.Incomes.route) { IncomesScreen() }
        composable(AppRoute.Inventory.route) { InventoryScreen() }
        composable(AppRoute.Users.route) { UsersScreen() }
        composable(AppRoute.Profile.route) { ProfileScreen() }
        composable(
            route = AppRoute.SaleDetail.route,
            arguments = listOf(navArgument("saleId") { type = NavType.IntType }),
        ) { entry ->
            val id = entry.arguments?.getInt("saleId") ?: 0
            SaleDetailScreen(saleId = id)
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun String?.toTitle(): String {
    return when {
        this == null -> "SmartBook"
        startsWith("sales/") -> "Detalle de venta"
        this == AppRoute.Dashboard.route -> "Dashboard"
        this == AppRoute.Clients.route -> "Clientes"
        this == AppRoute.Books.route -> "Libros"
        this == AppRoute.Sales.route -> "Ventas"
        this == AppRoute.Lots.route -> "Lotes"
        this == AppRoute.Incomes.route -> "Ingresos"
        this == AppRoute.Inventory.route -> "Inventario"
        this == AppRoute.Users.route -> "Usuarios"
        this == AppRoute.Profile.route -> "Perfil"
        else -> "SmartBook"
    }
}
