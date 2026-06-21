package id.viasco.dynamic_qris_android.data.local

import androidx.room.TypeConverter
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: TransactionStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)
}
