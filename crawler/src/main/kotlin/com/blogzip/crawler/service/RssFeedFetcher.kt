package com.blogzip.crawler.service

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.BufferedReader
import java.io.StringReader
import java.time.LocalDate
import java.time.ZoneId

private val SyndEntry.content: String?
    get() {
        if (this.contents.isNotEmpty()) {
            return this.contents[0].value
        }
        // description 에 전체 내용이 아닌 요약만 있는 경우도 있음. 500자 이하인 경우엔 전체 내용이 아니고 요약본이라고 판단한다.
        if (this.description.value.length <= 500) {
            return null
        }
        return this.description.value
    }

@Component
class RssFeedFetcher(
    private val webClient: WebClient,
    private val htmlCompressor: HtmlCompressor,
) {

    fun isContentContainsInRss(rss: String): Boolean {
        val articles = fetchArticles(rss)
        if (articles.isEmpty()) {
            return false
        }
        if (articles[0].content == null) {
            return false
        }
        return true
    }

    fun fetchArticles(rss: String): List<ArticleData> {
        try {
            val input = SyndFeedInput()
            val xmlString = webClient
                .get()
                .uri(rss)
                .exchangeToMono { response ->
                    if (response.statusCode().is3xxRedirection) {
                        val newUrl = response.headers().header("Location").firstOrNull()
                        if (newUrl != null) {
                            webClient.get().uri(newUrl).retrieve().bodyToMono(String::class.java)
                        } else {
                            Mono.error(RuntimeException("rss url로부터 리다이렉트 응답이 왔으나 Location 헤더값이 없음"))
                        }
                    } else {
                        response.bodyToMono(String::class.java)
                    }
                }
                .block()

            val validXmlString = xmlString!!.replace(
                "[^\\u0009\\r\\n\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]".toRegex(),
                ""
            )
            val entries = input.build(BufferedReader(StringReader(validXmlString))).entries
            return entries.map {
                ArticleData(
                    title = it.title,
                    content = it.content?.let { html -> htmlCompressor.compress(html) },
                    url = it.link,
                    createdDate = it.publishedDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    data class ArticleData(
        val title: String,
        val content: String?,
        val url: String,
        val createdDate: LocalDate
    )
}