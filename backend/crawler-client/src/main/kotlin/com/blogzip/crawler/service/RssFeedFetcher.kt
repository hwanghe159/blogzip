package com.blogzip.crawler.service

import com.blogzip.crawler.dto.Article
import com.blogzip.logger
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono
import reactor.netty.http.HttpProtocol
import reactor.netty.http.client.HttpClient
import java.io.BufferedReader
import java.io.StringReader
import java.time.ZoneId

// contents 또는 description 이 500자 이하인 경우, 요약본으로 판단.
private val SyndEntry.content: String?
  get() {
    val result = when {
      this.contents.isNotEmpty() -> {
        val content = this.contents[0].value
        if (content.isNullOrBlank() || content.length <= 500) {
          return null
        }
        content
      }
      else -> {
        val description = this.description?.value
        if (description.isNullOrBlank() || description.length <= 500) {
          return null
        }
        description
      }
    }

    val cDataRegex = "<!\\[CDATA\\[(.*?)]]>".toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
    return cDataRegex.find(result)?.groups?.get(1)?.value ?: result
  }

class RssFeedFetcher private constructor(
  private val xmlWebClient: WebClient,
) {

  val log = logger()

  private data class FetchedXml(
    val url: String,
    val statusCode: Int,
    val contentType: String?,
    val body: String,
  )

  companion object {
    fun create(): RssFeedFetcher {
      return RssFeedFetcher(
        xmlWebClient(),
      )
    }

    private fun xmlWebClient(): WebClient {
      val httpClient = HttpClient.create()
        .protocol(HttpProtocol.H2, HttpProtocol.HTTP11)

      return WebClient.builder()
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .defaultHeaders { headers ->
          headers.setAll(
            mapOf(
              HttpHeaders.ACCEPT to "application/rss+xml, application/atom+xml, application/xml, text/xml;q=0.9, */*;q=0.8",
              HttpHeaders.ACCEPT_LANGUAGE to "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
              HttpHeaders.CACHE_CONTROL to "max-age=0",
              HttpHeaders.PRAGMA to "no-cache",
              "Upgrade-Insecure-Requests" to "1",
              HttpHeaders.USER_AGENT to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
            )
          )
        }
        .exchangeStrategies(
          ExchangeStrategies.builder()
            .codecs { it.defaultCodecs().maxInMemorySize(-1) }
            .build()
        ).build()
    }
  }

  fun isContentContainsInRss(rss: String): Boolean {
    val articles: List<Article>

    try {
      articles = getArticles(rss)
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

  fun getArticles(rss: String): List<Article> {
    val fetchedXml = fetchXmlWithFallback(rss)

    // XML 에서 허용되지 않는 유니코드 문자 제거
    val validXmlString = fetchedXml.body.replace(
      "[^\\u0009\\r\\n\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]".toRegex(),
      ""
    )

    return convertToArticles(validXmlString)
  }

  private fun fetchXmlWithFallback(rss: String): FetchedXml {
    val primary = fetchXml(rss)
    return try {
      validateFetchedXml(primary)
      primary
    } catch (e: Exception) {
      if (!isRecoverableFetchError(e)) {
        throw e
      }

      log.warn("RSS 1차 요청 차단 감지. curl fallback 시도. rss=$rss, reason=${e.message}")

      val fallbackBody = fetchXmlByCurl(rss)
      val fallback = FetchedXml(
        url = rss,
        statusCode = 200,
        contentType = "application/xml",
        body = fallbackBody,
      )
      validateFetchedXml(fallback)
      fallback
    }
  }

  private fun fetchXml(rss: String): FetchedXml {
    return xmlWebClient
      .get()
      .uri(rss)
      .exchangeToMono { response ->
        if (response.statusCode().is3xxRedirection) {
          val newUrl = response.headers().header(HttpHeaders.LOCATION).firstOrNull()
          if (newUrl != null) {
            xmlWebClient.get()
              .uri(newUrl)
              .exchangeToMono { redirectedResponse -> toFetchedXml(newUrl, redirectedResponse) }
          } else {
            Mono.error(RuntimeException("rss url로부터 리다이렉트 응답이 왔으나 Location 헤더값이 없음"))
          }
        } else {
          toFetchedXml(rss, response)
        }
      }
      .block()
      ?: throw RuntimeException("rss 응답이 비어있음. rss=$rss")
  }

  private fun toFetchedXml(url: String, response: ClientResponse): Mono<FetchedXml> {
    val statusCode = response.statusCode().value()
    val contentType = response.headers().asHttpHeaders().contentType?.toString()
    return response.bodyToMono(String::class.java)
      .defaultIfEmpty("")
      .map { body ->
        FetchedXml(
          url = url,
          statusCode = statusCode,
          contentType = contentType,
          body = body,
        )
      }
  }

  private fun fetchXmlByCurl(rss: String): String {
    val process = ProcessBuilder(
      "curl",
      "-sS",
      "-L",
      "--http2",
      "--max-time",
      "35",
      rss,
      "-H",
      "accept: application/rss+xml, application/atom+xml, application/xml, text/xml;q=0.9, */*;q=0.8",
      "-H",
      "accept-language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
      "-H",
      "cache-control: max-age=0",
      "-H",
      "pragma: no-cache",
      "-H",
      "upgrade-insecure-requests: 1",
      "-H",
      "user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36",
    )
      .redirectErrorStream(true)
      .start()

    val output = process.inputStream.bufferedReader().use { it.readText() }
    val exitCode = process.waitFor()
    if (exitCode != 0) {
      throw RuntimeException("RSS_CURL_FETCH_FAILED: exit=$exitCode, rss=$rss, output=${preview(output)}")
    }

    return output
  }

  private fun validateFetchedXml(fetchedXml: FetchedXml) {
    val trimmedBody = fetchedXml.body.trimStart()
    val normalizedLowerBody = trimmedBody.lowercase()
    if (isBlockedResponse(fetchedXml.statusCode, normalizedLowerBody)) {
      throw RuntimeException(
        "RSS_FETCH_BLOCKED: status=${fetchedXml.statusCode}, contentType=${fetchedXml.contentType}, url=${fetchedXml.url}, preview=${preview(trimmedBody)}"
      )
    }
    if (!isXmlResponse(fetchedXml.contentType, trimmedBody, normalizedLowerBody)) {
      throw RuntimeException(
        "RSS_NOT_XML_RESPONSE: status=${fetchedXml.statusCode}, contentType=${fetchedXml.contentType}, url=${fetchedXml.url}, preview=${preview(trimmedBody)}"
      )
    }
  }

  private fun isRecoverableFetchError(error: Exception): Boolean {
    val message = error.message.orEmpty()
    return message.startsWith("RSS_FETCH_BLOCKED:") || message.startsWith("RSS_NOT_XML_RESPONSE:")
  }

  private fun isBlockedResponse(statusCode: Int, normalizedLowerBody: String): Boolean {
    val hasBlockedStatus = statusCode in setOf(401, 403, 429, 451, 503)
    val hasBlockKeyword =
      normalizedLowerBody.contains("the request could not be satisfied") ||
        normalizedLowerBody.contains("generated by cloudfront") ||
        normalizedLowerBody.contains("access denied") ||
        normalizedLowerBody.contains("request blocked") ||
        normalizedLowerBody.contains("보안 위배 접근 제한 페이지") ||
        normalizedLowerBody.contains("올바르지 않은 요청으로 페이지를 보실 수 없습니다.")

    return hasBlockKeyword || (hasBlockedStatus && normalizedLowerBody.startsWith("<!doctype html"))
  }

  private fun isXmlResponse(contentType: String?, trimmedBody: String, normalizedLowerBody: String): Boolean {
    val normalizedContentType = contentType?.lowercase().orEmpty()
    val xmlContentType =
      normalizedContentType.contains("xml") ||
        normalizedContentType.contains("rss") ||
        normalizedContentType.contains("atom")
    val xmlLikeBody =
      normalizedLowerBody.startsWith("<?xml") ||
        normalizedLowerBody.startsWith("<rss") ||
        normalizedLowerBody.startsWith("<feed")
    val htmlBody =
      normalizedLowerBody.startsWith("<!doctype html") ||
        normalizedLowerBody.startsWith("<html")

    return !htmlBody && (xmlContentType || xmlLikeBody || looksLikeXml(trimmedBody))
  }

  private fun looksLikeXml(body: String): Boolean {
    val firstTag = "<([A-Za-z0-9:_-]+)".toRegex().find(body)?.groups?.get(1)?.value?.lowercase() ?: return false
    return firstTag in setOf("rss", "feed", "rdf:rdf")
  }

  private fun preview(body: String): String {
    val oneLine = body.replace("\\s+".toRegex(), " ").trim()
    val maxLength = 180
    return if (oneLine.length <= maxLength) {
      oneLine
    } else {
      "${oneLine.take(maxLength)}..."
    }
  }

  private fun convertToArticles(xml: String): List<Article> {
    val entries = try {
      parseEntries(xml, allowDoctypes = false)
    } catch (e: Exception) {
      if (!isDoctypeDisallowedError(e)) {
        throw e
      }

      log.warn("DOCTYPE 선언이 포함된 RSS 파싱에 실패하여 DOCTYPE 허용 모드로 재시도합니다. message=${e.message}")
      parseEntries(xml, allowDoctypes = true)
    }
    val articles = entries.map {
      Article(
        title = it.title,
        content = it.content,
        url = it.link,
        createdDate = it.publishedDate?.toInstant()
          ?.atZone(ZoneId.systemDefault())
          ?.toLocalDate()
      )
    }
    return articles
  }

  private fun parseEntries(xml: String, allowDoctypes: Boolean) =
    SyndFeedInput().apply {
      setAllowDoctypes(allowDoctypes)
    }.build(BufferedReader(StringReader(xml))).entries

  private fun isDoctypeDisallowedError(throwable: Throwable): Boolean {
    var current: Throwable? = throwable
    while (current != null) {
      val message = current.message.orEmpty()
      if (
        message.contains("disallow-doctype-decl", ignoreCase = true) ||
        message.contains("doctype is disallowed", ignoreCase = true)
      ) {
        return true
      }
      current = current.cause
    }
    return false
  }
}
