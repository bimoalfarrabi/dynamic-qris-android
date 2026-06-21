package id.viasco.dynamic_qris_android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import id.viasco.dynamic_qris_android.ui.theme.StatusCancelled
import id.viasco.dynamic_qris_android.ui.theme.StatusCancelledContainer
import id.viasco.dynamic_qris_android.ui.theme.StatusExpired
import id.viasco.dynamic_qris_android.ui.theme.StatusExpiredContainer
import id.viasco.dynamic_qris_android.ui.theme.StatusPending
import id.viasco.dynamic_qris_android.ui.theme.StatusPendingContainer
import id.viasco.dynamic_qris_android.ui.theme.StatusSuccess
import id.viasco.dynamic_qris_android.ui.theme.StatusSuccessContainer

@Composable
fun StatusChip(status: TransactionStatus, modifier: Modifier = Modifier) {
    val (label, fg, bg) = when (status) {
        TransactionStatus.PENDING -> Triple("Menunggu", StatusPending, StatusPendingContainer)
        TransactionStatus.SUCCESS -> Triple("Berhasil", StatusSuccess, StatusSuccessContainer)
        TransactionStatus.EXPIRED -> Triple("Kedaluwarsa", StatusExpired, StatusExpiredContainer)
        TransactionStatus.CANCELLED -> Triple("Dibatalkan", StatusCancelled, StatusCancelledContainer)
    }
    StatusBadge(label = label, fg = fg, bg = bg, modifier = modifier)
}

@Composable
private fun StatusBadge(
    label: String,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
