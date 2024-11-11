package com.blogzip.ai

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.stereotype.Component

@Component
class JsonlConverter(
    private val objectMapper: ObjectMapper
) {

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