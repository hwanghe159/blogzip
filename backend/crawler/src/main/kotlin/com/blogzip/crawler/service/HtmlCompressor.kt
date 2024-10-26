package com.blogzip.crawler.service

import com.blogzip.logger
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.jsoup.Jsoup

class HtmlCompressor {

    val log = logger()

    // 불필요한 정보 제거 후 MD로 변환
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
        return result
    }
}