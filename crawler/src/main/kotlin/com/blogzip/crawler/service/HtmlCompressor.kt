package com.blogzip.crawler.service

import com.blogzip.crawler.common.logger
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class HtmlCompressor {

    val log = logger()

    fun compress(html: String): String {
        val body = Jsoup.parse(html).body()
        for (element in body.allElements) {
            val iterator = element.attributes().iterator()
            while (iterator.hasNext()) {
                iterator.next()
                iterator.remove()
            }
        }
        body.select("script").forEach { it.remove() }

        // html -> md
        val result = FlexmarkHtmlConverter.builder().build().convert(body.toString())
        if (result.isBlank()) {
            log.error("html -> md 변환 실패. html = ${html.substring(0, 100)}")
            return html
        }
        return result
    }
}