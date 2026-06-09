package co.edu.cecar.smartbookmobile.core.util

private val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()

fun String.isEmailValid(): Boolean = matches(emailRegex)
fun String.isRequired(): Boolean = isNotBlank()
