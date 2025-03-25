package com.blogzip.batch.fetch

import com.blogzip.domain.Article
import com.blogzip.domain.Blog
import java.time.LocalDate

interface NewArticlesFetcher {

  fun fetchArticles(blog: Blog, from: LocalDate): List<Article>
}