package com.blogzip.crawler.service

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class HtmlCompressor {

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
        return FlexmarkHtmlConverter.builder().build().convert(body.toString())
//        return body.toString().replace(Regex(">\\s+<"), "><")
    }
}