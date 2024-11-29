package com.blogzip.crawler.service

import com.blogzip.crawler.config.SeleniumProperties
import com.blogzip.crawler.dto.BlogMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class BlogMetadataScrapperTest {

    private lateinit var blogMetadataScrapper: BlogMetadataScrapper

    @BeforeEach
    fun setUp() {
        this.blogMetadataScrapper =
            BlogMetadataScrapper.create(SeleniumProperties(listOf("--window-size=1920,1080")))
    }

    @AfterEach
    fun tearDown() {
        blogMetadataScrapper.endUse()
    }

    @Test
    fun `thread safe 테스트`() {
        val datas: MutableList<BlogMetadata> =
            Collections.synchronizedList(mutableListOf())
        val threads = listOf(
            "https://techblog.woowahan.com",
            "https://d2.naver.com/helloworld",
            "https://techblog.lycorp.co.jp/ko",
            "https://hyperconnect.github.io",
        ).map { url -> Thread { datas.add(blogMetadataScrapper.getMetadata(url)) } }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        datas.forEach { println(it) }
        assertThat(datas).hasSize(threads.size)
        assertThat(datas.map { it.title }.distinct()).hasSize(threads.size)
    }
}