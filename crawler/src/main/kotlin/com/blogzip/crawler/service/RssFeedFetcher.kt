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
import java.util.*


@Component
class RssFeedFetcher(private val webClient: WebClient) {

    fun fetchContents(rss: String, from: LocalDate): List<ArticleData> {
        return fetchFeeds(rss)
            .filter {
                val publishedDate: Date = it.publishedDate ?: it.updatedDate
                from <= publishedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .map {
                ArticleData(
                    title = it.title,
                    content = it.contents[0].value,
                    url = it.link,
                    createdDate = it.publishedDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                )
            }
    }

    fun fetchLinks(rss: String, from: LocalDate): List<ArticleData> {
        return fetchFeeds(rss)
            .filter {
                val publishedDate: Date = it.publishedDate ?: it.updatedDate
                from <= publishedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .map {
                ArticleData(
                    title = it.title,
                    content = null,
                    url = it.link,
                    createdDate = it.publishedDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                )
            }
    }

    fun isContentContainsInRss(rss: String): Boolean {
        val feeds = fetchFeeds(rss)
        if (feeds.isEmpty()) {
            return false
        }
        if (feeds[0].contents.isEmpty()) {
            return false
        }
        return true
    }

    private fun fetchFeeds(rss: String): List<SyndEntry> {
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
            return input.build(BufferedReader(StringReader(validXmlString))).entries
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    class ArticleData(
        val title: String,
        val content: String?,
        val url: String,
        val createdDate: LocalDate
    )
}