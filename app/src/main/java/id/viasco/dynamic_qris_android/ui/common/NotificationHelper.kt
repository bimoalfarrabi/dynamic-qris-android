package id.viasco.dynamic_qris_android.ui.common

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
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

    fun handleLaravelDown(context: Context) = handleDown(
        context,
        notifId = NOTIF_ID_LARAVEL,
        wasDownKey = KEY_LARAVEL_WAS_DOWN,
        title = context.getString(R.string.notif_laravel_down_title),
        firstBody = context.getString(R.string.notif_laravel_down_body),
        stillBody = context.getString(R.string.notif_laravel_still_down_body),
    )

    fun handleLaravelUp(context: Context) = handleUp(
        context,
        notifId = NOTIF_ID_LARAVEL,
        wasDownKey = KEY_LARAVEL_WAS_DOWN,
        title = context.getString(R.string.notif_laravel_up_title),
        body = context.getString(R.string.notif_laravel_up_body),
    )

    fun handleQrisifyDown(context: Context) = handleDown(
        context,
        notifId = NOTIF_ID_QRISIFY,
        wasDownKey = KEY_QRISIFY_WAS_DOWN,
        title = context.getString(R.string.notif_qrisify_down_title),
        firstBody = context.getString(R.string.notif_qrisify_down_body),
        stillBody = context.getString(R.string.notif_qrisify_still_down_body),
    )

    fun handleQrisifyUp(context: Context) = handleUp(
        context,
        notifId = NOTIF_ID_QRISIFY,
        wasDownKey = KEY_QRISIFY_WAS_DOWN,
        title = context.getString(R.string.notif_qrisify_up_title),
        body = context.getString(R.string.notif_qrisify_up_body),
    )

    // --- private helpers ---

    /**
     * Kirim notif "down" dengan anti-spam:
     * - Skip jika notif sebelumnya belum di-dismiss user.
     * - Teks berbeda untuk first-down vs still-down.
     */
    private fun handleDown(
        context: Context,
        notifId: Int,
        wasDownKey: String,
        title: String,
        firstBody: String,
        stillBody: String,
    ) {
        if (!isNotifActive(context, notifId)) {
            notify(context, notifId, title, if (wasDown(context, wasDownKey)) stillBody else firstBody)
        }
        setWasDown(context, wasDownKey, true)
    }

    /** Kirim notif "up" hanya jika sebelumnya pernah down. */
    private fun handleUp(
        context: Context,
        notifId: Int,
        wasDownKey: String,
        title: String,
        body: String,
    ) {
        if (wasDown(context, wasDownKey)) {
            NotificationManagerCompat.from(context).cancel(notifId)
            notify(context, notifId, title, body)
        }
        setWasDown(context, wasDownKey, false)
    }

    private fun isNotifActive(context: Context, notifId: Int): Boolean =
        NotificationManagerCompat.from(context).activeNotifications
            .any { it.id == notifId }

    private fun wasDown(context: Context, key: String): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(key, false)

    private fun setWasDown(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(key, value) }
    }

    private fun notify(context: Context, id: Int, title: String, body: String) {
        // Android 13+ requires POST_NOTIFICATIONS permission at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) return

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
