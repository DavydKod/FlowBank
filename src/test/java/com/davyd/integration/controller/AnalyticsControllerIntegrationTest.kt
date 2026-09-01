package com.davyd.integration.controller

import com.davyd.integration.TestcontainersConfiguration
import com.davyd.models.BankAccount
import com.davyd.models.User
import com.davyd.repository.BankAccountRepository
import com.davyd.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class AnalyticsControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var bankAccountRepository: BankAccountRepository

    @AfterEach
    fun cleanUp() {
        bankAccountRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun shouldGetBankAccountAnalytics() {
        val user = userRepository.save(
            User("Davyd", "davyd@gmail.com")
        )

        val account = bankAccountRepository.save(
            BankAccount(user)
        )

        mockMvc.get("/analytics/accounts/{id}", account.id)
            .andExpect {
                status { isOk() }
                content {
                    contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                }

                jsonPath("$.accountId") {
                    value(account.id)
                }

                jsonPath("$.totalSent") {
                    value(0)
                }

                jsonPath("$.totalReceived") {
                    value(0)
                }

                jsonPath("$.sentTransactionCount") {
                    value(0)
                }

                jsonPath("$.receivedTransactionCount") {
                    value(0)
                }
            }
    }

    @Test
    fun shouldReturn404WhenBankAccountDoesNotExist() {
        mockMvc.get("/analytics/accounts/{id}", 999999L)
            .andExpect {
                status { isNotFound() }

                jsonPath("$.status") {
                    value(404)
                }

                jsonPath("$.error") {
                    value("Not Found")
                }

                jsonPath("$.message") {
                    isNotEmpty()
                }

                jsonPath("$.createdAt") {
                    exists()
                }
            }
    }

    @Test
    fun shouldReturn400WhenBankAccountIdIsNotPositive() {
        mockMvc.get("/analytics/accounts/{id}", 0)
            .andExpect {
                status { isBadRequest() }

                jsonPath("$.status") {
                    value(400)
                }

                jsonPath("$.error") {
                    value("Bad Request")
                }

                jsonPath("$.message") {
                    isNotEmpty()
                }

                jsonPath("$.createdAt") {
                    exists()
                }
            }
    }
}