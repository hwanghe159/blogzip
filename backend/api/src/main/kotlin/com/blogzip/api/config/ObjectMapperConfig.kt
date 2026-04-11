package com.blogzip.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class ObjectMapperConfig {

  @Bean
  fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
    val javaTimeModule = JavaTimeModule()

    javaTimeModule.addSerializer(
      LocalDate::class.java,
      LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    )
    javaTimeModule.addDeserializer(
      LocalDate::class.java,
      LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    )

    javaTimeModule.addSerializer(
      LocalDateTime::class.java,
      LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
    javaTimeModule.addDeserializer(
      LocalDateTime::class.java,
      LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )

    return Jackson2ObjectMapperBuilderCustomizer { builder ->
      builder.postConfigurer { mapper ->
        mapper.registerModule(KotlinModule.Builder().build())
        mapper.registerModule(javaTimeModule)
      }
    }
  }

  @Bean
  @Primary
  fun jsonlObjectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
    return builder.createXmlMapper(false).build()
  }
}
