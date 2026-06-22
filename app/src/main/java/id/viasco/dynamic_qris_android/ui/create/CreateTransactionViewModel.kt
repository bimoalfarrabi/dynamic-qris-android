package id.viasco.dynamic_qris_android.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.viasco.dynamic_qris_android.data.repository.TransactionRepository
import id.viasco.dynamic_qris_android.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CreateTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
) : ViewModel() {

    data class State(
        val amountText: String = "",
        val externalId: String = "",
        val expiryMinutesText: String = "15",
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
        val created: Transaction? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun onAmountChanged(value: String) {
        // Only digits allowed.
        val digits = value.filter(Char::isDigit)
        _state.update { it.copy(amountText = digits, errorMessage = null) }
    }

    fun onExternalIdChanged(value: String) {
        _state.update { it.copy(externalId = value) }
    }

    fun onExpiryChanged(value: String) {
        _state.update { it.copy(expiryMinutesText = value.filter(Char::isDigit)) }
    }

    fun submit() {
        val current = _state.value
        val amount = current.amountText.toLongOrNull()
        if (amount == null || amount < 1_000) {
            _state.update { it.copy(errorMessage = "Jumlah minimal Rp 1.000") }
            return
        }
        val expiry = current.expiryMinutesText.toIntOrNull() ?: 15

        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            repository.create(
                amount = amount,
                externalId = current.externalId.takeIf { it.isNotBlank() },
                expiryMinutes = expiry,
            ).fold(
                onSuccess = { trx ->
                    _state.update { it.copy(isSubmitting = false, created = trx) }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = e.toUserMessage(),
                        )
                    }
                },
            )
        }
    }

    fun consumeCreated() {
        _state.update { it.copy(created = null) }
    }

    private fun Throwable.toUserMessage(): String = when {
        this is HttpException && code() == 502 ->
            "Provider pembayaran QRIS sedang gangguan. Coba lagi nanti."
        this is HttpException && code() in 500..599 ->
            "Server sedang bermasalah (${code()}). Coba lagi nanti."
        this is IOException ->
            "Tidak bisa terhubung ke server. Periksa koneksi Anda."
        else -> message ?: "Gagal membuat transaksi"
    }
}
