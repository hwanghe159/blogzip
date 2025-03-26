package com.blogzip.api.config

import com.blogzip.api.admin.AdminTokenInterceptor
import com.blogzip.api.auth.AuthenticatedUserArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
  private val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
  private val adminTokenInterceptor: AdminTokenInterceptor,
) : WebMvcConfigurer {

  override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
    resolvers.add(authenticatedUserArgumentResolver)
  }

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(adminTokenInterceptor)
  }
}