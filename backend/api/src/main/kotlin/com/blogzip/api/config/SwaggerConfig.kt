package com.blogzip.api.config

import com.blogzip.api.admin.AdminTokenRequired
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OperationCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod

@Configuration
class SwaggerConfig {

  @Bean
  fun openAPI(): OpenAPI {
    return OpenAPI()
      .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
      .components(
        Components()
          .addSecuritySchemes(
            "bearerAuth",
            SecurityScheme()
              .name("bearerAuth")
              .type(SecurityScheme.Type.HTTP)
              .scheme("bearer")
              .bearerFormat("JWT")
          )
      )
  }

  @Bean
  fun customGlobalHeaders(): OperationCustomizer {
    return OperationCustomizer { operation: Operation, handlerMethod: HandlerMethod ->
      if (handlerMethod.hasMethodAnnotation(AdminTokenRequired::class.java)) {
        val adminTokenHeader = Parameter()
          .`in`("header")
          .schema(StringSchema())
          .name("Admin-Token")
          .description("어드민 토큰")
          .required(true)
        operation.addParametersItem(adminTokenHeader)
      }
      operation
    }
  }

  @Bean
  fun publicApi(): GroupedOpenApi {
    return GroupedOpenApi.builder()
      .group("public")
      .pathsToMatch("/api/**")
      .addOperationCustomizer(customGlobalHeaders())
      .build()
  }
}