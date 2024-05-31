package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.*
import com.blogzip.domain.User
import com.blogzip.api.common.GoogleAuthService
import com.blogzip.service.UserService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.DayOfWeek

@RestController
class UserController(
    private val userService: UserService,
    private val googleAuthService: GoogleAuthService,
) {

    @GetMapping("/api/v1/login/google")
    fun googleLogin(@RequestParam code: String): ResponseEntity<LoginResponse> {
        val response = googleAuthService.googleLogin(code)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/v1/me")
    fun me(@Parameter(hidden = true) @Authenticated user: AuthenticatedUser): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @PutMapping("/api/v1/me")
    fun update(
        @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
        @RequestBody request: UserUpdateRequest,
    ): ResponseEntity<UserResponse> {
        userService.update(user.id, request.receiveDays)
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @GetMapping("/api/v1/user/{day}")
    fun getByDay(@PathVariable day: DayOfWeek): ResponseEntity<List<UserResponse>> {
        val response = userService.findAllByDayOfWeek(day)
            .map { UserResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/v1/user")
    fun getAll(): ResponseEntity<List<User>> {
        val users = userService.findAll()
        return ResponseEntity.ok(users)
    }
}