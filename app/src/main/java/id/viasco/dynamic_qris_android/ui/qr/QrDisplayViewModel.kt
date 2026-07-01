package id.viasco.dynamic_qris_android.ui.qr

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.viasco.dynamic_qris_android.data.repository.TransactionRepository
import id.viasco.dynamic_qris_android.domain.model.Transaction
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import id.viasco.dynamic_qris_android.ui.common.NotificationHelper
import id.viasco.dynamic_qris_android.ui.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val POLL_INTERVAL_MS = 3_000L

@HiltViewModel
class QrDisplayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    data class State(
        val transaction: Transaction? = null,
        val isCancelling: Boolean = false,
        val showCancelConfirm: Boolean = false,
        val errorMessage: String? = null,
    )

    private val transactionId: String = checkNotNull(
        savedStateHandle[Screen.QrDisplay.ARG_TRANSACTION_ID],
    ) { "transactionId is required" }

    private val _ui = MutableStateFlow(State())
    val state: StateFlow<State> = _ui.asStateFlow()

    private var pollJob: Job? = null

    init {
        // Observe Room directly — emits cached value immediately, then on every poll upsert.
        viewModelScope.launch {
            repository.observeById(transactionId).collect { trx ->
                _ui.update { it.copy(transaction = trx) }
            }
        }
        startPolling()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                val result = repository.refreshById(transactionId)
                result.onSuccess { trx ->
                    if (trx.status.isTerminal) {
                        if (trx.status == TransactionStatus.SUCCESS) {
                            NotificationHelper.notifyPaymentSuccess(
                                context,
                                trx.amountTotal?.toString() ?: trx.amountRequested.toString(),
                            )
                        }
                        pollJob?.cancel()
                        return@launch
                    }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun showCancelConfirm() = _ui.update { it.copy(showCancelConfirm = true) }
    fun dismissCancelConfirm() = _ui.update { it.copy(showCancelConfirm = false) }

    fun cancel() {
        _ui.update { it.copy(isCancelling = true, showCancelConfirm = false) }
        viewModelScope.launch {
            repository.cancel(transactionId).fold(
                onSuccess = { trx ->
                    pollJob?.cancel()
                    _ui.update { it.copy(transaction = trx, isCancelling = false) }
                },
                onFailure = { e ->
                    _ui.update {
                        it.copy(
                            isCancelling = false,
                            errorMessage = e.message ?: "Gagal membatalkan transaksi",
                        )
                    }
                },
            )
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}
