package com.davyd.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Analytics for a bank account")
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