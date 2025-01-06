package com.blogzip.crawler.service

import com.blogzip.crawler.vo.MediumUrl
import com.blogzip.crawler.vo.VelogUrl
import com.blogzip.service.BlogMetadataScrapper
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

class JsoupBlogMetadataScrapper(
    private val rssFeedFetcher: RssFeedFetcher,
) : BlogMetadataScrapper {

    override fun getMetadata(url: String): BlogMetadataScrapper.BlogMetadata {
        // URL로 HTML 문서를 가져오기
        val document = fetchDocument(url)

        // title 추출
        val title: String = document.title()

        // imageUrl 추출 및 절대경로 변환
        val imageUrl: String? = document.select("meta[property=og:image], meta[name=og:image]")
            .attr("content")
            .takeIf { it.isNotBlank() }
            ?.let { convertIfRelative(it, url) }

        // rssUrl를 추출하고, 추출한 rssUrl의 응답이 4XX, 5XX인 경우 null로 세팅
        val rssUrl = findRssUrl(url, document)?.let { validateRssUrl(it) }

        // RSS 내용을 검사하여 게시물 글 내용 포함 여부 확인
        val isContentContainsInRss =
            rssUrl?.let { rssFeedFetcher.isContentContainsInRss(it) } ?: false

        return BlogMetadataScrapper.BlogMetadata(
            title = title,
            imageUrl = imageUrl,
            rss = rssUrl,
            isContentContainsInRss = isContentContainsInRss
        )
    }

    private fun fetchDocument(url: String): Document {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0") // 사용자 에이전트 설정
            .timeout(10_000)         // 타임아웃 설정
            .get()
    }

    private fun findRssUrl(url: String, document: Document): String? {
        if (VelogUrl.isVelogUrl(url)) {
            return VelogUrl(url).rssUrl()
        }
        if (MediumUrl.isMediumUrl(url)) {
            return MediumUrl(url).rssUrl()
        }
        val rssLink: String? =
            document.select("link[type='application/rss+xml'], link[type='application/atom+xml']")
                .firstOrNull()
                ?.attr("href")
                ?.takeIf { it.isNotBlank() }
        if (rssLink != null) {
            return convertIfRelative(rssLink, url)
        }

        val rssPaths = listOf("/rss", "/feed", "/rss.xml", "/feed.xml")
        val validContentTypes = setOf(
            "application/xml",
            "application/rss+xml",
            "application/atom+xml",
            "text/xml"
        )
        for (path in rssPaths) {
            val testUrl = "$url$path"
            try {
                val response = Jsoup.connect(testUrl)
                    .userAgent("Mozilla/5.0") // 사용자 에이전트 설정
                    .timeout(10_000)         // 타임아웃 설정
                    .ignoreHttpErrors(true) // HTTP 에러 무시
                    .method(Connection.Method.HEAD) // HEAD 요청으로 Content-Type만 확인
                    .execute()

                // Content-Type 확인
                val contentType = response.contentType()?.split(";")
                    ?.firstOrNull() // "text/xml; charset=utf-8" 등에서 "text/xml"만 추출
                if (contentType in validContentTypes) {
                    return testUrl // 유효한 Content-Type 발견 시 URL 반환
                }
            } catch (e: Exception) {
                // 네트워크 에러나 예외 발생 시 무시하고 다음 경로 시도
                println("Error checking $testUrl: ${e.message}")
            }
        }
        return null
    }

    private fun convertIfRelative(url: String, baseUrl: String): String {
        val uri = URI(url)
        return if (uri.isAbsolute) {
            url
        } else {
            val baseUri = URI(baseUrl).resolve("/")
            baseUri.resolve(uri).toString()
        }
    }

    private fun validateRssUrl(rssUrl: String): String? {
        return try {
            val response = Jsoup.connect(rssUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .ignoreHttpErrors(true)
                .execute()

            if (response.statusCode() in 400..599) {
                null
            } else {
                rssUrl
            }
        } catch (e: Exception) {
            null
        }
    }
}