package id.viasco.dynamic_qris_android.ui.status

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.viasco.dynamic_qris_android.data.remote.QrisifyStatusDto
import id.viasco.dynamic_qris_android.data.repository.TransactionRepository
import id.viasco.dynamic_qris_android.ui.common.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionStatusViewModel @Inject constructor(
    private val repository: TransactionRepository,
    @ApplicationContext private val context: Context,
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
        NotificationHelper.createChannel(context)
        check()
    }

    fun check() {
        _state.update { it.copy(isChecking = true, isCheckingQrisify = true, errorMessage = null) }
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            repository.healthCheck().fold(
                onSuccess = {
                    val elapsed = System.currentTimeMillis() - start
                    NotificationHelper.handleLaravelUp(context)
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
                    NotificationHelper.handleLaravelDown(context)
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
                    if (dto.ok) NotificationHelper.handleQrisifyUp(context)
                    else NotificationHelper.handleQrisifyDown(context)
                    _state.update { it.copy(isCheckingQrisify = false, qrisify = dto) }
                },
                onFailure = { e ->
                    NotificationHelper.handleQrisifyDown(context)
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
