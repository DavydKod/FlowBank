package com.davyd.dto

import java.math.BigDecimal

data class AccountAnalytics(
    val accountId: Long,
    val totalSent: BigDecimal,
    val totalReceived: BigDecimal,
    val transactionCount: Long,
    val sentTransactionCount: Long,
    val receivedTransactionCount: Long,
    val averageTransactionSent: BigDecimal,
    val averageTransactionReceived: BigDecimal,
    val largestSentTransaction: BigDecimal?,
    val largestReceivedTransaction: BigDecimal?
)
