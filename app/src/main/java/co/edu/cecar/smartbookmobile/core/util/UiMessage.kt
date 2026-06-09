package co.edu.cecar.smartbookmobile.core.util

fun Throwable.readableMessage(): String =
    message ?: "Ocurrió un error inesperado."
