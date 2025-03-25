package com.blogzip.api.auth

import com.blogzip.api.common.JwtService
import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.service.UserService
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthenticatedUserArgumentResolver(
  private val jwtService: JwtService,
  private val userService: UserService,
) : HandlerMethodArgumentResolver {

  override fun supportsParameter(parameter: MethodParameter): Boolean {
    return parameter.hasParameterAnnotation(Authenticated::class.java)
  }

  override fun resolveArgument(
    parameter: MethodParameter,
    mavContainer: ModelAndViewContainer?,
    webRequest: NativeWebRequest,
    binderFactory: WebDataBinderFactory?
  ): Any? {
    val authorizationHeader = webRequest.getHeader("Authorization")
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw DomainException(ErrorCode.LOGIN_FAILED)
    }
    val token = authorizationHeader.substring(7)
    val email = jwtService.getEmail(token) ?: throw DomainException(ErrorCode.LOGIN_FAILED)
    val user =
      userService.findByEmail(email) ?: throw DomainException(ErrorCode.LOGIN_FAILED)
    return AuthenticatedUser(
      id = user.id!!,
      email = user.email,
      socialType = user.socialType,
      socialId = user.socialId,
      receiveDays = user.receiveDays,
      createdAt = user.createdAt,
      updatedAt = user.updatedAt,
    )
  }
}