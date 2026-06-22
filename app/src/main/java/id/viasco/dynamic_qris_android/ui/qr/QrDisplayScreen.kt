package id.viasco.dynamic_qris_android.ui.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.viasco.dynamic_qris_android.R
import id.viasco.dynamic_qris_android.domain.model.Transaction
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import id.viasco.dynamic_qris_android.ui.common.QrEncoder
import id.viasco.dynamic_qris_android.ui.common.StatusChip
import id.viasco.dynamic_qris_android.ui.common.formatRemaining
import id.viasco.dynamic_qris_android.ui.common.formatRupiah
import kotlinx.coroutines.delay
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrDisplayScreen(
    onBack: () -> Unit,
    viewModel: QrDisplayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val transaction = state.transaction
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.qr_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (transaction == null) {
                CircularProgressIndicator()
            } else {
                QrContent(
                    transaction = transaction,
                    isCancelling = state.isCancelling,
                    errorMessage = state.errorMessage,
                    onCancelClick = viewModel::showCancelConfirm,
                    onBack = onBack,
                )
            }
        }
    }

    if (state.showCancelConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCancelConfirm,
            title = { Text(stringResource(R.string.qr_cancel_confirm_title)) },
            text = { Text(stringResource(R.string.qr_cancel_confirm_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::cancel) {
                    Text(stringResource(R.string.qr_cancel_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissCancelConfirm) {
                    Text(stringResource(R.string.qr_cancel_confirm_no))
                }
            },
        )
    }
}

@Composable
private fun QrContent(
    transaction: Transaction,
    isCancelling: Boolean,
    errorMessage: String?,
    onCancelClick: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.qr_amount_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatRupiah(transaction.amountTotal ?: transaction.amountRequested),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        StatusChip(transaction.status)

        Spacer(Modifier.size(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (transaction.status) {
                TransactionStatus.PENDING -> QrImage(transaction.qrisString)
                TransactionStatus.SUCCESS -> StatusOverlay(stringResource(R.string.qr_status_success))
                TransactionStatus.EXPIRED -> StatusOverlay(stringResource(R.string.qr_status_expired))
                TransactionStatus.CANCELLED -> StatusOverlay(stringResource(R.string.qr_status_cancelled))
            }
        }

        if (transaction.status == TransactionStatus.PENDING && transaction.expiresAt != null) {
            ExpiryCountdown(transaction.expiresAt)
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.size(8.dp))

        if (transaction.status.isCancellable) {
            Button(
                onClick = onCancelClick,
                enabled = !isCancelling,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(stringResource(R.string.qr_cancel))
                }
            }
        } else {
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.qr_back))
            }
        }
    }
}

@Composable
private fun QrImage(qrisString: String?) {
    val bitmap = remember(qrisString) {
        qrisString?.let { QrEncoder.encode(it, sizePx = 1024) }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "QRIS",
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        CircularProgressIndicator()
    }
}

@Composable
private fun StatusOverlay(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleLarge,
        color = Color.Black,
    )
}

@Composable
private fun ExpiryCountdown(deadline: Instant) {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(deadline) {
        while (true) {
            now = Instant.now()
            delay(1_000)
        }
    }
    Text(
        text = stringResource(R.string.qr_expires_in, formatRemaining(now, deadline)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
