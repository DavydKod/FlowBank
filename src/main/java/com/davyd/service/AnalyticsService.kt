package com.davyd.service

import com.davyd.repository.BankAccountRepository
import com.davyd.repository.TransactionRepository
import com.davyd.dto.response.AccountAnalytics
import com.davyd.exception.BankAccountNotFoundException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class AnalyticsService (
    private val transactionRepository: TransactionRepository,
    private val bankAccountRepository: BankAccountRepository
){
    fun getAccountAnalytics(accountId: Long): AccountAnalytics {
        if (!bankAccountRepository.existsById(accountId)) {
            throw BankAccountNotFoundException(accountId)
        }

        val totalSent: BigDecimal = transactionRepository.getTotalSent(accountId)

        val totalReceived: BigDecimal = transactionRepository.getTotalReceived(accountId)

        val sentTransactionCount: Long = transactionRepository.getSentTransactionCount(accountId)

        val receivedTransactionCount: Long = transactionRepository.getReceivedTransactionCount(accountId)

        val transactionCount: Long = sentTransactionCount + receivedTransactionCount

        val averageTransactionSent: BigDecimal = if (sentTransactionCount == 0L){
            BigDecimal.ZERO} else {
                totalSent.divide(BigDecimal.valueOf(sentTransactionCount),
                    2, RoundingMode.HALF_UP)
        }

        val averageTransactionReceived: BigDecimal = if (receivedTransactionCount == 0L){
            BigDecimal.ZERO} else {
            totalReceived.divide(BigDecimal(receivedTransactionCount),
                2,
                RoundingMode.HALF_UP)
        }

        val largestSentTransaction: BigDecimal? = transactionRepository.getLargestSentTransaction(accountId)

        val largestReceivedTransaction: BigDecimal? = transactionRepository.getLargestReceivedTransaction(accountId)

        return AccountAnalytics(accountId = accountId, totalSent = totalSent, totalReceived = totalReceived,
            transactionCount = transactionCount, sentTransactionCount = sentTransactionCount,
            receivedTransactionCount = receivedTransactionCount,
            averageTransactionSent = averageTransactionSent, averageTransactionReceived = averageTransactionReceived,
            largestSentTransaction = largestSentTransaction, largestReceivedTransaction = largestReceivedTransaction)
    }
}