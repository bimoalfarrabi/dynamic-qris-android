package id.viasco.dynamic_qris_android.ui.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.viasco.dynamic_qris_android.data.remote.QrisifyStatusDto
import id.viasco.dynamic_qris_android.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionStatusViewModel @Inject constructor(
    private val repository: TransactionRepository,
) : ViewModel() {

    data class State(
        val isChecking: Boolean = false,
        val isConnected: Boolean? = null,
        val errorMessage: String? = null,
        val responseTimeMs: Long? = null,
        val qrisify: QrisifyStatusDto? = null,
        val isCheckingQrisify: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        check()
    }

    fun check() {
        _state.update { it.copy(isChecking = true, isCheckingQrisify = true, errorMessage = null) }
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            repository.healthCheck().fold(
                onSuccess = {
                    val elapsed = System.currentTimeMillis() - start
                    _state.update {
                        it.copy(
                            isChecking = false,
                            isConnected = true,
                            responseTimeMs = elapsed,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isChecking = false,
                            isConnected = false,
                            responseTimeMs = null,
                            errorMessage = e.message,
                        )
                    }
                },
            )
        }
        viewModelScope.launch {
            repository.checkQrisify().fold(
                onSuccess = { dto ->
                    _state.update { it.copy(isCheckingQrisify = false, qrisify = dto) }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isCheckingQrisify = false,
                            qrisify = QrisifyStatusDto(
                                ok = false,
                                statusCode = null,
                                responseTimeMs = 0,
                                error = e.message,
                                checkedAt = "",
                            ),
                        )
                    }
                },
            )
        }
    }
}
