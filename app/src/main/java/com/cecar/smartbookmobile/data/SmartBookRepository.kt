package com.cecar.smartbookmobile.data

import com.cecar.smartbookmobile.core.model.Book
import com.cecar.smartbookmobile.core.model.BookDetail
import com.cecar.smartbookmobile.core.model.Client
import com.cecar.smartbookmobile.core.model.CreateBookDto
import com.cecar.smartbookmobile.core.model.CreateClientDto
import com.cecar.smartbookmobile.core.model.CreateIncomeDto
import com.cecar.smartbookmobile.core.model.CreateLotDto
import com.cecar.smartbookmobile.core.model.DashboardResponse
import com.cecar.smartbookmobile.core.model.Income
import com.cecar.smartbookmobile.core.model.InventoryItem
import com.cecar.smartbookmobile.core.model.LoginRequest
import com.cecar.smartbookmobile.core.model.LoginResponse
import com.cecar.smartbookmobile.core.model.Lot
import com.cecar.smartbookmobile.core.model.PasswordResetConfirm
import com.cecar.smartbookmobile.core.model.PasswordResetRequest
import com.cecar.smartbookmobile.core.model.RegisterUserDto
import com.cecar.smartbookmobile.core.model.SaleDetail
import com.cecar.smartbookmobile.core.model.SaleRequest
import com.cecar.smartbookmobile.core.model.SaleSummary
import com.cecar.smartbookmobile.core.model.UpdateBookDto
import com.cecar.smartbookmobile.core.model.UpdateClientDto
import com.cecar.smartbookmobile.core.model.UpdateUserDto
import com.cecar.smartbookmobile.core.model.User
import com.cecar.smartbookmobile.core.model.UserProfile
import com.cecar.smartbookmobile.core.network.ApiRoutes
import com.cecar.smartbookmobile.core.network.SmartBookApiClient
import com.cecar.smartbookmobile.core.util.AppResult
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.json.JsonObject

class SmartBookRepository(
    private val api: SmartBookApiClient,
) {
    suspend fun login(email: String, password: String): AppResult<LoginResponse> =
        api.post(ApiRoutes.LOGIN, LoginRequest(email = email, password = password))

    suspend fun requestPasswordReset(email: String): AppResult<JsonObject> =
        api.post(ApiRoutes.REQUEST_RESET, PasswordResetRequest(email = email))

    suspend fun resetPassword(code: String, newPassword: String): AppResult<JsonObject> =
        api.post(
            ApiRoutes.RESET_PASSWORD,
            PasswordResetConfirm(codigo = code, newPassword = newPassword),
        )

    suspend fun verifyEmail(token: String): AppResult<JsonObject> =
        api.get("${ApiRoutes.VERIFY_EMAIL}?token=${token.encodeURLParameter()}")

    suspend fun getDashboard(): AppResult<DashboardResponse> =
        api.get(ApiRoutes.DASHBOARD)

    suspend fun getClients(name: String = ""): AppResult<List<Client>> =
        api.get(withQuery(ApiRoutes.CLIENTS, "nombres", name))

    suspend fun getClient(identification: String): AppResult<Client> =
        api.get("${ApiRoutes.CLIENTS}/$identification")

    suspend fun createClient(client: Client): AppResult<JsonObject> =
        api.post(
            ApiRoutes.CLIENTS,
            CreateClientDto(
                identificacion = client.identificacion,
                nombres = client.nombres,
                email = client.email,
                celular = client.celular,
                fechaNacimiento = client.fechaNacimiento,
            ),
        )

    suspend fun updateClient(identification: String, client: Client): AppResult<JsonObject> =
        api.put(
            "${ApiRoutes.CLIENTS}/$identification",
            UpdateClientDto(
                nombres = client.nombres,
                email = client.email,
                celular = client.celular,
                fechaNacimiento = client.fechaNacimiento,
            ),
        )

    suspend fun getBooks(name: String = ""): AppResult<List<Book>> =
        api.get(withQuery(ApiRoutes.BOOKS, "Nombre", name))

    suspend fun getBook(id: Int): AppResult<BookDetail> =
        api.get("${ApiRoutes.BOOKS}/$id")

    suspend fun createBook(payload: CreateBookDto): AppResult<JsonObject> =
        api.post(ApiRoutes.BOOKS, payload)

    suspend fun updateBook(id: Int, payload: UpdateBookDto): AppResult<JsonObject> =
        api.put("${ApiRoutes.BOOKS}/$id", payload)

    suspend fun getLots(): AppResult<List<Lot>> =
        api.get(ApiRoutes.LOTS)

    suspend fun createLot(lotCode: String): AppResult<JsonObject> =
        api.post(ApiRoutes.LOTS, CreateLotDto(lote = lotCode))

    suspend fun getInventory(lote: String = ""): AppResult<List<InventoryItem>> =
        api.get(withQuery(ApiRoutes.INVENTORY, "lote", lote))

    suspend fun getIncomes(lote: String = ""): AppResult<List<Income>> =
        api.get(withQuery(ApiRoutes.INCOMES, "Lote", lote))

    suspend fun createIncome(payload: CreateIncomeDto): AppResult<JsonObject> =
        api.post(ApiRoutes.INCOMES, payload)

    suspend fun getIncomeLots(): AppResult<List<String>> =
        api.get(ApiRoutes.INCOME_LOTS)

    suspend fun getSales(cliente: String = ""): AppResult<List<SaleSummary>> =
        api.get(withQuery(ApiRoutes.SALES, "cliente", cliente))

    suspend fun getSaleDetail(id: Int): AppResult<SaleDetail> =
        api.get("${ApiRoutes.SALES}/$id")

    suspend fun createSale(request: SaleRequest): AppResult<JsonObject> =
        api.post(ApiRoutes.SALES, request)

    suspend fun getUsers(name: String = ""): AppResult<List<User>> =
        api.get(withQuery(ApiRoutes.USERS, "nombres", name))

    suspend fun getUser(id: Int): AppResult<User> =
        api.get("${ApiRoutes.USERS}/$id")

    suspend fun createUser(payload: RegisterUserDto): AppResult<JsonObject> =
        api.post(ApiRoutes.USERS, payload)

    suspend fun updateUser(id: Int, payload: UpdateUserDto): AppResult<JsonObject> =
        api.put("${ApiRoutes.USERS}/$id", payload)

    suspend fun updateUserStatus(id: Int, active: Boolean): AppResult<JsonObject> =
        api.patch("${ApiRoutes.USERS}/$id/estado?activo=$active")

    suspend fun getProfile(): AppResult<UserProfile> =
        api.get(ApiRoutes.PROFILE)

    private fun withQuery(path: String, key: String, value: String): String {
        if (value.isBlank()) return path
        return "$path?$key=${value.encodeURLParameter()}"
    }
}
