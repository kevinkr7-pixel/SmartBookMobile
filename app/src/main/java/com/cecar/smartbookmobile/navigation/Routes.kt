package com.cecar.smartbookmobile.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object ForgotPassword : AppRoute("forgot-password")
    data object ResetPassword : AppRoute("reset-password")

    data object Dashboard : AppRoute("dashboard")
    data object Clients : AppRoute("clients")
    data object Books : AppRoute("books")
    data object Sales : AppRoute("sales")
    data object Lots : AppRoute("lots")
    data object Incomes : AppRoute("incomes")
    data object Inventory : AppRoute("inventory")
    data object Users : AppRoute("users")
    data object Profile : AppRoute("profile")
    data object SaleDetail : AppRoute("sales/{saleId}") {
        fun create(id: Int): String = "sales/$id"
    }
}
