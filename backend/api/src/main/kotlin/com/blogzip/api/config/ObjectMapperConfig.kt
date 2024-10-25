package com.blogzip.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class ObjectMapperConfig {

    @Bean
    fun jsonlObjectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        val javaTimeModule = JavaTimeModule()

        javaTimeModule.addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        javaTimeModule.addDeserializer(LocalDate::class.java, LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

        javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        javaTimeModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

        return objectMapper.registerModule(javaTimeModule)
    }
}