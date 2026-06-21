package id.viasco.dynamic_qris_android.data.remote

import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Backend response DTOs.
 *
 * Laravel's [App\Http\Resources\TransactionResource] uses snake_case + ISO-8601 strings.
 * `paginated_index` uses Laravel's standard paginator wrapper: { data: [...], links, meta }.
 */

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val meta: PaginationMeta? = null,
)

@Serializable
data class PaginationMeta(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("last_page") val lastPage: Int,
    @SerialName("per_page") val perPage: Int,
    val total: Int,
)

@Serializable
data class SingleResponse<T>(val data: T)

@Serializable
data class TransactionDto(
    val id: String,
    @SerialName("qrisify_transaction_id") val qrisifyTransactionId: String? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("amount_requested") val amountRequested: Long,
    @SerialName("unique_code") val uniqueCode: Int? = null,
    @SerialName("amount_total") val amountTotal: Long? = null,
    val status: TransactionStatus,
    @SerialName("qris_string") val qrisString: String? = null,
    @SerialName("payment_provider") val paymentProvider: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class CreateTransactionRequest(
    val amount: Long,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("expiry_minutes") val expiryMinutes: Int = 15,
)

@Serializable
data class ErrorResponse(
    val message: String? = null,
    val errors: Map<String, List<String>>? = null,
)
