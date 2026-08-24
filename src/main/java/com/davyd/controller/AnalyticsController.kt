package com.davyd.controller

import com.davyd.dto.response.AccountAnalytics
import com.davyd.service.AnalyticsService
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

    @GetMapping("accounts/{id}")
    fun getBankAccountAnalytics(@PathVariable("id") @Positive accountId: Long):
            ResponseEntity<AccountAnalytics>{
        return ResponseEntity.ok(analyticsService.getAccountAnalytics(accountId))
    }
}