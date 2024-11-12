package com.blogzip.ai.common

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.stereotype.Component

@Component
class JsonlConverter {

    private val objectMapper = jsonMapper { addModule(kotlinModule()) }
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // 정의되지 않은 필드가 넘어오면 무시

    fun <T> objectsToJsonl(objects: List<T>): String {
        return objects.joinToString(separator = "\n") { obj ->
            objectMapper.writeValueAsString(obj)
        }
    }

    fun <T> jsonlToObjects(jsonl: String, valueType: Class<T>): List<T> {
        return jsonl.lines()
            .filter { it.isNotBlank() }
            .map { line -> objectMapper.readValue(line, valueType) }
    }

    fun jsonlToNodes(jsonl: String): List<JsonNode> {
        return jsonl.lines()
            .filter { it.isNotBlank() }
            .map { line -> objectMapper.readTree(line) }
    }
}