package id.viasco.dynamic_qris_android.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirror of backend [App\Enums\TransactionStatus] (TransactionStatus.php).
 * Wire format: uppercase string.
 */
@Serializable
enum class TransactionStatus {
    @SerialName("PENDING")
    PENDING,

    @SerialName("SUCCESS")
    SUCCESS,

    @SerialName("EXPIRED")
    EXPIRED,

    @SerialName("CANCELLED")
    CANCELLED;

    val isTerminal: Boolean
        get() = this == SUCCESS || this == EXPIRED || this == CANCELLED

    val isCancellable: Boolean
        get() = this == PENDING
}
