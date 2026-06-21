package id.viasco.dynamic_qris_android.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.viasco.dynamic_qris_android.R
import id.viasco.dynamic_qris_android.domain.model.Transaction
import id.viasco.dynamic_qris_android.ui.common.StatusChip
import id.viasco.dynamic_qris_android.ui.common.formatDateTime
import id.viasco.dynamic_qris_android.ui.common.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    onBack: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
) {
    val transaction by viewModel.transaction.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        if (transaction == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            DetailContent(
                transaction = transaction!!,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun DetailContent(transaction: Transaction, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.detail_status),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 12.dp),
            )
            StatusChip(transaction.status)
        }

        HorizontalDivider()

        DetailRow(stringResource(R.string.detail_external_id), transaction.externalId ?: "-")
        DetailRow(stringResource(R.string.detail_amount_requested), formatRupiah(transaction.amountRequested))
        DetailRow(stringResource(R.string.detail_unique_code), transaction.uniqueCode?.toString() ?: "-")
        DetailRow(stringResource(R.string.detail_amount_total), formatRupiah(transaction.amountTotal))
        DetailRow(stringResource(R.string.detail_payment_provider), transaction.paymentProvider ?: "-")

        HorizontalDivider()

        DetailRow(stringResource(R.string.detail_created_at), formatDateTime(transaction.createdAt))
        DetailRow(stringResource(R.string.detail_expires_at), formatDateTime(transaction.expiresAt))
        DetailRow(stringResource(R.string.detail_paid_at), formatDateTime(transaction.paidAt))
        DetailRow(stringResource(R.string.detail_cancelled_at), formatDateTime(transaction.cancelledAt))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
