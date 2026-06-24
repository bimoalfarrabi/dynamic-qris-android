package id.viasco.dynamic_qris_android.ui.common

/**
 * QR encoder using ZXing's bundled algorithm.
 *
 * Note: minimal implementation using BitMatrix → ImageBitmap conversion.
 * Avoids pulling a separate compose-qrcode dependency.
 */

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrEncoder {

    fun encodeBitmap(content: String, sizePx: Int = 1024): Bitmap? {
        if (content.isBlank()) return null
        return runCatching {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8",
            )
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val bmp = createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bmp[x, y] = if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                }
            }
            bmp
        }.getOrNull()
    }

    fun encode(content: String, sizePx: Int = 1024): ImageBitmap? =
        encodeBitmap(content, sizePx)?.asImageBitmap()
}
