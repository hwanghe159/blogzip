package com.blogzip.api.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
}