package id.viasco.dynamic_qris_android.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.viasco.dynamic_qris_android.data.repository.TransactionRepository
import id.viasco.dynamic_qris_android.domain.model.Transaction
import id.viasco.dynamic_qris_android.ui.navigation.Screen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: TransactionRepository,
) : ViewModel() {

    private val transactionId: String = checkNotNull(
        savedStateHandle[Screen.Detail.ARG_TRANSACTION_ID],
    )

    val transaction: StateFlow<Transaction?> = repository.observeById(transactionId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { repository.refreshById(transactionId) }
    }
}
