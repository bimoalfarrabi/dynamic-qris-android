package id.viasco.dynamic_qris_android.data.repository

import id.viasco.dynamic_qris_android.data.remote.QrisifyStatusDto
import id.viasco.dynamic_qris_android.domain.model.Transaction
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

/** Abstraction over transaction data access (SOLID — Dependency Inversion). */
interface TransactionRepository {
    fun observeAll(status: TransactionStatus? = null): Flow<List<Transaction>>
    fun observeById(id: String): Flow<Transaction?>
    suspend fun getCached(id: String): Transaction?
    suspend fun refreshAll(status: TransactionStatus? = null): Result<Unit>
    suspend fun refreshById(id: String): Result<Transaction>
    suspend fun create(amount: Long, externalId: String?, expiryMinutes: Int): Result<Transaction>
    suspend fun cancel(id: String): Result<Transaction>
    suspend fun healthCheck(): Result<Unit>
    suspend fun checkQrisify(): Result<QrisifyStatusDto>
}
