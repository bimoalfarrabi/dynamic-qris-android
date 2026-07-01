package id.viasco.dynamic_qris_android.ui.status

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.viasco.dynamic_qris_android.data.remote.QrisifyStatusDto
import id.viasco.dynamic_qris_android.data.repository.TransactionRepository
import id.viasco.dynamic_qris_android.ui.common.NotificationHelper
import kotlinx.coroutines.async
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

    enum class OverallStatus { UNKNOWN, ALL_UP, PARTIAL, ALL_DOWN }

    data class State(
        val isChecking: Boolean = false,
        val isConnected: Boolean? = null,
        val errorMessage: String? = null,
        val responseTimeMs: Long? = null,
        val qrisify: QrisifyStatusDto? = null,
    ) {
        val overallStatus: OverallStatus get() = when {
            isChecking -> OverallStatus.UNKNOWN
            isConnected == null || qrisify == null -> OverallStatus.UNKNOWN
            isConnected == true && qrisify.ok -> OverallStatus.ALL_UP
            isConnected == false && !qrisify.ok -> OverallStatus.ALL_DOWN
            else -> OverallStatus.PARTIAL
        }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        NotificationHelper.createChannel(context)
        check()
    }

    fun check() {
        _state.update { it.copy(isChecking = true, errorMessage = null) }
        viewModelScope.launch {
            val start = System.currentTimeMillis()

            // Run both checks in parallel.
            val laravelDeferred = async { repository.healthCheck() }
            val qrisifyDeferred = async { repository.checkQrisify() }

            val laravelResult = laravelDeferred.await()
            val qrisifyResult = qrisifyDeferred.await()

            val elapsed = System.currentTimeMillis() - start

            laravelResult.fold(
                onSuccess = { NotificationHelper.handleLaravelUp(context) },
                onFailure = { NotificationHelper.handleLaravelDown(context) },
            )

            qrisifyResult.fold(
                onSuccess = { dto ->
                    if (dto.ok) NotificationHelper.handleQrisifyUp(context)
                    else NotificationHelper.handleQrisifyDown(context)
                },
                onFailure = { NotificationHelper.handleQrisifyDown(context) },
            )

            _state.update {
                it.copy(
                    isChecking = false,
                    isConnected = laravelResult.isSuccess,
                    responseTimeMs = if (laravelResult.isSuccess) elapsed else null,
                    errorMessage = laravelResult.exceptionOrNull()?.message,
                    qrisify = qrisifyResult.getOrElse { e ->
                        QrisifyStatusDto(
                            ok = false,
                            statusCode = null,
                            responseTimeMs = 0,
                            error = e.message,
                            checkedAt = "",
                        )
                    },
                )
            }
        }
    }
}
