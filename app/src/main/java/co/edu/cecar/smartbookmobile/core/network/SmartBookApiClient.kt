package co.edu.cecar.smartbookmobile.core.network

import co.edu.cecar.smartbookmobile.core.util.AppResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException

class SmartBookApiClient(
    private val tokenProvider: () -> String?,
    private val onUnauthorized: () -> Unit,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    private val client = HttpClient(Android) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("Ktor: $message")
                }
            }
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        install(HttpRequestRetry) {
            maxRetries = 2
            retryOnExceptionIf { _, cause -> cause is IOException }
            retryIf { _, response -> response.status.value in 500..599 }
            exponentialDelay()
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
            tokenProvider()?.let { token ->
                headers.append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    internal suspend inline fun <reified T> get(path: String): AppResult<T> =
        execute { get("${ApiConfig.BASE_URL}$path") }

    internal suspend inline fun <reified T> delete(path: String): AppResult<T> =
        execute { delete("${ApiConfig.BASE_URL}$path") }

    internal suspend inline fun <reified T, reified B> post(path: String, body: B): AppResult<T> =
        execute {
            post("${ApiConfig.BASE_URL}$path") {
                setBody(body)
            }
        }

    internal suspend inline fun <reified T, reified B> put(path: String, body: B): AppResult<T> =
        execute {
            put("${ApiConfig.BASE_URL}$path") {
                setBody(body)
            }
        }

    internal suspend inline fun <reified T> patch(path: String): AppResult<T> =
        execute {
            patch("${ApiConfig.BASE_URL}$path")
        }

    internal suspend inline fun <reified T> execute(
        crossinline request: suspend HttpClient.() -> HttpResponse,
    ): AppResult<T> {
        return try {
            val response = client.request()
            when (response.status.value) {
                in 200..299 -> {
                    if (T::class == Unit::class) {
                        @Suppress("UNCHECKED_CAST")
                        AppResult.Success(Unit as T)
                    } else {
                        val payload = response.bodyAsText()
                        if (payload.isBlank()) {
                            AppResult.Error("Respuesta vacía del servidor.")
                        } else if (T::class == String::class) {
                            @Suppress("UNCHECKED_CAST")
                            AppResult.Success(payload as T)
                        } else {
                            AppResult.Success(json.decodeFromString<T>(payload))
                        }
                    }
                }
                401 -> {
                    onUnauthorized()
                    AppResult.Error("Sesión expirada, vuelve a iniciar sesión.", 401)
                }
                403 -> AppResult.Error("No tienes permisos para esta acción.", 403)
                404 -> AppResult.Error("Recurso no encontrado.", 404)
                in 400..499 -> {
                    val payload = response.bodyAsText()
                    AppResult.Error(cleanErrorMessage(response.status.value, payload), response.status.value)
                }
                else -> {
                    val payload = response.bodyAsText()
                    AppResult.Error(cleanErrorMessage(response.status.value, payload), response.status.value)
                }
            }
        } catch (se: SerializationException) {
            AppResult.Error("Error procesando datos de la API: ${se.message}")
        } catch (ioe: IOException) {
            AppResult.Error("No se pudo conectar. Verifica tu red e inténtalo de nuevo.")
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Ocurrió un error inesperado.")
        }
    }

    private fun cleanErrorMessage(statusCode: Int, payload: String): String {
        if (payload.isBlank()) return defaultErrorMessage(statusCode)

        val parsed = runCatching { json.parseToJsonElement(payload).jsonObject }.getOrNull()
            ?: return payload.takeUnless { it.trimStart().startsWith("{") } ?: defaultErrorMessage(statusCode)

        val fields = validationFields(parsed)
        if (fields.isNotEmpty()) {
            return "Revisa los datos ingresados. Campos: ${fields.joinToString(", ")}."
        }

        val title = parsed.stringValue("title").orEmpty()
        val detail = parsed.stringValue("detail")
        val message = parsed.stringValue("message")

        if (title.contains("validation", ignoreCase = true)) {
            return "Revisa los datos ingresados. Hay campos obligatorios o con formato inválido."
        }

        val bestMessage = detail ?: message
        if (!bestMessage.isNullOrBlank() && !bestMessage.trimStart().startsWith("{")) {
            return bestMessage
        }

        return defaultErrorMessage(statusCode)
    }

    private fun validationFields(error: JsonObject): List<String> {
        val errors = error["errors"] as? JsonObject ?: return emptyList()
        return errors.entries
            .flatMap { (key, value) ->
                listOf(cleanFieldName(key)) + fieldNamesFromMessages(value)
            }
            .mapNotNull { it.takeIf(String::isNotBlank) }
            .distinct()
    }

    private fun fieldNamesFromMessages(value: JsonElement): List<String> {
        val messages = when (value) {
            is JsonArray -> value.mapNotNull { it.asStringOrNull() }
            is JsonPrimitive -> listOfNotNull(value.contentOrNull)
            else -> emptyList()
        }

        return messages.mapNotNull { message ->
            FIELD_NAME_REGEX.find(message)?.groupValues?.getOrNull(1)?.let(::cleanFieldName)
        }
    }

    private fun cleanFieldName(raw: String): String {
        return raw
            .substringAfterLast(".")
            .removePrefix("$")
            .removePrefix("[")
            .removeSuffix("]")
            .trim('\'', '"', ' ', ':')
            .replaceFirstChar { it.lowercase() }
    }

    private fun JsonObject.stringValue(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun JsonElement.asStringOrNull(): String? =
        (this as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun defaultErrorMessage(statusCode: Int): String =
        when (statusCode) {
            400 -> "Revisa los datos ingresados e inténtalo de nuevo."
            409 -> "No se pudo completar la acción porque el registro ya existe o está en uso."
            in 400..499 -> "No se pudo completar la acción. Revisa los datos e inténtalo de nuevo."
            else -> "El servidor no pudo completar la solicitud. Inténtalo de nuevo."
        }

    private companion object {
        val FIELD_NAME_REGEX = Regex("'([^']+)'")
    }
}
