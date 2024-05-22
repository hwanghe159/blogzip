package com.blogzip.crawler.service

import com.blogzip.crawler.common.logger
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.BufferedReader
import java.io.StringReader
import java.time.LocalDate
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
class RssFeedFetcher(
    private val webClient: WebClient,
    private val htmlCompressor: HtmlCompressor,
) {

    val log = logger()

    fun isContentContainsInRss(rss: String): Boolean {
        val articles: List<ArticleData>

        try {
            articles = fetchArticles(rss)
        } catch (e: Exception) {
            return false
        }

        if (articles.isEmpty()) {
            return false
        }
        if (articles[0].content == null) {
            return false
        }
        return true
    }

    fun fetchArticles(rss: String): List<ArticleData> {
        val input = SyndFeedInput()
        val xmlString = webClient
            .get()
            .uri(rss)
            .exchangeToMono { response ->
                if (response.statusCode().is3xxRedirection) {
                    val newUrl = response.headers().header("Location").firstOrNull()
                    if (newUrl != null) {
                        webClient.get()
                            .uri(newUrl)
                            .retrieve()
                            .bodyToMono(String::class.java)
                    } else {
                        Mono.error(RuntimeException("rss url로부터 리다이렉트 응답이 왔으나 Location 헤더값이 없음"))
                    }
                } else {
                    response.bodyToMono(String::class.java)
                }
            }
            .block()

        // XML 에서 허용되지 않는 유니코드 문자 제거
        val validXmlString = xmlString!!.replace(
            "[^\\u0009\\r\\n\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]".toRegex(),
            ""
        )
        val entries = input.build(BufferedReader(StringReader(validXmlString))).entries
        val articles = entries.map {
            ArticleData(
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

    data class ArticleData(
        val title: String,
        val content: String?,
        val url: String,
        val createdDate: LocalDate?
    )
}