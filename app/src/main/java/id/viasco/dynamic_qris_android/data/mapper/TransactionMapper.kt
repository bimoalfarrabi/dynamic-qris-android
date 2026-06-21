package id.viasco.dynamic_qris_android.data.mapper

import id.viasco.dynamic_qris_android.data.local.TransactionEntity
import id.viasco.dynamic_qris_android.data.remote.TransactionDto
import id.viasco.dynamic_qris_android.domain.model.Transaction
import java.time.Instant

private fun parseInstant(value: String?): Instant? =
    value?.let { runCatching { Instant.parse(it) }.getOrNull() }

fun TransactionDto.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    qrisifyTransactionId = qrisifyTransactionId,
    externalId = externalId,
    amountRequested = amountRequested,
    uniqueCode = uniqueCode,
    amountTotal = amountTotal,
    status = status,
    qrisString = qrisString,
    paymentProvider = paymentProvider,
    expiresAt = parseInstant(expiresAt)?.toEpochMilli(),
    paidAt = parseInstant(paidAt)?.toEpochMilli(),
    cancelledAt = parseInstant(cancelledAt)?.toEpochMilli(),
    createdAt = parseInstant(createdAt)?.toEpochMilli() ?: System.currentTimeMillis(),
    updatedAt = parseInstant(updatedAt)?.toEpochMilli() ?: System.currentTimeMillis(),
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    qrisifyTransactionId = qrisifyTransactionId,
    externalId = externalId,
    amountRequested = amountRequested,
    uniqueCode = uniqueCode,
    amountTotal = amountTotal,
    status = status,
    qrisString = qrisString,
    paymentProvider = paymentProvider,
    expiresAt = expiresAt?.let(Instant::ofEpochMilli),
    paidAt = paidAt?.let(Instant::ofEpochMilli),
    cancelledAt = cancelledAt?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

fun TransactionDto.toDomain(): Transaction = toEntity().toDomain()
