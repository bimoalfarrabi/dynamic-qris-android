package id.viasco.dynamic_qris_android.data.repository

import id.viasco.dynamic_qris_android.data.local.TransactionDao
import id.viasco.dynamic_qris_android.data.mapper.toDomain
import id.viasco.dynamic_qris_android.data.mapper.toEntity
import id.viasco.dynamic_qris_android.data.remote.CreateTransactionRequest
import id.viasco.dynamic_qris_android.data.remote.TransactionApi
import id.viasco.dynamic_qris_android.domain.model.Transaction
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val api: TransactionApi,
    private val dao: TransactionDao,
) {
    /** Offline-first observation; cache is updated by [refreshAll] / [refreshById]. */
    fun observeAll(status: TransactionStatus? = null): Flow<List<Transaction>> {
        val source = if (status == null) dao.observeAll() else dao.observeByStatus(status)
        return source.map { rows -> rows.map { it.toDomain() } }
    }

    fun observeById(id: String): Flow<Transaction?> =
        dao.observeById(id).map { it?.toDomain() }

    suspend fun getCached(id: String): Transaction? = dao.getById(id)?.toDomain()

    /** Force refresh of full list from backend. Persists to Room. */
    suspend fun refreshAll(status: TransactionStatus? = null): Result<Unit> = runCatching {
        val response = api.list(status = status?.name)
        dao.upsertAll(response.data.map { it.toEntity() })
    }

    /** Force refresh of a single transaction. Persists to Room. */
    suspend fun refreshById(id: String): Result<Transaction> = runCatching {
        val response = api.show(id)
        val entity = response.data.toEntity()
        dao.upsert(entity)
        entity.toDomain()
    }

    suspend fun create(
        amount: Long,
        externalId: String?,
        expiryMinutes: Int,
    ): Result<Transaction> = runCatching {
        val response = api.create(
            CreateTransactionRequest(
                amount = amount,
                externalId = externalId?.takeIf { it.isNotBlank() },
                expiryMinutes = expiryMinutes,
            ),
        )
        val entity = response.data.toEntity()
        dao.upsert(entity)
        entity.toDomain()
    }

    suspend fun cancel(id: String): Result<Transaction> = runCatching {
        val response = api.cancel(id)
        val entity = response.data.toEntity()
        dao.upsert(entity)
        entity.toDomain()
    }
}
