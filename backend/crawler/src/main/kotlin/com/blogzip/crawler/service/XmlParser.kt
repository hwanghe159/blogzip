package com.blogzip.crawler.service

import com.blogzip.crawler.dto.Article
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.StringReader
import java.time.ZoneId

// contents 또는 description 이 500자 이하인 경우, 요약본으로 판단.
private val SyndEntry.content: String?
    get() {
        var result: String
        if (this.contents.isEmpty()) {
            if (this.description.value.length <= 500) {
                return null
            }
            result = this.description.value
        } else {
            val content = this.contents[0].value
            if (content.length <= 500) {
                return null
            }
            result = content
        }

        val cDataRegex = "<!\\[CDATA\\[(.*?)]]>".toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
        result = cDataRegex.find(result)?.groups?.get(1)?.value ?: result
        return result
    }

@Component
class XmlParser(
    private val htmlCompressor: HtmlCompressor,
) {

    fun convertToArticles(xml: String): List<Article> {
        val input = SyndFeedInput()
        val entries = input.build(BufferedReader(StringReader(xml))).entries
        val articles = entries.map {
            Article(
                title = it.title,
                content = it.content?.let { html -> htmlCompressor.compress(html) },
                url = it.link,
                createdDate = it.publishedDate?.toInstant()
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate()
            )
        }
        return articles
    }
}