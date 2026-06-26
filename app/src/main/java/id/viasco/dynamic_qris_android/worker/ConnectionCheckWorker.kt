package id.viasco.dynamic_qris_android.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import id.viasco.dynamic_qris_android.data.repository.TransactionRepository
import id.viasco.dynamic_qris_android.ui.common.NotificationHelper

@HiltWorker
class ConnectionCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TransactionRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        NotificationHelper.createChannel(applicationContext)

        repository.healthCheck().fold(
            onSuccess = { NotificationHelper.handleLaravelUp(applicationContext) },
            onFailure = { NotificationHelper.handleLaravelDown(applicationContext) },
        )

        repository.checkQrisify().fold(
            onSuccess = { dto ->
                if (dto.ok) NotificationHelper.handleQrisifyUp(applicationContext)
                else NotificationHelper.handleQrisifyDown(applicationContext)
            },
            onFailure = { NotificationHelper.handleQrisifyDown(applicationContext) },
        )

        return Result.success()
    }
}
