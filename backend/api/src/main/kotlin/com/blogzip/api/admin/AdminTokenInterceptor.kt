package com.blogzip.api.admin

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminTokenInterceptor(
  @Value("\${admin-token}") private val adminToken: String
) : HandlerInterceptor {

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    if (handler is HandlerMethod) {
      val method = handler.method
      if (method.isAnnotationPresent(AdminTokenRequired::class.java)) {
        val token = request.getHeader("Admin-Token")
        if (token == null || token != adminToken) {
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
          return false
        }
      }
    }
    return true
  }
}