package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.SubscriptionCreateRequest
import com.blogzip.api.dto.SubscriptionDeleteRequest
import com.blogzip.api.dto.SubscriptionResponse
import com.blogzip.service.SubscriptionCommandService
import com.blogzip.service.SubscriptionQueryService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class SubscriptionController(
  private val subscriptionCommandService: SubscriptionCommandService,
  private val subscriptionQueryService: SubscriptionQueryService
) {

  @GetMapping("/api/v1/subscription")
  fun getMySubscriptions(@Parameter(hidden = true) @Authenticated user: AuthenticatedUser)
          : ResponseEntity<List<SubscriptionResponse>> {
    val response = subscriptionQueryService.findAllByUserId(user.id)
      .sortedByDescending { it.subscription.id }
      .map { SubscriptionResponse.from(it) }
    return ResponseEntity.ok(response)
  }

  @PostMapping("/api/v1/subscription")
  fun addSubscription(
    @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
    @RequestBody request: SubscriptionCreateRequest,
  ): ResponseEntity<SubscriptionResponse> {
    val subscription = subscriptionCommandService.save(user.id, request.blogId)
    val response = SubscriptionResponse.from(subscription)
    return ResponseEntity.ok(response)
  }

  @DeleteMapping("/api/v1/subscription")
  fun deleteSubscription(
    @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
    @RequestBody request: SubscriptionDeleteRequest,
  ): ResponseEntity<Void> {
    subscriptionCommandService.delete(user.id, request.blogId)
    return ResponseEntity.noContent().build()
  }
}