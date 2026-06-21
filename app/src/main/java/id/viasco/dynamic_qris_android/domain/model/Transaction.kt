package id.viasco.dynamic_qris_android.domain.model

import java.time.Instant

/**
 * Domain model for a transaction.
 * Time fields are [Instant] (UTC). Format at the UI layer.
 */
data class Transaction(
    val id: String,
    val qrisifyTransactionId: String?,
    val externalId: String?,
    val amountRequested: Long,
    val uniqueCode: Int?,
    val amountTotal: Long?,
    val status: TransactionStatus,
    val qrisString: String?,
    val paymentProvider: String?,
    val expiresAt: Instant?,
    val paidAt: Instant?,
    val cancelledAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
