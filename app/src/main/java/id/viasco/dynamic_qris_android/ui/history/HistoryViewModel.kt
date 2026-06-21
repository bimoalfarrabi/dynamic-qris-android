package id.viasco.dynamic_qris_android.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.viasco.dynamic_qris_android.data.repository.TransactionRepository
import id.viasco.dynamic_qris_android.domain.model.Transaction
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TransactionRepository,
) : ViewModel() {

    data class State(
        val filter: TransactionStatus? = null,
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = _state
        .flatMapLatest { repository.observeAll(it.filter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun setFilter(status: TransactionStatus?) {
        _state.update { it.copy(filter = status) }
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            repository.refreshAll(_state.value.filter).fold(
                onSuccess = { _state.update { it.copy(isRefreshing = false) } },
                onFailure = { e ->
                    _state.update {
                        it.copy(isRefreshing = false, errorMessage = e.message)
                    }
                },
            )
        }
    }
}
