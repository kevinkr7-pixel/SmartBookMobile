package co.edu.cecar.smartbookmobile.core.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String? = null,
    @SerialName("accessToken") val accessToken: String? = null,
    val usuario: UserSummary? = null,
)

@Serializable
data class UserSummary(
    val id: Int? = null,
    @SerialName("nombres") val nombre: String? = null,
    val email: String? = null,
    val rol: String? = null,
)

@Serializable
data class PasswordResetRequest(
    val email: String,
)

@Serializable
data class PasswordResetConfirm(
    val codigo: String,
    @SerialName("newPassword") val newPassword: String,
)

@Serializable
data class DashboardResponse(
    @SerialName("totalClientes") val totalClientes: Int = 0,
    @SerialName("totalLibros") val totalLibros: Int = 0,
    @SerialName("cantVentasMes") val cantVentasMes: Int = 0,
    @SerialName("totalVentasMes") val totalVentasMes: Double = 0.0,
    @SerialName("ventasHoy") val ventasHoy: List<DashboardSale> = emptyList(),
)

@Serializable
data class DashboardSale(
    @SerialName("numeroRecibo") val numeroRecibo: String = "",
    val total: Double = 0.0,
    val fecha: String = "",
)

@Serializable
data class Client(
    val id: Int? = null,
    val identificacion: String = "",
    val nombres: String = "",
    val email: String? = null,
    val celular: String? = null,
    val fechaNacimiento: String? = null,
)

@Serializable
data class CreateClientDto(
    val identificacion: String,
    val nombres: String,
    val email: String? = null,
    val celular: String? = null,
    val fechaNacimiento: String? = null,
)

@Serializable
data class UpdateClientDto(
    val nombres: String,
    val email: String? = null,
    val celular: String? = null,
    val fechaNacimiento: String? = null,
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Book(
    val id: Int? = null,
    val nombre: String = "",
    val nivel: String = "",
    @SerialName("tipo") val tipoRaw: JsonElement? = null,
    val edicion: String = "",
    @SerialName("lote") val loteRaw: JsonElement? = null,
    @SerialName("stockTotal") val stockTotal: Int = 0,
    @SerialName("valorCompra") @JsonNames("valorCompa") val valorCompra: Double? = null,
    @SerialName("valorVentaPublico") @JsonNames("valorVentaPulico") val valorVentaPublico: Double? = null,
) {
    fun tipoLabel(): String {
        val primitive = tipoRaw as? JsonPrimitive ?: return ""
        return primitive.content
    }

    val lote: String?
        get() = loteRaw?.asPlainString()
}

@Serializable
data class BookDetail(
    val id: Int? = null,
    val nombre: String = "",
    val nivel: String = "",
    val tipo: Int = 1,
    val edicion: String = "",
)

@Serializable
data class CreateBookDto(
    val nombre: String,
    val nivel: String,
    val tipo: Int,
    val edicion: String,
    val unidades: Int,
    val lote: Int,
    val valorCompra: Double,
    val valorVentaPublico: Double,
)

@Serializable
data class UpdateBookDto(
    val nombre: String,
    val nivel: String,
    val tipo: Int,
    val edicion: String,
    val unidades: Int? = null,
    val lote: Int? = null,
    val valorCompra: Double? = null,
    val valorVentaPublico: Double? = null,
)

@Serializable
data class Lot(
    @SerialName("codigo") val codigoRaw: JsonElement? = null,
    val actual: Boolean = false,
) {
    val lote: String
        get() = codigoRaw?.asPlainString().orEmpty()
}

@Serializable
data class CreateLotDto(
    val lote: String,
)

@Serializable
data class InventoryItem(
    val nivelLibro: String = "",
    val nombreLibro: String = "",
    val edicionLibro: String = "",
    val tipoLibro: String = "",
    val cantidadIngresada: Int = 0,
    val cantidadVendida: Int = 0,
    val stockDisponible: Int = 0,
    val lote: String = "",
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Income(
    val id: Int = 0,
    val fecha: String = "",
    @SerialName("lote") @JsonNames("codigoLote") val codigoLoteRaw: JsonElement? = null,
    val unidades: Int = 0,
    val valorCompra: Double = 0.0,
    val valorVentaPublico: Double = 0.0,
) {
    val codigoLote: String
        get() = codigoLoteRaw?.asPlainString().orEmpty()
}

@Serializable
data class IncomeDetail(
    val id: Int = 0,
    @SerialName("lote") val loteRaw: JsonElement? = null,
    val fecha: String = "",
    val libroNombre: String = "",
    val nivel: String = "",
    val tipo: String = "",
    val unidades: Int = 0,
    val valorCompra: Double = 0.0,
    val valorVentaPublico: Double = 0.0,
) {
    val lote: String
        get() = loteRaw?.asPlainString().orEmpty()
}

@Serializable
data class CreateIncomeDto(
    val libroId: Int,
    val unidades: Int,
    val lote: String,
    val valorCompra: Double,
    val valorVentaPublico: Double,
)

@Serializable
data class SaleItemRequest(
    @SerialName("libroId") val libroId: Int,
    @SerialName("lote") val lote: String,
    val cantidad: Int,
)

@Serializable
data class SaleRequest(
    @SerialName("identificacionCliente") val identificacionCliente: String,
    @SerialName("numeroComprobante") val numeroComprobante: String? = null,
    @SerialName("observaciones") val observaciones: String? = null,
    val items: List<SaleItemRequest>,
)

@Serializable
data class SaleSummary(
    val id: Int = 0,
    val numeroRecibo: String = "",
    val numeroComprobante: String? = null,
    val total: Double = 0.0,
    val fecha: String = "",
    @SerialName("clienteNombre") val clienteNombre: String = "",
)

@Serializable
data class SaleDetail(
    val id: Int = 0,
    val numeroRecibo: String = "",
    val numeroComprobante: String? = null,
    val total: Double = 0.0,
    val fecha: String = "",
    @SerialName("clienteNombre") val clienteNombre: String = "",
)

@Serializable
data class User(
    val id: Int? = null,
    val identificacion: String = "",
    val nombres: String = "",
    val email: String = "",
    val rol: String = "",
    @SerialName("activo") val activoRaw: JsonElement? = null,
) {
    fun isActive(): Boolean {
        val primitive = activoRaw as? JsonPrimitive ?: return false
        primitive.booleanOrNull?.let { return it }
        primitive.intOrNull?.let { return it == 1 }
        return primitive.content.equals("true", ignoreCase = true)
    }
}

@Serializable
data class RegisterUserDto(
    val identificacion: String,
    val nombres: String,
    val email: String,
    val password: String,
    val rol: Int,
)

@Serializable
data class UpdateUserDto(
    val nombres: String,
    val email: String,
    val rol: Int,
    val activo: Boolean,
)

@Serializable
data class UserProfile(
    val id: Int,
    val nombres: String,
    val email: String,
    val rol: String,
)

@Serializable
data class ApiError(
    val message: String? = null,
    val errors: Map<String, List<String>>? = null,
)

private fun JsonElement.asPlainString(): String {
    val primitive = this as? JsonPrimitive ?: return toString()
    primitive.intOrNull?.let { return it.toString() }
    primitive.doubleOrNull?.let { value ->
        return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
    }
    return primitive.content
}
