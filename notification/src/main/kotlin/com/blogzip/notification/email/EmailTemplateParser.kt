package com.blogzip.notification.email

import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Component
class EmailTemplateParser(private val templateEngine: TemplateEngine) {
    fun parseArticles(articles: List<Article>): String {
        val context = Context()
        context.setVariable("articles", articles)
        return templateEngine.process("email/new-articles", context)
    }

    fun parseVerification(address: String, code: String): String {
        val context = Context()
        context.setVariable("address", address)
        context.setVariable("code", code)
        return templateEngine.process("email/email-address-verification", context)
    }
}