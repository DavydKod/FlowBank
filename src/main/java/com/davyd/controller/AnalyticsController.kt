package com.davyd.controller

import com.davyd.dto.response.AccountAnalytics
import com.davyd.service.AnalyticsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Analytics", description = "Analytics for user")
@RestController
@RequestMapping("/analytics")
class AnalyticsController(private val analyticsService: AnalyticsService) {

    @Operation(
        summary = "Get bank account analytics",
        description = "Returns transaction analytics for the specified bank account"
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
            responseCode = "404",
            description = "Bank account not found"
        )
    ])
    @GetMapping("accounts/{id}")
    fun getBankAccountAnalytics(
        @Parameter(description = "Bank account ID", example = "1")
        @PathVariable("id")
        @Positive
        accountId: Long): ResponseEntity<AccountAnalytics> =
        ResponseEntity.ok(analyticsService.getAccountAnalytics(accountId))
}