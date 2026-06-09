package com.cecar.smartbookmobile.core.network

object ApiConfig {
    const val BASE_URL = "https://api.smartbooks.cecar.cloud/api"
}

object ApiRoutes {
    const val LOGIN = "/Seguridad/iniciar-sesion"
    const val VERIFY_EMAIL = "/Seguridad/verificar-correo"
    const val REQUEST_RESET = "/Seguridad/solicitar-restablecimiento"
    const val RESET_PASSWORD = "/Seguridad/restablecer-contrasena"

    const val DASHBOARD = "/Dashboard"

    const val CLIENTS = "/Clientes"
    const val BOOKS = "/Libros"
    const val LOTS = "/Lotes"
    const val INCOMES = "/Ingresos"
    const val INCOME_LOTS = "/Ingresos/lotes"
    const val INVENTORY = "/Inventarios"
    const val SALES = "/Ventas"
    const val USERS = "/Usuarios"
    const val PROFILE = "/Usuarios/perfil"
}
