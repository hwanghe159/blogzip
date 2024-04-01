package com.blogzip.domain

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.DayOfWeek

@Converter
class ReceiveDaysConverter : AttributeConverter<List<DayOfWeek>, String> {
    override fun convertToDatabaseColumn(attribute: List<DayOfWeek>?): String? {
        return attribute?.joinToString(separator = ",") { it.name }
    }

    override fun convertToEntityAttribute(dbData: String?): List<DayOfWeek>? {
        return dbData?.split(",")?.map { DayOfWeek.valueOf(it) }
    }
}