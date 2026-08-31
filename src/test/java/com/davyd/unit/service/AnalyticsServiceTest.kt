package com.davyd.unit.service

import com.davyd.exception.BankAccountNotFoundException
import com.davyd.repository.BankAccountRepository
import com.davyd.repository.TransactionRepository
import com.davyd.service.AnalyticsService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class AnalyticsServiceTest {

    @Mock
    lateinit var transactionRepository: TransactionRepository

    @Mock
    lateinit var bankAccountRepository: BankAccountRepository

    private lateinit var analyticsService: AnalyticsService

    @BeforeEach
    fun setUp() {
        analyticsService = AnalyticsService(
            transactionRepository,
            bankAccountRepository
        )
    }

    @Test
    fun `should calculate account analytics`() {
        val accountId = 1L

        whenever(bankAccountRepository.existsById(accountId))
            .thenReturn(true)

        whenever(transactionRepository.getTotalSent(accountId))
            .thenReturn(BigDecimal("600.00"))

        whenever(transactionRepository.getTotalReceived(accountId))
            .thenReturn(BigDecimal("900.00"))

        whenever(transactionRepository.getSentTransactionCount(accountId))
            .thenReturn(3L)

        whenever(transactionRepository.getReceivedTransactionCount(accountId))
            .thenReturn(2L)

        whenever(transactionRepository.getLargestSentTransaction(accountId))
            .thenReturn(BigDecimal("300.00"))

        whenever(transactionRepository.getLargestReceivedTransaction(accountId))
            .thenReturn(BigDecimal("600.00"))

        val result = analyticsService.getAccountAnalytics(accountId)

        assertEquals(accountId, result.accountId)
        assertEquals(BigDecimal("600.00"), result.totalSent)
        assertEquals(BigDecimal("900.00"), result.totalReceived)

        assertEquals(5L, result.transactionCount)
        assertEquals(3L, result.sentTransactionCount)
        assertEquals(2L, result.receivedTransactionCount)

        assertEquals(
            BigDecimal("200.00"),
            result.averageTransactionSent
        )

        assertEquals(
            BigDecimal("450.00"),
            result.averageTransactionReceived
        )

        assertEquals(
            BigDecimal("300.00"),
            result.largestSentTransaction
        )

        assertEquals(
            BigDecimal("600.00"),
            result.largestReceivedTransaction
        )
    }

    @Test
    fun `should return zero averages when account has no transactions`() {
        val accountId = 1L

        whenever(bankAccountRepository.existsById(accountId))
            .thenReturn(true)

        whenever(transactionRepository.getTotalSent(accountId))
            .thenReturn(BigDecimal.ZERO)

        whenever(transactionRepository.getTotalReceived(accountId))
            .thenReturn(BigDecimal.ZERO)

        whenever(transactionRepository.getSentTransactionCount(accountId))
            .thenReturn(0L)

        whenever(transactionRepository.getReceivedTransactionCount(accountId))
            .thenReturn(0L)

        whenever(transactionRepository.getLargestSentTransaction(accountId))
            .thenReturn(null)

        whenever(transactionRepository.getLargestReceivedTransaction(accountId))
            .thenReturn(null)

        val result = analyticsService.getAccountAnalytics(accountId)

        assertEquals(0L, result.transactionCount)
        assertEquals(0L, result.sentTransactionCount)
        assertEquals(0L, result.receivedTransactionCount)

        assertEquals(
            BigDecimal.ZERO,
            result.averageTransactionSent
        )

        assertEquals(
            BigDecimal.ZERO,
            result.averageTransactionReceived
        )

        assertNull(result.largestSentTransaction)
        assertNull(result.largestReceivedTransaction)
    }

    @Test
    fun `should round average transaction amounts to two decimal places`() {
        val accountId = 1L

        whenever(bankAccountRepository.existsById(accountId))
            .thenReturn(true)

        whenever(transactionRepository.getTotalSent(accountId))
            .thenReturn(BigDecimal("100.00"))

        whenever(transactionRepository.getTotalReceived(accountId))
            .thenReturn(BigDecimal("200.00"))

        whenever(transactionRepository.getSentTransactionCount(accountId))
            .thenReturn(3L)

        whenever(transactionRepository.getReceivedTransactionCount(accountId))
            .thenReturn(3L)

        whenever(transactionRepository.getLargestSentTransaction(accountId))
            .thenReturn(BigDecimal("50.00"))

        whenever(transactionRepository.getLargestReceivedTransaction(accountId))
            .thenReturn(BigDecimal("100.00"))

        val result = analyticsService.getAccountAnalytics(accountId)

        assertEquals(
            BigDecimal("33.33"),
            result.averageTransactionSent
        )

        assertEquals(
            BigDecimal("66.67"),
            result.averageTransactionReceived
        )
    }

    @Test
    fun `should throw when account does not exist`() {
        val accountId = 999L

        whenever(bankAccountRepository.existsById(accountId))
            .thenReturn(false)

        assertThrows<BankAccountNotFoundException> {
            analyticsService.getAccountAnalytics(accountId)
        }
    }
}