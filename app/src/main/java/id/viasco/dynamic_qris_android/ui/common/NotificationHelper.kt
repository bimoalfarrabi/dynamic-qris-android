package id.viasco.dynamic_qris_android.ui.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import id.viasco.dynamic_qris_android.R

object NotificationHelper {

    private const val CHANNEL_ID = "connection_status"
    private const val CHANNEL_NAME = "Status Koneksi"
    const val NOTIF_ID_LARAVEL = 1001
    const val NOTIF_ID_QRISIFY = 1002
    private const val NOTIF_ID_PAYMENT = 1003

    private const val PREFS_NAME = "notif_state"
    private const val KEY_LARAVEL_WAS_DOWN = "laravel_was_down"
    private const val KEY_QRISIFY_WAS_DOWN = "qrisify_was_down"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /** Dipanggil sekali saat payment status SUCCESS. Tidak ada cooldown — hanya terjadi sekali per transaksi. */
    fun notifyPaymentSuccess(context: Context, amount: String) {
        notify(
            context,
            NOTIF_ID_PAYMENT,
            context.getString(R.string.notif_payment_success_title),
            context.getString(R.string.notif_payment_success_body, amount),
        )
    }

    /**
     * - Jika notif sebelumnya belum di-dismiss user → skip (avoid spam).
     * - Jika sudah di-dismiss (atau belum pernah notif) → kirim.
     * - Notif pertama: "down". Notif berikutnya (masih down): "masih down".
     */
    fun handleLaravelDown(context: Context) {
        if (!isNotifActive(context, NOTIF_ID_LARAVEL)) {
            val wasDown = wasDown(context, KEY_LARAVEL_WAS_DOWN)
            notify(
                context,
                NOTIF_ID_LARAVEL,
                context.getString(R.string.notif_laravel_down_title),
                if (wasDown) context.getString(R.string.notif_laravel_still_down_body)
                else context.getString(R.string.notif_laravel_down_body),
            )
        }
        setWasDown(context, KEY_LARAVEL_WAS_DOWN, true)
    }

    /**
     * Dipanggil saat QRIS-ify down.
     * Sama seperti handleLaravelDown.
     */
    fun handleQrisifyDown(context: Context) {
        if (!isNotifActive(context, NOTIF_ID_QRISIFY)) {
            val wasDown = wasDown(context, KEY_QRISIFY_WAS_DOWN)
            notify(
                context,
                NOTIF_ID_QRISIFY,
                context.getString(R.string.notif_qrisify_down_title),
                if (wasDown) context.getString(R.string.notif_qrisify_still_down_body)
                else context.getString(R.string.notif_qrisify_down_body),
            )
        }
        setWasDown(context, KEY_QRISIFY_WAS_DOWN, true)
    }

    /**
     * Dipanggil saat Laravel up.
     * Kirim notif "up" sekali hanya jika sebelumnya down — tidak ikut jadwal 1 jam.
     */
    fun handleLaravelUp(context: Context) {
        if (wasDown(context, KEY_LARAVEL_WAS_DOWN)) {
            NotificationManagerCompat.from(context).cancel(NOTIF_ID_LARAVEL)
            notify(
                context,
                NOTIF_ID_LARAVEL,
                context.getString(R.string.notif_laravel_up_title),
                context.getString(R.string.notif_laravel_up_body),
            )
            setWasDown(context, KEY_LARAVEL_WAS_DOWN, false)
        }
    }

    /**
     * Dipanggil saat QRIS-ify up.
     * Sama seperti handleLaravelUp.
     */
    fun handleQrisifyUp(context: Context) {
        if (wasDown(context, KEY_QRISIFY_WAS_DOWN)) {
            NotificationManagerCompat.from(context).cancel(NOTIF_ID_QRISIFY)
            notify(
                context,
                NOTIF_ID_QRISIFY,
                context.getString(R.string.notif_qrisify_up_title),
                context.getString(R.string.notif_qrisify_up_body),
            )
            setWasDown(context, KEY_QRISIFY_WAS_DOWN, false)
        }
    }

    // ponytail: activeNotifications tersedia sejak API 23, minSdk=31, aman
    private fun isNotifActive(context: Context, notifId: Int): Boolean =
        context.getSystemService(NotificationManager::class.java)
            .activeNotifications.any { it.id == notifId }

    private fun wasDown(context: Context, key: String): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(key, false)

    private fun setWasDown(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(key, value).apply()
    }

    private fun notify(context: Context, id: Int, title: String, body: String) {
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notif)
    }
}
