package com.blogzip.crawler.service

class RemoteWebScrapper(
  private val crawlerHttpClient: CrawlerHttpClient,
) : WebScrapper {
  override fun getContent(url: String): String? {
    return crawlerHttpClient.getContent(url)
  }

  override fun getArticles(
    blogUrl: String,
    cssSelector: String,
    articleUrls: Set<String>,
  ): WebScrapper.ScrapResult {
    return crawlerHttpClient.getArticles(
      blogUrl = blogUrl,
      cssSelector = cssSelector,
      articleUrls = articleUrls,
    )
  }

  override fun test(url: String): String? {
    return crawlerHttpClient.getContent(url)
  }

  override fun endUse() {
    // 원격 crawler 서비스는 별도 프로세스에서 관리한다.
  }
}
