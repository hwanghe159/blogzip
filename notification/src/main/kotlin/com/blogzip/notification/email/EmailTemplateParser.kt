package com.blogzip.notification.email

import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class EmailTemplateParser(private val templateEngine: TemplateEngine) {

    companion object {
        // ex: "5/8(수)"
        val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d(EEE)", Locale.KOREA)
    }

    fun parseArticles(user: User, articles: List<Article>): String {
        val context = Context()
        context.setVariables(
            mapOf(
                "articles" to articles,
                "dateRange" to createDateRangeString(user.receiveDates),
            )
        )
        return templateEngine.process("email/new-articles", context)
    }

    fun parseVerification(address: String, code: String): String {
        val context = Context()
        context.setVariable("address", address)
        context.setVariable("code", code)
        return templateEngine.process("email/email-address-verification", context)
    }

    private fun createDateRangeString(dates: List<LocalDate>): String {
        if (dates.isEmpty()) {
            return ""
        }
        val sortedDates = dates.sorted()
        val first = sortedDates.first().format(FORMATTER)
        val last = sortedDates.last().format(FORMATTER)
        if (first == last) {
            return first
        } else {
            return "${first} ~ ${last} 사이"
        }
    }
}