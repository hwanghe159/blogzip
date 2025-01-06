package com.blogzip.crawler.service

import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class JsoupBlogMetadataScrapperTest {

    private lateinit var jsoupBlogMetadataScrapper: JsoupBlogMetadataScrapper

    @BeforeEach
    fun setUp() {
        this.jsoupBlogMetadataScrapper = JsoupBlogMetadataScrapper(RssFeedFetcher.create())
    }

    @Test
    fun getMetadata() {
        val metadata = jsoupBlogMetadataScrapper.getMetadata("https://tech.kakaobank.com/")
        println(metadata)
    }
}