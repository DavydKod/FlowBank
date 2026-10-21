package com.davyd.analytics

import com.davyd.analytics.dto.AccountAnalytics
import com.davyd.analytics.AnalyticsService
import com.davyd.security.principal.FlowBankPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Analytics", description = "Current user bank account analytics using session authentication")
@RestController
@RequestMapping("/analytics")
class AnalyticsController(private val analyticsService: AnalyticsService) {

    @Operation(
        summary = "Get bank account analytics",
        description = "Returns transaction totals, counts, averages and largest amounts for a bank account " +
            "owned by the user authenticated by the session cookie. " +
            "Average amounts are zero and largest amounts are null when there are no transactions " +
            "in the corresponding direction."
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Bank account analytics retrieved successfully"
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid bank account ID"
        ),
        ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "403",
            description = "Bank account belongs to another user",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Bank account not found"
        )
    ])
    @GetMapping("accounts/{id}")
    fun getBankAccountAnalytics(
        @Parameter(description = "Bank account ID", example = "1")
        @PathVariable("id")
        @Positive
        accountId: Long,
        @Parameter(hidden = true)
        @AuthenticationPrincipal principal: FlowBankPrincipal): ResponseEntity<AccountAnalytics> =
        ResponseEntity.ok(analyticsService.getAccountAnalytics(principal.userId, accountId))
}