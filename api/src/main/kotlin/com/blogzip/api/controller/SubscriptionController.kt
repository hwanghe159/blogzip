package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.BlogResponse
import com.blogzip.api.dto.SubscriptionResponse
import com.blogzip.service.SubscriptionService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class SubscriptionController(
    private val subscriptionService: SubscriptionService,
) {

    @GetMapping("/api/v1/subscription")
    fun getMySubscriptions(@Parameter(hidden = true) @Authenticated user: AuthenticatedUser): ResponseEntity<List<SubscriptionResponse>> {
        val response = subscriptionService.findByUserId(user.id)
            .map {
                SubscriptionResponse(
                    id = it.id!!,
                    blog = BlogResponse.from(it.blog),
                    createdAt = it.createdAt,
                )
            }
        return ResponseEntity.ok(response)
    }
}