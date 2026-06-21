package id.viasco.dynamic_qris_android.ui.qr

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.viasco.dynamic_qris_android.data.repository.TransactionRepository
import id.viasco.dynamic_qris_android.domain.model.Transaction
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import id.viasco.dynamic_qris_android.ui.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val POLL_INTERVAL_MS = 3_000L

@HiltViewModel
class QrDisplayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository,
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

    /** Observe local cache; ViewModel keeps the canonical [transaction] copy in [_ui]. */
    val cached: StateFlow<Transaction?> = repository.observeById(transactionId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var pollJob: Job? = null

    init {
        // Seed initial state from cache, then start polling.
        viewModelScope.launch {
            repository.getCached(transactionId)?.let { cached ->
                _ui.update { it.copy(transaction = cached) }
            }
            startPolling()
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                repository.refreshById(transactionId).fold(
                    onSuccess = { trx ->
                        _ui.update { it.copy(transaction = trx, errorMessage = null) }
                        if (trx.status.isTerminal) {
                            return@launch
                        }
                    },
                    onFailure = { e ->
                        _ui.update { it.copy(errorMessage = e.message) }
                    },
                )
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
