package com.blogzip.crawler.service

import com.blogzip.crawler.common.logger
import com.blogzip.crawler.dto.Article
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class RssFeedFetcher(
    private val xmlWebClient: WebClient,
    private val xmlParser: XmlParser
) {

    val log = logger()

    fun isContentContainsInRss(rss: String): Boolean {
        val articles: List<Article>

        try {
            val validXmlString = fetchXmlString(rss)
            articles = xmlParser.convertToArticles(validXmlString)
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

    fun fetchXmlString(rss: String): String {
        val xmlString = xmlWebClient
            .get()
            .uri(rss)
            .exchangeToMono { response ->
                if (response.statusCode().is3xxRedirection) {
                    val newUrl = response.headers().header("Location").firstOrNull()
                    if (newUrl != null) {
                        xmlWebClient.get()
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
        return validXmlString
    }
}