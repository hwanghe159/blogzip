package com.blogzip.domain

import java.time.DayOfWeek

class ReceiveDaysConverter {
  companion object {
    fun toString(attribute: List<DayOfWeek>): String {
      return attribute.joinToString(separator = ",") { it.name }
    }

    fun toList(dbData: String): List<DayOfWeek> {
      if (dbData.isBlank()) {
        return emptyList()
      }
      return dbData.split(",").map { DayOfWeek.valueOf(it) }
    }
  }
}