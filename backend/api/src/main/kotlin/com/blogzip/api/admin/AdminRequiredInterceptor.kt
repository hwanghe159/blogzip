package com.blogzip.api.admin

import com.blogzip.api.common.JwtService
import com.blogzip.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminRequiredInterceptor(
  private val jwtService: JwtService,
  private val userService: UserService,
) : HandlerInterceptor {

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    if (handler is HandlerMethod) {
      val method = handler.method
      val needAdminAuth = method.isAnnotationPresent(AdminRequired::class.java) ||
        request.requestURI.startsWith("/api/admin/")
      if (!needAdminAuth) {
        return true
      }

      val authorizationHeader = request.getHeader("Authorization")
      if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        return false
      }
      val token = authorizationHeader.substring(7)
      val email = jwtService.getEmail(token)
      if (email == null) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        return false
      }
      val user = userService.findByEmailIncludingDeleted(email)
      if (user == null || user.isDeleted || !user.isAdmin) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
        return false
      }
    }
    return true
  }
}
