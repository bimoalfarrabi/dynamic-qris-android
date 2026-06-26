package id.viasco.dynamic_qris_android.data.repository

import id.viasco.dynamic_qris_android.data.local.TransactionDao
import id.viasco.dynamic_qris_android.data.mapper.toDomain
import id.viasco.dynamic_qris_android.data.mapper.toEntity
import id.viasco.dynamic_qris_android.data.remote.CreateTransactionRequest
import id.viasco.dynamic_qris_android.data.remote.QrisifyStatusDto
import id.viasco.dynamic_qris_android.data.remote.TransactionApi
import id.viasco.dynamic_qris_android.domain.model.Transaction
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val api: TransactionApi,
    private val dao: TransactionDao,
) : TransactionRepository {

    override fun observeAll(status: TransactionStatus?): Flow<List<Transaction>> {
        val source = if (status == null) dao.observeAll() else dao.observeByStatus(status)
        return source.map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeById(id: String): Flow<Transaction?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getCached(id: String): Transaction? = dao.getById(id)?.toDomain()

    override suspend fun refreshAll(status: TransactionStatus?): Result<Unit> = runCatching {
        val response = api.list(status = status?.name)
        dao.upsertAll(response.data.map { it.toEntity() })
    }

    override suspend fun refreshById(id: String): Result<Transaction> = runCatching {
        val response = api.show(id)
        val entity = response.data.toEntity()
        dao.upsert(entity)
        entity.toDomain()
    }

    override suspend fun create(
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

    override suspend fun cancel(id: String): Result<Transaction> = runCatching {
        val response = api.cancel(id)
        val entity = response.data.toEntity()
        dao.upsert(entity)
        entity.toDomain()
    }

    override suspend fun healthCheck(): Result<Unit> = runCatching {
        api.health()
    }

    override suspend fun checkQrisify(): Result<QrisifyStatusDto> = runCatching {
        api.qrisifyStatus()
    }
}
