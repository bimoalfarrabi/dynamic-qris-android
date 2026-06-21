package id.viasco.dynamic_qris_android.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus

/**
 * Room entity mirroring the backend transaction table for offline-first reads.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "qrisify_transaction_id")
    val qrisifyTransactionId: String?,

    @ColumnInfo(name = "external_id")
    val externalId: String?,

    @ColumnInfo(name = "amount_requested")
    val amountRequested: Long,

    @ColumnInfo(name = "unique_code")
    val uniqueCode: Int?,

    @ColumnInfo(name = "amount_total")
    val amountTotal: Long?,

    val status: TransactionStatus,

    @ColumnInfo(name = "qris_string")
    val qrisString: String?,

    @ColumnInfo(name = "payment_provider")
    val paymentProvider: String?,

    /** Epoch millis */
    @ColumnInfo(name = "expires_at")
    val expiresAt: Long?,

    @ColumnInfo(name = "paid_at")
    val paidAt: Long?,

    @ColumnInfo(name = "cancelled_at")
    val cancelledAt: Long?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
