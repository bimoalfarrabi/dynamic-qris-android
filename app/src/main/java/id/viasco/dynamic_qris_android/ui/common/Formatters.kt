package id.viasco.dynamic_qris_android.ui.common

import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val idr: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
    maximumFractionDigits = 0
}

fun formatRupiah(amount: Long?): String =
    if (amount == null) "-" else idr.format(amount)

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("in", "ID"))
        .withZone(ZoneId.systemDefault())

fun formatDateTime(instant: Instant?): String =
    if (instant == null) "-" else dateTimeFormatter.format(instant)

/** Returns "MM:SS" countdown until [deadline], or "00:00" if past. */
fun formatRemaining(now: Instant, deadline: Instant): String {
    val seconds = Duration.between(now, deadline).seconds.coerceAtLeast(0)
    val mm = seconds / 60
    val ss = seconds % 60
    return "%02d:%02d".format(mm, ss)
}
