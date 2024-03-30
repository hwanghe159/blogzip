package com.blogzip.crawler.service

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@Component
class ArticleFetcher {

    fun fetchArticles(rss: String, from: LocalDate): List<ArticleData> {
        try {
            val client = WebClient.create()
            val response = client.get()
                .uri(rss)
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
            val feedInput = SyndFeedInput()
//        val feed = feedInput.build(response.byteInputStream())
            // todo webClient 로 redirection 처리하고, 응답을 rome으로 파싱.

            val feed = SyndFeedInput().build(XmlReader(URL(rss)))
            return feed.entries
                .filter {
                    val publishedDate: Date = it.publishedDate ?: it.updatedDate
                    from <= publishedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                }
                .map {
                    val publishedDate: Date = it.publishedDate ?: it.updatedDate
                    ArticleData(
                        title = it.title,
                        url = it.link,
                        createdDate = publishedDate.toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    )
                }
        } catch (e: Exception) {
            // todo com.rometools.rome.io.ParsingFeedException: Invalid XML: Error on line 45: An invalid XML character (Unicode: 0xffff) was found in the CDATA section.
            // todo com.rometools.rome.io.ParsingFeedException: Invalid XML: Error on line 2410: An invalid XML character (Unicode: 0x1f) was found in the element content of the document.
            // todo com.rometools.rome.io.ParsingFeedException: Invalid XML: Error on line 36: An invalid XML character (Unicode: 0x1c) was found in the CDATA section.
            // todo com.rometools.rome.io.ParsingFeedException: Invalid XML: Error on line 6: The element type "hr" must be terminated by the matching end-tag "</hr>".
            System.err.println("${rss} 가져오기 실패.")
            e.printStackTrace()
            return emptyList()
        }

    }

    class ArticleData(val title: String, val url: String, val createdDate: LocalDate)
}