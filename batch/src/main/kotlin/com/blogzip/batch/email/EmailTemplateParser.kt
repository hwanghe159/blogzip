package com.blogzip.batch.email

import com.blogzip.domain.Article
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
}