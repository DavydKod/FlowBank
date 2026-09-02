package com.davyd.integration.service

import com.davyd.service.AnalyticsService
import com.davyd.service.BankAccountService
import com.davyd.service.TransactionService
import com.davyd.service.UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnalyticsServiceIT : BaseServiceIT() {

    @Autowired
    lateinit var analyticsService: AnalyticsService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var bankAccountService: BankAccountService

    @Autowired
    lateinit var transactionService: TransactionService

    @Test
    fun `should calculate analytics including transfers deposits and withdrawals`() {
        val user1 = userService.createUser(
            "Davyd",
            "davyd@gmail.com"
        )

        val user2 = userService.createUser(
            "Max",
            "max@gmail.com"
        )

        val account1 = bankAccountService.createAccount(user1.id())
        val account2 = bankAccountService.createAccount(user2.id())

        transactionService.deposit(
            account1.id(),
            BigDecimal("1000.00"),
            "deposit-1"
        )

        transactionService.deposit(
            account2.id(),
            BigDecimal("500.00"),
            "deposit-2"
        )

        transactionService.transfer(
            account1.id(),
            account2.id(),
            BigDecimal("300.00"),
            "transfer-1"
        )

        transactionService.transfer(
            account2.id(),
            account1.id(),
            BigDecimal("200.00"),
            "transfer-2"
        )

        transactionService.withdraw(
            account1.id(),
            BigDecimal("100.00"),
            "withdraw-1"
        )

        val result = analyticsService.getAccountAnalytics(account1.id())

        assertEquals(BigDecimal("400.00"), result.totalSent)
        assertEquals(BigDecimal("1200.00"), result.totalReceived)

        assertEquals(4L, result.transactionCount)

        assertEquals(2L, result.sentTransactionCount)
        assertEquals(2L, result.receivedTransactionCount)

        assertEquals(
            BigDecimal("200.00"),
            result.averageTransactionSent
        )

        assertEquals(
            BigDecimal("600.00"),
            result.averageTransactionReceived
        )

        assertEquals(
            BigDecimal("300.00"),
            result.largestSentTransaction
        )

        assertEquals(
            BigDecimal("1000.00"),
            result.largestReceivedTransaction
        )
    }

    @Test
    fun `should return empty analytics for account without transactions`() {
        val user = userService.createUser(
            "Davyd",
            "davyd@gmail.com"
        )

        val account = bankAccountService.createAccount(user.id())

        val result = analyticsService.getAccountAnalytics(account.id())

        assertEquals(BigDecimal.ZERO, result.totalSent)
        assertEquals(BigDecimal.ZERO, result.totalReceived)

        assertEquals(0L, result.transactionCount)
        assertEquals(0L, result.sentTransactionCount)
        assertEquals(0L, result.receivedTransactionCount)

        assertEquals(BigDecimal.ZERO, result.averageTransactionSent)
        assertEquals(BigDecimal.ZERO, result.averageTransactionReceived)

        assertNull(result.largestSentTransaction)
        assertNull(result.largestReceivedTransaction)
    }
}