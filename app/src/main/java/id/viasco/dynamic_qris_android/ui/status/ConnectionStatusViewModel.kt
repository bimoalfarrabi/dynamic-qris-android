package id.viasco.dynamic_qris_android.ui.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
        val isConnected: Boolean? = null, // null = not checked yet
        val errorMessage: String? = null,
        val responseTimeMs: Long? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        check()
    }

    fun check() {
        _state.update { it.copy(isChecking = true, errorMessage = null) }
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
    }
}
